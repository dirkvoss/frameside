package com.dirkvoss.frameside.tv;

import android.graphics.Color;
import android.service.dreams.DreamService;
import android.view.Gravity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Bildschirmschoner: zeigt die Fernseher-Seite des gemerkten Frameside-Servers (dasselbe Geraet wie in der App, gleiche Kopplung). */
public class FramesideDream extends DreamService {
    private WebView web;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);
        FrameLayout wurzel = new FrameLayout(this);
        wurzel.setBackgroundColor(Color.BLACK);
        String server = Server.gemerkt(this);
        if (server.isEmpty()) {
            TextView t = new TextView(this);
            t.setText("Öffne zuerst die App „Frameside“, damit der Bildschirmschoner den Server kennt.");
            t.setTextColor(Color.WHITE); t.setTextSize(26); t.setGravity(Gravity.CENTER);
            wurzel.addView(t, new FrameLayout.LayoutParams(-1, -1));
        } else {
            web = new WebView(this);
            web.setBackgroundColor(Color.BLACK);
            WebSettings w = web.getSettings();
            w.setJavaScriptEnabled(true);
            w.setDomStorageEnabled(true);
            w.setMediaPlaybackRequiresUserGesture(false);
            w.setUseWideViewPort(true);
            w.setLoadWithOverviewMode(true);
            w.setUserAgentString(w.getUserAgentString() + " FramesideTV/dream");
            web.setWebViewClient(new WebViewClient());
            web.loadUrl(server + "/tv/");
            wurzel.addView(web, new FrameLayout.LayoutParams(-1, -1));
        }
        setContentView(wurzel);
    }

    @Override
    public void onDetachedFromWindow() {
        if (web != null) { web.stopLoading(); web.destroy(); web = null; }
        super.onDetachedFromWindow();
    }
}
