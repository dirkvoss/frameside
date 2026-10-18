package com.dirkvoss.frameside.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

/**
 * Aktualisiert die App selbst: fragt den Frameside-Server nach der aktuellen App-Version und laedt bei Bedarf die neue Datei von GitHub.
 * Installiert wird mit dem Android-Paketinstaller: Android nimmt nur eine Datei an, die mit demselben Schluessel signiert ist wie die vorhandene App,
 * und fragt einmal nach ("Aktualisieren?"). Ganz ohne Beruehren geht das auf einem normalen Tablet nicht.
 */
final class AppUpdate {
    private AppUpdate() {}

    static final String URL = "https://github.com/dirkvoss/frameside/releases/latest/download/Frameside-TV.apk";
    /** Zuletzt gefundene neuere Version ("" = keine oder noch nicht geprueft); fuer den Eintrag im Menue. */
    static volatile String neuere = "";

    interface Rueckruf { void fertig(String neuereVersion); }

    /** "1.10.0" > "1.9.2": Zahl fuer Zahl vergleichen; Unsinn zaehlt als gleich. */
    static boolean istNeuer(String aktuell, String installiert) {
        try {
            String[] a = aktuell.trim().split("\\."), b = installiert.trim().split("\\.");
            for (int i = 0; i < Math.max(a.length, b.length); i++) {
                int x = i < a.length ? Integer.parseInt(a[i]) : 0, y = i < b.length ? Integer.parseInt(b[i]) : 0;
                if (x != y) return x > y;
            }
        } catch (Exception e) { /* keine Zahl: nichts melden */ }
        return false;
    }

    static String installiert(Context c) {
        try { return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName; } catch (Exception e) { return "0"; }
    }

    /** Fragt im Hintergrund den Server nach der aktuellen App-Version; meldet die neuere Version oder "" (im Hintergrund-Thread). */
    static void pruefen(final Context c, final Rueckruf cb) {
        final String server = Server.gemerkt(c);
        if (server.isEmpty()) return;
        new Thread(() -> {
            String neu = "";
            try {
                HttpURLConnection h = (HttpURLConnection) new URL(server + "/api/config").openConnection();
                h.setConnectTimeout(5000);
                h.setReadTimeout(5000);
                try (InputStream in = h.getInputStream()) {
                    java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[4096]; int n;
                    while ((n = in.read(buf)) > 0) o.write(buf, 0, n);
                    String aktuell = new JSONObject(o.toString("UTF-8")).optString("app_aktuell", "");
                    if (!aktuell.isEmpty() && istNeuer(aktuell, installiert(c))) neu = aktuell;
                }
            } catch (Exception e) { /* Server nicht erreichbar: kein Update-Hinweis */ }
            neuere = neu;
            if (cb != null) cb.fertig(neu);
        }, "frameside-update-pruefen").start();
    }

    /** Laedt die neue App und startet die Installation. aus_der_ferne: vom Handy ausgeloest (kein Dialog, nur Hinweise als Toast). */
    static void starten(final Activity a, final boolean ausDerFerne) {
        if (Build.VERSION.SDK_INT >= 26 && !a.getPackageManager().canRequestPackageInstalls()) {
            if (ausDerFerne) Toast.makeText(a, "Frameside darf noch keine Apps installieren. Bitte einmal erlauben.", Toast.LENGTH_LONG).show();
            new AlertDialog.Builder(a).setTitle("Einmal erlauben")
                    .setMessage("Damit sich Frameside selbst aktualisieren kann, muss es einmalig „Apps installieren“ dürfen. Auf der nächsten Seite den Schalter einschalten und mit Zurück hierher kommen.")
                    .setPositiveButton("Weiter", (d, w) -> a.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + a.getPackageName()))))
                    .setNegativeButton("Abbrechen", null).show();
            return;
        }
        final AlertDialog warten = new AlertDialog.Builder(a).setTitle("Update").setMessage("Die neue Frameside-App wird geladen …").setCancelable(false).show();
        new Thread(() -> {
            String fehler = null;
            try {
                installieren(a);
            } catch (Exception e) {
                fehler = "Das Laden ging nicht (" + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()) + "). Ist das Tablet im Internet?";
            }
            final String f = fehler;
            a.runOnUiThread(() -> {
                warten.dismiss();
                if (f != null) Toast.makeText(a, f, Toast.LENGTH_LONG).show();
            });
        }, "frameside-update").start();
    }

    private static void installieren(Context c) throws Exception {
        PackageInstaller pi = c.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams p = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        p.setAppPackageName(c.getPackageName());
        int id = pi.createSession(p);
        PackageInstaller.Session s = pi.openSession(id);
        try {
            HttpURLConnection h = (HttpURLConnection) new URL(URL).openConnection();
            h.setConnectTimeout(10000);
            h.setReadTimeout(30000);
            h.setInstanceFollowRedirects(true);
            if (h.getResponseCode() != 200) throw new Exception("HTTP " + h.getResponseCode());
            long laenge = h.getContentLengthLong();
            try (InputStream in = h.getInputStream(); OutputStream out = s.openWrite("Frameside-TV.apk", 0, laenge > 0 ? laenge : -1)) {
                byte[] buf = new byte[32 * 1024]; int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                s.fsync(out);
            }
            Intent i = new Intent(c, UpdateReceiver.class).setPackage(c.getPackageName());
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0);
            s.commit(PendingIntent.getBroadcast(c, id, i, flags).getIntentSender());
            s = null;
        } finally {
            if (s != null) { try { s.abandon(); } catch (Exception e) { /* egal */ } }
        }
    }
}
