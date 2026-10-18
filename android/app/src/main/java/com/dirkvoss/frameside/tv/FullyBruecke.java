package com.dirkvoss.frameside.tv;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.provider.Settings;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;

/**
 * Stellt der Rahmen-Seite dieselben fuenf Funktionen bereit wie die JavaScript-Schnittstelle von Fully Kiosk ("window.fully"):
 * Akkustand, Ladezustand, Helligkeit (lesen/setzen), Bildschirm einschalten und Anzeige neu starten. Damit laufen Nachtruhe,
 * Akku-Anzeige und Fernbefehle in der App ohne Fully und ohne eine Zeile Anpassung der Seite.
 */
class FullyBruecke {
    private final MainActivity a;
    private volatile int helligkeit = -1;                      // zuletzt gesetzter Wert (0-255); -1 = noch nie gesetzt

    FullyBruecke(MainActivity a) { this.a = a; }

    private Intent akku() {
        return a.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @JavascriptInterface
    public int getBatteryLevel() {
        Intent i = akku();
        if (i == null) return -1;
        int l = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1), s = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return l < 0 || s <= 0 ? -1 : Math.round(l * 100f / s);
    }

    @JavascriptInterface
    public boolean isPlugged() {
        Intent i = akku();
        return i != null && i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
    }

    @JavascriptInterface
    public int getScreenBrightness() {
        if (helligkeit >= 0) return helligkeit;
        try { return Settings.System.getInt(a.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS); } catch (Exception e) { return 160; }
    }

    @JavascriptInterface
    public void setScreenBrightness(final int wert) {
        helligkeit = Math.max(0, Math.min(255, wert));
        a.runOnUiThread(() -> {
            WindowManager.LayoutParams lp = a.getWindow().getAttributes();
            lp.screenBrightness = helligkeit <= 0 ? 0.01f : helligkeit / 255f;       // 0 waere bei manchen Geraeten "Hintergrundlicht aus"
            a.getWindow().setAttributes(lp);
        });
    }

    @JavascriptInterface
    public void turnScreenOn() {
        a.runOnUiThread(() -> {
            if (android.os.Build.VERSION.SDK_INT >= 27) a.setTurnScreenOn(true);
            a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        });
    }

    /** Zustand der Ladegrenze fuer den Server ("an:20:80", "aus:20:80", "fehler:20:80", "suche:20:80"); leer, wenn nicht eingerichtet. */
    @JavascriptInterface
    public String getLadegrenze() { return Ladegrenze.status(); }

    @JavascriptInterface
    public void restartApp() { a.neuStarten(); }

    /** Befehl vom Handy ("App jetzt aktualisieren"): laedt die neue App und startet die Installation. */
    @JavascriptInterface
    public void updateApp() { a.runOnUiThread(() -> AppUpdate.starten(a, true)); }
}
