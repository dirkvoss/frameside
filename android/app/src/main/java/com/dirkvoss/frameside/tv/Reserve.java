package com.dirkvoss.frameside.tv;

import android.content.Context;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Foto-Reserve in der App: Die Seite und das, was der Rahmen zum Weiterlaufen braucht, werden auf dem Geraet zwischengespeichert.
 * Faellt das Netz oder der Server aus (auch ohne HTTPS), zeigt der Rahmen weiter schon gesehene Fotos, statt eine Fehlerseite zu zeigen.
 * Fotos: zuerst der Speicher (ein Foto aendert sich nie), sonst das Netz. Seite, Einstellungen und Foto-Liste: zuerst das Netz, bei Ausfall der Speicher.
 */
final class Reserve {
    private Reserve() {}

    private static final int FOTOS_MAX = 150;                 // so viele Fotos bleiben auf dem Geraet (je Foto meist unter 1 MB)
    private static final long FOTO_ALTER_MS = 7L * 24 * 3600 * 1000;
    private static final int GROESSE_MAX = 25 * 1024 * 1024;
    private static final long AUSFALL_PAUSE_MS = 20_000;      // nach einem Netzfehler 20 s lang gar nicht erst das Netz versuchen
    private static volatile long ausfallBis = 0;
    private static final Random ZUFALL = new Random();

    private enum Art { FOTO, SEITE }

