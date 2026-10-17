package com.dirkvoss.frameside.tv;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Ladegrenze: haelt den Akku des Tablets in einem Bereich (z. B. 20 bis 80 %), indem eine schaltbare Steckdose im selben WLAN
 * bei der Obergrenze aus- und bei der Untergrenze wieder eingeschaltet wird. Das schont den Akku bei Dauerbetrieb am Netzteil.
 * Alles laeuft lokal in der App: kein Skript auf der Steckdose, kein Passwort, keine Firewall-Regel, keine Cloud.
 *
 * Sicherung: Die Steckdose bekommt "nach Stromausfall AN" und "nach {@link #SICHERUNG_SEK} Sekunden Aus von selbst wieder AN"
 * (Shelly), damit das Tablet auch bei Ausfall der App oder des WLANs nicht leerlaeuft.
 */
final class Ladegrenze {
    private Ladegrenze() {}

    static final String PREFS = "frameside_ladegrenze";
    static final int SICHERUNG_SEK = 8 * 3600;
    static final int MIN_ABSTAND = 10;                    // Ober- minus Untergrenze: kleiner flattert die Steckdose
    private static final int FEHLER_BIS_NEUSUCHE = 3;

    /** Entscheidung: 1 = einschalten, 0 = ausschalten, -1 = nichts tun. Reine Rechnung (Hysterese). */
    static int entscheide(int akku, int unten, int oben) {
        if (akku < 0 || akku > 100) return -1;
        if (akku >= oben) return 0;
        if (akku <= unten) return 1;
        return -1;
    }

    /** Pruefung der Eingabe; null = in Ordnung, sonst der Hinweis fuer den Benutzer. */
    static String pruefeGrenzen(int unten, int oben) {
        if (unten < 5 || oben > 100) return "Die Grenzen müssen zwischen 5 und 100 % liegen.";
        if (oben - unten < MIN_ABSTAND) return "Zwischen Unter- und Obergrenze brauchen es mindestens " + MIN_ABSTAND + " %, sonst schaltet die Steckdose ständig.";
        return null;
    }

    static final class Konfig {
        boolean aktiv;
        String art = "";                                   // "shelly" oder "url"
        String host = "", id = "", anUrl = "", ausUrl = "";
        int unten = 20, oben = 80;
    }

    static Konfig lesen(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Konfig k = new Konfig();
        k.aktiv = p.getBoolean("aktiv", false);
        k.art = p.getString("art", "");
        k.host = p.getString("host", "");
        k.id = p.getString("id", "");
        k.anUrl = p.getString("anUrl", "");
        k.ausUrl = p.getString("ausUrl", "");
        k.unten = p.getInt("unten", 20);
        k.oben = p.getInt("oben", 80);
        return k;
    }

    static void speichern(Context c, Konfig k) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("aktiv", k.aktiv).putString("art", k.art).putString("host", k.host).putString("id", k.id)
                .putString("anUrl", k.anUrl).putString("ausUrl", k.ausUrl).putInt("unten", k.unten).putInt("oben", k.oben).apply();
    }

    static Steckdose steckdose(Konfig k) {
        if ("url".equals(k.art)) return new Steckdose.Adressen(k.anUrl, k.ausUrl);
        return k.host.isEmpty() ? null : Steckdose.shelly(k.host);
    }

    static int akku(Context c) {
        Intent i = c.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (i == null) return -1;
        int l = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1), s = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return l < 0 || s <= 0 ? -1 : Math.round(l * 100f / s);
    }

    // ------------------------------------------------------------------ Schleife

    private static ScheduledExecutorService pool;
    private static volatile String status = "";           // fuer den Server: "an:20:80", "aus:20:80", "fehler:20:80" oder "suche:20:80"; leer = nicht eingerichtet
    private static volatile String letzteMeldung = "";
    private static int fehler;
    private static boolean abgesichert;

    /** Fuer die Rahmen-Seite (Fully-Schnittstelle): Zustand der Ladegrenze, leer wenn nicht eingerichtet. */
    static String status() { return status; }

    static String letzteMeldung() { return letzteMeldung; }

    static synchronized void starten(Context c) {
        final Context app = c.getApplicationContext();
        stoppen();
        if (!lesen(app).aktiv) { status = ""; return; }
        fehler = 0;
        abgesichert = false;
        pool = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "frameside-ladegrenze"); t.setDaemon(true); return t; });
        pool.scheduleWithFixedDelay(() -> { try { runde(app); } catch (Throwable t) { letzteMeldung = "Fehler: " + t; } }, 3, 60, TimeUnit.SECONDS);
    }

    static synchronized void stoppen() {
        if (pool != null) { pool.shutdownNow(); pool = null; }
    }

    private static void setzeStatus(String zustand, Konfig k) { status = zustand + ":" + k.unten + ":" + k.oben; }

    static void runde(Context c) {
        Konfig k = lesen(c);
        if (!k.aktiv) { status = ""; return; }
        Steckdose s = steckdose(k);
        Boolean an = s == null ? null : s.istAn();
        if (s == null || (an == null && !"url".equals(k.art))) {
            fehler++;
            setzeStatus(fehler >= FEHLER_BIS_NEUSUCHE ? "fehler" : (status.isEmpty() ? "suche" : status.substring(0, status.indexOf(':'))), k);
            letzteMeldung = "Steckdose nicht erreichbar (" + fehler + ")";
            if ("shelly".equals(k.art) && fehler >= FEHLER_BIS_NEUSUCHE && fehler % FEHLER_BIS_NEUSUCHE == 0) neuSuchen(c, k);
            return;
        }
        fehler = 0;
        if (!abgesichert && s.absichern(SICHERUNG_SEK)) abgesichert = true;
        int akku = akku(c);
        int ziel = entscheide(akku, k.unten, k.oben);
        if (ziel >= 0 && (an == null || an != (ziel == 1))) {
            if (s.schalte(ziel == 1)) { an = ziel == 1; letzteMeldung = "Akku " + akku + " %: Steckdose " + (an ? "an" : "aus"); }
            else letzteMeldung = "Schalten fehlgeschlagen";
        } else letzteMeldung = "Akku " + akku + " %, Steckdose " + (an == null ? "?" : an ? "an" : "aus");
        setzeStatus(an != null && !an ? "aus" : "an", k);
    }

    /** Die Adresse der Steckdose hat sich geaendert (DHCP)? Dann im Netz nach derselben Kennung suchen und die neue Adresse merken. */
    private static void neuSuchen(Context c, Konfig k) {
        if (k.id.isEmpty()) return;
        setzeStatus("suche", k);
        List<Steckdose.Fund> funde = Steckdose.suchen(c);
        for (Steckdose.Fund f : funde) {
            if (f.id.equals(k.id)) {
                k.host = f.host;
                speichern(c, k);
                abgesichert = false;
                fehler = 0;
                letzteMeldung = "Steckdose unter " + f.host + " wiedergefunden";
                return;
            }
        }
    }

    /** Ladegrenze abschalten; die Steckdose wird dabei eingeschaltet, damit das Tablet nicht leerlaeuft. */
    static void ausschalten(Context c) {
        Konfig k = lesen(c);
        k.aktiv = false;
        speichern(c, k);
        stoppen();
        status = "";
        try { Steckdose s = steckdose(k); if (s != null) s.schalte(true); } catch (Exception ignored) { }
    }
}
