package com.dirkvoss.frameside.tv;

import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.view.Gravity;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Bildschirmschoner: zeigt die Fernseher-Seite des gemerkten Frameside-Servers (dasselbe Geraet wie in der App, gleiche Kopplung).
 * Selbstheilend wie die App: Ist der Server oder das Netz weg, bleibt der Bildschirm schwarz (keine Fehlerseite) und es wird alle
 * 20 Sekunden bzw. sofort bei Rueckkehr des Netzes neu geladen; stuerzt die Anzeige ab, wird sie neu aufgebaut.
 */
public class FramesideDream extends DreamService {
    private static final long WIEDERHOLUNG_MS = 20000;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private FrameLayout wurzel;
    private WebView web;
    private String server = "";
    private volatile boolean fehler = false;
    private ConnectivityManager.NetworkCallback netz;
    private final Runnable neuLaden = this::laden;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);
        wurzel = new FrameLayout(this);
        wurzel.setBackgroundColor(Color.BLACK);
        server = Server.gemerkt(this);
        if (server.isEmpty()) {
            TextView t = new TextView(this);
            t.setText("Öffne zuerst die App „Frameside“, damit der Bildschirmschoner den Server kennt.");
            t.setTextColor(Color.WHITE); t.setTextSize(26); t.setGravity(Gravity.CENTER);
            wurzel.addView(t, new FrameLayout.LayoutParams(-1, -1));
        } else {
            laden();
            netzBeobachten();
        }
        setContentView(wurzel);
    }

    private void bauen() {
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        WebSettings w = web.getSettings();
        w.setJavaScriptEnabled(true);
        w.setDomStorageEnabled(true);
        w.setMediaPlaybackRequiresUserGesture(false);
        w.setUseWideViewPort(true);
        w.setLoadWithOverviewMode(true);
        w.setUserAgentString(w.getUserAgentString() + " FramesideTV/dream");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {       // Foto-Reserve: bei Ausfall von Netz oder Server weiter schon gesehene Fotos zeigen
                return Reserve.holen(FramesideDream.this, server, r);
            }

            @Override
            public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) serverWeg();
            }

            @Override
            public boolean onRenderProcessGone(WebView v, RenderProcessGoneDetail d) {      // Absturz/Speichermangel der Anzeige: neu aufbauen statt schwarz stehenbleiben
                try { wurzel.removeAllViews(); v.destroy(); } catch (Exception e) { /* egal */ }
                web = null;
                ui.removeCallbacks(neuLaden);
                ui.postDelayed(neuLaden, 1500);
                return true;
            }
        });
        wurzel.addView(web, new FrameLayout.LayoutParams(-1, -1));
    }

    private void laden() {
        ui.removeCallbacks(neuLaden);
        fehler = false;
        if (web == null) bauen();
        web.loadUrl(server + "/tv/");
    }

    /** Server nicht erreichbar: schwarz zeigen (statt der Browser-Fehlerseite) und spaeter erneut versuchen. */
    private void serverWeg() {
        fehler = true;
        if (web != null) { web.stopLoading(); web.loadUrl("about:blank"); }
        ui.removeCallbacks(neuLaden);
        ui.postDelayed(neuLaden, WIEDERHOLUNG_MS);
    }

    /** Kommt das Netz zurueck, waehrend der Server fehlt, sofort neu laden. */
    private void netzBeobachten() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            netz = new ConnectivityManager.NetworkCallback() {
                @Override public void onAvailable(Network n) { if (fehler) ui.post(neuLaden); }
            };
            cm.registerNetworkCallback(new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), netz);
        } catch (Exception e) { netz = null; /* ohne Netzmeldung bleibt der 20-Sekunden-Versuch */ }
    }

    @Override
    public void onDetachedFromWindow() {
        ui.removeCallbacksAndMessages(null);
        if (netz != null) {
            try { ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).unregisterNetworkCallback(netz); } catch (Exception e) { /* egal */ }
            netz = null;
        }
        if (web != null) { web.stopLoading(); web.destroy(); web = null; }
        super.onDetachedFromWindow();
    }
}