    /** Antwort fuer eine Anfrage der Seite oder null, wenn die WebView sie selbst bearbeiten soll. */
    static WebResourceResponse holen(Context c, String server, WebResourceRequest r) {
        try {
            if (r == null || !"GET".equals(r.getMethod()) || server == null || server.isEmpty()) return null;
            Uri u = r.getUrl();
            Uri s = Uri.parse(server);
            if (u == null || s.getHost() == null || !s.getHost().equalsIgnoreCase(u.getHost()) || port(s) != port(u) || !s.getScheme().equalsIgnoreCase(u.getScheme())) return null;
            Art art = art(u);
            if (art == null) return null;
            File dir = new File(c.getCacheDir(), "reserve");
            if (!dir.isDirectory() && !dir.mkdirs()) return null;
            File datei = new File(dir, art == Art.FOTO ? "f-" + hash(u.toString()) : "s-" + hash(u.toString()));
            if (art == Art.FOTO && datei.isFile() && System.currentTimeMillis() - datei.lastModified() < FOTO_ALTER_MS) {
                datei.setLastModified(System.currentTimeMillis());
                WebResourceResponse a = ausDatei(datei);
                if (a != null) return a;
            }
            if (System.currentTimeMillis() >= ausfallBis) {
                try {
                    Map<String, String> kopf = new HashMap<>();
                    String cookie = CookieManager.getInstance().getCookie(u.toString());
                    if (cookie != null) kopf.put("Cookie", cookie);
                    for (Map.Entry<String, String> e : r.getRequestHeaders().entrySet()) {
                        String k = e.getKey();
                        if (k.equalsIgnoreCase("User-Agent") || k.equalsIgnoreCase("Accept") || k.equalsIgnoreCase("Accept-Language")) kopf.put(k, e.getValue());
                    }
                    HttpURLConnection h = (HttpURLConnection) new URL(u.toString()).openConnection();
                    try {
                        h.setConnectTimeout(4000);
                        h.setReadTimeout(20000);
                        for (Map.Entry<String, String> e : kopf.entrySet()) h.setRequestProperty(e.getKey(), e.getValue());
                        int code = h.getResponseCode();
                        if (code >= 500) throw new IOException("Server " + code);               // z. B. 502 vom Proxy: wie ein Ausfall behandeln
                        InputStream in = code >= 400 ? h.getErrorStream() : h.getInputStream();
                        byte[] roh = in == null ? new byte[0] : lesen(in);
                        String typ = h.getContentType() == null ? "application/octet-stream" : h.getContentType();
                        if (code == 200) {
                            speichern(datei, typ, roh);
                            if (art == Art.FOTO) aufraeumen(dir);
                        }
                        String mime = typ.split(";")[0].trim();
                        String zeichensatz = typ.toLowerCase().contains("charset=") ? typ.substring(typ.toLowerCase().indexOf("charset=") + 8).trim() : (mime.startsWith("text/") || mime.contains("json") || mime.contains("javascript") ? "utf-8" : null);
                        Map<String, String> antwortKopf = new HashMap<>();
                        antwortKopf.put("Cache-Control", "no-cache");
                        WebResourceResponse a = new WebResourceResponse(mime, zeichensatz, code, h.getResponseMessage() == null || h.getResponseMessage().isEmpty() ? "OK" : h.getResponseMessage(), antwortKopf, new ByteArrayInputStream(roh));
                        return a;
                    } finally {
                        h.disconnect();
                    }
                } catch (IOException e) {
                    ausfallBis = System.currentTimeMillis() + AUSFALL_PAUSE_MS;
                }
            }
            // Netz oder Server nicht erreichbar: aus dem Speicher
            if (datei.isFile()) { WebResourceResponse a = ausDatei(datei); if (a != null) return a; }
            if (art == Art.FOTO) {                                                           // ein anderes, schon gesehenes Foto ist besser als ein leerer Bildschirm
                File[] fotos = dir.listFiles((d, n) -> n.startsWith("f-") && !n.endsWith(".typ") && !n.endsWith(".neu"));
                if (fotos != null && fotos.length > 0) { WebResourceResponse a = ausDatei(fotos[ZUFALL.nextInt(fotos.length)]); if (a != null) return a; }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static int port(Uri u) {
        if (u.getPort() > 0) return u.getPort();
        return "https".equalsIgnoreCase(u.getScheme()) ? 443 : 80;
    }

    /** Was zwischengespeichert wird: die Rahmen-Seite samt Zubehoer, die Einstellungen, die Foto-Liste des Dauerprogramms und die grossen Fotos. */
    private static Art art(Uri u) {
        String p = u.getPath() == null ? "" : u.getPath();
        if (p.startsWith("/api/vorschau/")) {
            String s = u.getQueryParameter("s");
            return "gross".equals(s) || "voll".equals(s) ? Art.FOTO : null;
        }
        if (p.equals("/api/config") || p.equals("/api/rahmen/zufall")) return Art.SEITE;
        if (p.equals("/tv") || p.startsWith("/tv/") || p.equals("/i18n.js") || p.startsWith("/i18n/") || p.equals("/qrcode.js") || p.startsWith("/fonts/") || p.startsWith("/icon-")) return Art.SEITE;
        return null;
    }

    private static byte[] lesen(InputStream in) throws IOException {
        try (InputStream i = in) {
            ByteArrayOutputStream o = new ByteArrayOutputStream(64 * 1024);
            byte[] b = new byte[16 * 1024];
            int n;
            while ((n = i.read(b)) > 0) {
                o.write(b, 0, n);
                if (o.size() > GROESSE_MAX) throw new IOException("zu gross");
            }
            return o.toByteArray();
        }
    }

    private static void speichern(File datei, String typ, byte[] roh) {
        try {
            File tmp = new File(datei.getPath() + ".neu");
            try (FileOutputStream o = new FileOutputStream(tmp)) { o.write(roh); }
            File t = new File(datei.getPath() + ".typ");
            try (FileOutputStream o = new FileOutputStream(t)) { o.write(typ.getBytes("UTF-8")); }
            if (!tmp.renameTo(datei)) { datei.delete(); tmp.renameTo(datei); }
        } catch (Exception ignored) { /* Speicher voll o. ae.: dann eben ohne Reserve */ }
    }

    private static WebResourceResponse ausDatei(File datei) {
        try {
            String typ = "application/octet-stream";
            File t = new File(datei.getPath() + ".typ");
            if (t.isFile()) {
                try (FileInputStream i = new FileInputStream(t)) {
                    byte[] b = new byte[(int) Math.min(t.length(), 200)];
                    int n = i.read(b);
                    if (n > 0) typ = new String(b, 0, n, "UTF-8");
                }
            }
            String mime = typ.split(";")[0].trim();
            String zeichensatz = mime.startsWith("text/") || mime.contains("json") || mime.contains("javascript") ? "utf-8" : null;
            Map<String, String> kopf = new HashMap<>();
            kopf.put("Cache-Control", "no-cache");
            return new WebResourceResponse(mime, zeichensatz, 200, "OK", kopf, new FileInputStream(datei));
        } catch (Exception e) {
            return null;
        }
    }

    /** Hoechstens FOTOS_MAX Fotos behalten: die am laengsten nicht gezeigten fliegen zuerst raus. */
    private static void aufraeumen(File dir) {
        File[] fotos = dir.listFiles((d, n) -> n.startsWith("f-") && !n.endsWith(".typ") && !n.endsWith(".neu"));
        if (fotos == null || fotos.length <= FOTOS_MAX) return;
        Arrays.sort(fotos, new Comparator<File>() { @Override public int compare(File a, File b) { return Long.compare(a.lastModified(), b.lastModified()); } });
        for (int i = 0; i < fotos.length - FOTOS_MAX; i++) { fotos[i].delete(); new File(fotos[i].getPath() + ".typ").delete(); }
    }

    private static String hash(String s) throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-1").digest(s.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
