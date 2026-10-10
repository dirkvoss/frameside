package com.dirkvoss.frameside.tv;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Gemerkte Server-Adresse und die Suche nach Frameside im Heimnetz (Netz /24 des Geraets, Ports 8090 und 80). */
final class Server {
    private Server() {}

    static final String PREFS = "frameside";
    static final String KEY = "server";

    static String gemerkt(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "");
    }

    static void merken(Context c, String adresse) {
        SharedPreferences.Editor e = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        if (adresse == null || adresse.isEmpty()) e.remove(KEY); else e.putString(KEY, adresse);
        e.apply();
    }

    /** "192.168.1.20:8090", "http://nas.local" oder "https://tv.example.com/" -> "http://192.168.1.20:8090" (ohne Schraegstrich am Ende). */
    static String normal(String roh) {
        if (roh == null) return "";
        String s = roh.trim();
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        if (s.isEmpty()) return "";
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://" + s;
        return s;
    }

    static final class Treffer {
        final String adresse, name, version;
        Treffer(String adresse, String name, String version) { this.adresse = adresse; this.name = name; this.version = version; }
        @Override public String toString() { return name + "  " + adresse.replace("http://", "") + (version.isEmpty() ? "" : "  (" + version + ")"); }
    }

    /** Fragt /api/config ab; Frameside erkennt man an den Feldern "name", "version" und "konfiguriert". Sonst null. */
    static Treffer pruefen(String basis, int timeoutMs) {
        HttpURLConnection h = null;
        try {
            h = (HttpURLConnection) new URL(basis + "/api/config").openConnection();
            h.setConnectTimeout(timeoutMs);
            h.setReadTimeout(timeoutMs + 500);
            if (h.getResponseCode() != 200) return null;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(h.getInputStream(), "UTF-8"))) {
                String z;
                while ((z = r.readLine()) != null && sb.length() < 200000) sb.append(z);
            }
            JSONObject o = new JSONObject(sb.toString());
            if (!o.has("version") || !o.has("konfiguriert") || !o.has("name")) return null;
            return new Treffer(basis, o.optString("name", "Frameside"), o.optString("version", ""));
        } catch (Exception e) {
            return null;
        } finally {
            if (h != null) h.disconnect();
        }
    }

    interface Rueckruf { void gefunden(Treffer t); void fertig(); }

    /** Sucht im eigenen /24-Netz in Hintergrund-Threads; ruft {@code gefunden} fuer jeden Treffer auf (nicht im UI-Thread). */
    static void suchen(Context c, final Rueckruf cb) {
        final String netz = eigenesNetz(c);
        new Thread(() -> {
            if (netz == null) { cb.fertig(); return; }
            ExecutorService pool = Executors.newFixedThreadPool(48);
            final int[] ports = {8090, 80};
            for (int i = 1; i < 255; i++) {
                final String host = netz + i;
                for (final int port : ports) {
                    pool.execute(() -> {
                        Treffer t = pruefen("http://" + host + (port == 80 ? "" : ":" + port), 700);
                        if (t != null) cb.gefunden(t);
                    });
                }
            }
            pool.shutdown();
            try { pool.awaitTermination(40, TimeUnit.SECONDS); } catch (InterruptedException ignored) { }
            cb.fertig();
        }, "frameside-suche").start();
    }

    /** "192.168.1." fuer ein Geraet mit 192.168.1.x (nur private IPv4-Netze). */
    static String eigenesNetz(Context c) {
        try {
            List<NetworkInterface> alle = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : alle) {
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> ad = ni.getInetAddresses();
                while (ad.hasMoreElements()) {
                    InetAddress a = ad.nextElement();
                    byte[] b = a.getAddress();
                    if (b.length != 4) continue;
                    int b0 = b[0] & 0xff, b1 = b[1] & 0xff;
                    boolean privat = b0 == 10 || (b0 == 192 && b1 == 168) || (b0 == 172 && b1 >= 16 && b1 <= 31);
                    if (privat) return b0 + "." + b1 + "." + (b[2] & 0xff) + ".";
                }
            }
        } catch (Exception ignored) { }
        return null;
    }
}
