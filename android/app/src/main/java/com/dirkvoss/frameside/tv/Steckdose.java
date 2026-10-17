package com.dirkvoss.frameside.tv;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Eine schaltbare Steckdose im selben WLAN: Shelly der 1. Generation, Shelly Gen2/Gen3/Gen4 (Plus, Plug S ...) oder zwei beliebige Web-Adressen (Ein/Aus, z. B. Tasmota).
 * Alle Aufrufe blockieren und gehoeren in einen Hintergrund-Thread. Es wird nur das lokale Netz benutzt, kein Konto und keine Cloud.
 */
abstract class Steckdose {
    /** true = an, false = aus, null = nicht erreichbar oder unbekannt. */
    abstract Boolean istAn();

    abstract boolean schalte(boolean an);

    /** Sicherung im Geraet selbst, damit auch bei Ausfall der App wieder eingeschaltet wird: nach Stromausfall AN und nach {@code sek} Sekunden AUS von selbst wieder AN. */
    boolean absichern(int sek) { return true; }

    String kennung() { return ""; }

    // ------------------------------------------------------------------ Shelly

    static final class Fund {
        final String host, id, name;
        Fund(String host, String id, String name) { this.host = host; this.id = id; this.name = name; }
        @Override public String toString() { return name + "  " + host + "  (" + (id.length() > 6 ? id.substring(id.length() - 6) : id) + ")"; }
    }

    /** Fragt /shelly ab; ein Shelly meldet dort "id" (Gen2+: dazu "gen"; Gen1: "type" und "mac"). Sonst null. */
    static Fund shellyPruefen(String host, int ms) {
        String s = get("http://" + host + "/shelly", ms);
        if (s == null) return null;
        try {
            JSONObject o = new JSONObject(s);
            if (o.has("id") && o.has("gen")) return new Fund(host, o.getString("id"), o.optString("app", o.getString("id")));
            if (o.has("type") && o.has("mac")) return new Fund(host, "shelly-" + o.getString("mac").toLowerCase(), o.getString("type"));
        } catch (Exception e) { /* kein Shelly */ }
        return null;
    }

    /** Sucht im eigenen /24-Netz alle Shelly. Ergebnis nach Name sortiert. */
    static List<Fund> suchen(android.content.Context c) {
        final List<Fund> liste = Collections.synchronizedList(new ArrayList<Fund>());
        final String netz = Server.eigenesNetz(c);
        if (netz == null) return liste;
        ExecutorService pool = Executors.newFixedThreadPool(48);
        for (int i = 1; i < 255; i++) {
            final String host = netz + i;
            pool.execute(() -> { Fund f = shellyPruefen(host, 700); if (f != null) liste.add(f); });
        }
        pool.shutdown();
        try { pool.awaitTermination(30, TimeUnit.SECONDS); } catch (InterruptedException ignored) { }
        List<Fund> r = new ArrayList<>(liste);
        Collections.sort(r, new Comparator<Fund>() { @Override public int compare(Fund a, Fund b) { return a.id.compareTo(b.id); } });
        return r;
    }

    /** Eine Steckdose zum Shelly unter {@code host}; null, wenn dort kein Shelly antwortet. */
    static Steckdose shelly(String host) {
        String s = get("http://" + host + "/shelly", 2500);
        if (s == null) return null;
        try {
            JSONObject o = new JSONObject(s);
            if (o.has("gen") && o.optInt("gen", 1) >= 2) return new ShellyGen2(host, o.optString("id", ""));
            if (o.has("type")) return new ShellyGen1(host, "shelly-" + o.optString("mac", "").toLowerCase());
        } catch (Exception e) { /* unlesbar */ }
        return null;
    }

    static final class ShellyGen2 extends Steckdose {
        private final String host, id;
        ShellyGen2(String host, String id) { this.host = host; this.id = id; }

        @Override String kennung() { return id; }

        @Override Boolean istAn() {
            String s = get("http://" + host + "/rpc/Switch.GetStatus?id=0", 4000);
            try { return s == null ? null : (Boolean) new JSONObject(s).getBoolean("output"); } catch (Exception e) { return null; }
        }

        @Override boolean schalte(boolean an) {
            return get("http://" + host + "/rpc/Switch.Set?id=0&on=" + an, 4000) != null;
        }

        @Override boolean absichern(int sek) {
            String body = "{\"id\":0,\"config\":{\"initial_state\":\"on\",\"auto_on\":true,\"auto_on_delay\":" + sek + "}}";
            return post("http://" + host + "/rpc/Switch.SetConfig", body, 4000) != null;
        }
    }

    static final class ShellyGen1 extends Steckdose {
        private final String host, id;
        ShellyGen1(String host, String id) { this.host = host; this.id = id; }

        @Override String kennung() { return id; }

        @Override Boolean istAn() {
            String s = get("http://" + host + "/relay/0", 4000);
            try { return s == null ? null : (Boolean) new JSONObject(s).getBoolean("ison"); } catch (Exception e) { return null; }
        }

        @Override boolean schalte(boolean an) {
            return get("http://" + host + "/relay/0?turn=" + (an ? "on" : "off"), 4000) != null;
        }

        @Override boolean absichern(int sek) {
            return get("http://" + host + "/settings/relay/0?default_state=on&auto_on=" + sek, 4000) != null;
        }
    }

    // ------------------------------------------------------------------ beliebige Web-Adressen (Tasmota, Home Assistant, eigene Schalter ...)

    static final class Adressen extends Steckdose {
        private final String an, aus;
        private volatile Boolean zuletzt;
        Adressen(String an, String aus) { this.an = an; this.aus = aus; }

        @Override Boolean istAn() { return zuletzt; }                      // diese Steckdosen melden ihren Zustand nicht: es zaehlt der letzte Befehl (er wird bei Bedarf erneut gesendet)

        @Override boolean schalte(boolean ein) {
            boolean ok = get(ein ? an : aus, 4000) != null;
            if (ok) zuletzt = ein;
            return ok;
        }
    }

    // ------------------------------------------------------------------ HTTP

    static String get(String adresse, int ms) { return anfrage(adresse, null, ms); }

    static String post(String adresse, String json, int ms) { return anfrage(adresse, json, ms); }

    private static String anfrage(String adresse, String body, int ms) {
        if (adresse == null || !adresse.startsWith("http")) return null;
        HttpURLConnection h = null;
        try {
            h = (HttpURLConnection) new URL(adresse).openConnection();
            h.setConnectTimeout(ms);
            h.setReadTimeout(ms);
            if (body != null) {
                h.setRequestMethod("POST");
                h.setDoOutput(true);
                h.setRequestProperty("Content-Type", "application/json");
                try (OutputStream o = h.getOutputStream()) { o.write(body.getBytes("UTF-8")); }
            }
            if (h.getResponseCode() / 100 != 2) return null;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(h.getInputStream(), "UTF-8"))) {
                String z;
                while ((z = r.readLine()) != null && sb.length() < 100000) sb.append(z);
            }
            return sb.length() == 0 ? "{}" : sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (h != null) h.disconnect();
        }
    }
}
