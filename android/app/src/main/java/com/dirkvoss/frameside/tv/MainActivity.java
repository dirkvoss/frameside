package com.dirkvoss.frameside.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Frameside TV: zeigt die Fernseher-Seite von Frameside im Vollbild. Beim ersten Start wird der Server im Heimnetz gesucht. */
public class MainActivity extends Activity {
    private final Handler ui = new Handler(Looper.getMainLooper());
    private FrameLayout wurzel;
    private WebView web;
    private boolean sucheLaeuft;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wurzel = new FrameLayout(this);
        wurzel.setBackgroundColor(Color.parseColor("#0d1117"));
        setContentView(wurzel);
        vollbild();
        uebernehmeIntent(getIntent());
        starten();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        uebernehmeIntent(intent);
        starten();
    }

    /** Per ADB vorgeben:  am start -n com.dirkvoss.frameside.tv/.MainActivity --es adresse 192.168.1.20:8090 */
    private void uebernehmeIntent(Intent i) {
        if (i == null) return;
        String a = i.getStringExtra("adresse");
        if (a != null && !a.trim().isEmpty()) Server.merken(this, Server.normal(a));
    }

    private void vollbild() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) vollbild();
    }

    private void starten() {
        String s = Server.gemerkt(this);
        if (s.isEmpty()) zeigeSuche(); else zeigeSeite(s);
    }

    // ------------------------------------------------------------------ Seite

    private void zeigeSeite(String server) {
        wurzel.removeAllViews();
        if (web == null) {
            web = new WebView(this);
            web.setBackgroundColor(Color.BLACK);
            web.setOnLongClickListener(v -> { menue(); return true; });       // Tablet: langes Druecken auf das Bild oeffnet das Menue
            WebSettings w = web.getSettings();
            w.setJavaScriptEnabled(true);
            w.setDomStorageEnabled(true);
            w.setMediaPlaybackRequiresUserGesture(false);
            w.setCacheMode(WebSettings.LOAD_DEFAULT);
            w.setUseWideViewPort(true);                 // die Seite ist fuer 1920 x 1080 gebaut (meldet sich ueber den Benutzer-Agent "FramesideTV")
            w.setLoadWithOverviewMode(true);
            w.setUserAgentString(w.getUserAgentString() + " FramesideTV/" + version());
            web.setWebChromeClient(new WebChromeClient());
            web.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                    if (r.isForMainFrame()) zeigeFehler();
                }
            });
        }
        if (web.getParent() != null) ((ViewGroup) web.getParent()).removeView(web);
        wurzel.addView(web, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        web.loadUrl(server + "/tv/");
        web.requestFocus();
    }

    private String version() {
        try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionName; } catch (Exception e) { return "1"; }
    }

    private void zeigeFehler() {
        final String s = Server.gemerkt(this);
        ui.post(() -> {
            if (web != null) web.stopLoading();
            LinearLayout l = leer("Keine Verbindung zum Server", s.replace("http://", "") + " antwortet nicht.");
            Button nochmal = knopf("Erneut versuchen");
            nochmal.setOnClickListener(v -> zeigeSeite(s));
            Button aendern = knopf("Anderen Server wählen");
            aendern.setOnClickListener(v -> { Server.merken(this, ""); zeigeSuche(); });
            l.addView(nochmal);
            l.addView(aendern);
            ersetze(l);
            nochmal.requestFocus();
            ui.postDelayed(() -> { if (wurzel.getChildCount() == 1 && wurzel.getChildAt(0) == l) zeigeSeite(s); }, 20000);   // von selbst weiter versuchen
        });
    }

    // ------------------------------------------------------------------ Suche

    private void zeigeSuche() {
        final LinearLayout l = leer("Frameside wird gesucht …", "Dein Server im Heimnetz wird gesucht. Das dauert etwa eine halbe Minute.");
        final TextView status = (TextView) l.getChildAt(1);
        final List<Server.Treffer> treffer = new ArrayList<>();
        final ArrayAdapter<Server.Treffer> adapter = new ArrayAdapter<Server.Treffer>(this, android.R.layout.simple_list_item_1, treffer) {
            @Override public View getView(int p, View v, ViewGroup g) {
                TextView t = (TextView) super.getView(p, v, g);
                t.setTextColor(Color.WHITE); t.setTextSize(22); t.setPadding(24, 20, 24, 20);
                return t;
            }
        };
        final ListView liste = new ListView(this);
        liste.setAdapter(adapter);
        liste.setDivider(null);
        liste.setItemsCanFocus(false);
        liste.setSelector(new android.graphics.drawable.ColorDrawable(Color.parseColor("#33d9a441")));
        liste.setOnItemClickListener((p, v, i, id) -> { Server.merken(this, treffer.get(i).adresse); zeigeSeite(treffer.get(i).adresse); });
        l.addView(liste, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        final Button nochmal = knopf("Erneut suchen");
        nochmal.setOnClickListener(v -> zeigeSuche());
        final Button hand = knopf("Adresse eintippen");
        hand.setOnClickListener(v -> adresseAbfragen());
        l.addView(nochmal);
        l.addView(hand);
        ersetze(l);
        hand.requestFocus();

        sucheLaeuft = true;
        Server.suchen(this, new Server.Rueckruf() {
            @Override public void gefunden(final Server.Treffer t) {
                ui.post(() -> {
                    for (Server.Treffer x : treffer) if (x.adresse.equals(t.adresse)) return;
                    treffer.add(t);
                    adapter.notifyDataSetChanged();
                    status.setText(treffer.size() == 1 ? "Gefunden. Mit OK auswählen." : "Mehrere gefunden. Wähle deinen Server.");
                    ((TextView) l.getChildAt(0)).setText("Frameside gefunden");
                    liste.requestFocus();
                });
            }
            @Override public void fertig() {
                sucheLaeuft = false;
                ui.post(() -> {
                    if (treffer.isEmpty()) {
                        ((TextView) l.getChildAt(0)).setText("Nichts gefunden");
                        status.setText("Läuft Frameside im selben WLAN oder Netzwerk? Du kannst die Adresse auch eintippen, zum Beispiel 192.168.1.20:8090.");
                    } else if (treffer.size() == 1 && Server.gemerkt(MainActivity.this).isEmpty() && wurzel.getChildAt(0) == l) {
                        Server.merken(MainActivity.this, treffer.get(0).adresse);
                        zeigeSeite(treffer.get(0).adresse);
                    }
                });
            }
        });
    }

    private void adresseAbfragen() {
        final EditText e = new EditText(this);
        e.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        e.setHint("192.168.1.20:8090");
        e.setSingleLine(true);
        e.setTextSize(22);
        new AlertDialog.Builder(this).setTitle("Adresse des Servers").setView(e)
                .setPositiveButton("Verbinden", (d, w) -> {
                    String a = Server.normal(e.getText().toString());
                    if (!a.isEmpty()) { Server.merken(this, a); zeigeSeite(a); }
                })
                .setNegativeButton("Abbrechen", null).show();
    }

    // ------------------------------------------------------------------ Menue (Menue-Taste der Fernbedienung)

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS) { menue(); return true; }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { menue(); return true; }
        return super.onKeyLongPress(keyCode, event);
    }

    private void menue() {
        final String[] punkte = {"Seite neu laden", "Anderen Server wählen", "Adresse eintippen", "Beenden", "Start nach dem Einschalten erlauben"};
        new AlertDialog.Builder(this).setTitle("Frameside").setItems(punkte, (d, i) -> {
            if (i == 0 && !Server.gemerkt(this).isEmpty()) zeigeSeite(Server.gemerkt(this));
            else if (i == 1) { Server.merken(this, ""); zeigeSuche(); }
            else if (i == 2) adresseAbfragen();
            else if (i == 3) finish();
            else if (i == 4) autostartErlauben();
        }).show();
    }

    /** Android 10+ erlaubt den Start nach dem Einschalten nur mit "Ueber anderen Apps einblenden" - die Einstellung dafuer oeffnen. */
    private void autostartErlauben() {
        if (android.os.Build.VERSION.SDK_INT >= 23 && !android.provider.Settings.canDrawOverlays(this)) {
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:" + getPackageName())));
            } catch (Exception e) { android.widget.Toast.makeText(this, "Bitte in den Android-Einstellungen für Frameside „Über anderen Apps einblenden“ erlauben.", android.widget.Toast.LENGTH_LONG).show(); }
        } else android.widget.Toast.makeText(this, "Frameside startet jetzt nach dem Einschalten von selbst.", android.widget.Toast.LENGTH_LONG).show();
    }

    // ------------------------------------------------------------------ kleine Hilfen fuer die Oberflaeche

    private LinearLayout leer(String titel, String text) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER_HORIZONTAL);
        l.setPadding(160, 90, 160, 60);
        TextView t = new TextView(this);
        t.setText(titel); t.setTextColor(Color.parseColor("#f4e3b8")); t.setTextSize(40); t.setTypeface(Typeface.SERIF); t.setGravity(Gravity.CENTER);
        TextView s = new TextView(this);
        s.setText(text); s.setTextColor(Color.parseColor("#b6c0d4")); s.setTextSize(20); s.setGravity(Gravity.CENTER); s.setPadding(0, 16, 0, 28);
        l.addView(t);
        l.addView(s);
        return l;
    }

    private Button knopf(String text) {
        Button b = new Button(this);
        b.setText(text); b.setTextSize(20); b.setAllCaps(false); b.setFocusable(true);
        b.setPadding(40, 14, 40, 14);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(700, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = 14;
        b.setLayoutParams(p);
        return b;
    }

    private void ersetze(View v) {
        if (web != null && web.getParent() != null) ((ViewGroup) web.getParent()).removeView(web);
        wurzel.removeAllViews();
        wurzel.addView(v, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (web != null) web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
    }
}
