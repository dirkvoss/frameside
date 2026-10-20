package com.dirkvoss.frameside.tv;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Kamera-Ansicht: liest einen QR-Code (die Adresse des Frameside-Servers) und gibt den Text als Ergebnis "text" zurueck. */
@SuppressWarnings("deprecation")          // android.hardware.Camera laeuft auf allen Geraeten bis Android 5 zurueck (auch Fire OS), ohne Zusatzbibliotheken
public class QrScanActivity extends Activity implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {
    private static final int ANFRAGE = 7;
    private Camera kamera;
    private TextureView bild;
    private FrameLayout wurzel;
    private int vorschauB, vorschauH, drehung;
    private final ExecutorService leser = Executors.newSingleThreadExecutor();
    private volatile boolean beschaeftigt, fertig;
    private long letzterVersuch;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wurzel = new FrameLayout(this);
        wurzel.setBackgroundColor(Color.BLACK);
        bild = new TextureView(this);
        bild.setSurfaceTextureListener(this);
        wurzel.addView(bild, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView hinweis = new TextView(this);
        hinweis.setText("Halte die Kamera auf den QR-Code mit der Server-Adresse (in Frameside unter Geräte → Gerät hinzufügen).");
        hinweis.setTextColor(Color.WHITE); hinweis.setTextSize(20); hinweis.setGravity(Gravity.CENTER); hinweis.setBackgroundColor(Color.parseColor("#99000000")); hinweis.setPadding(40, 24, 40, 24);
        wurzel.addView(hinweis, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP));
        Button zu = new Button(this);
        zu.setText("Abbrechen"); zu.setAllCaps(false); zu.setTextSize(20);
        zu.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(600, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        p.bottomMargin = 40;
        wurzel.addView(zu, p);
        setContentView(wurzel);
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.CAMERA}, ANFRAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] rechte, int[] ergebnis) {
        super.onRequestPermissionsResult(code, rechte, ergebnis);
        if (code != ANFRAGE) return;
        if (ergebnis.length > 0 && ergebnis[0] == PackageManager.PERMISSION_GRANTED) { if (bild.isAvailable()) oeffnen(bild.getSurfaceTexture()); }
        else { Toast.makeText(this, "Ohne Kamera-Freigabe kann Frameside den QR-Code nicht lesen. Du kannst die Adresse auch eintippen.", Toast.LENGTH_LONG).show(); finish(); }
    }

    private boolean kameraErlaubt() {
        return Build.VERSION.SDK_INT < 23 || checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void oeffnen(SurfaceTexture st) {
        if (kamera != null || !kameraErlaubt()) return;
        try {
            int wahl = 0;
            Camera.CameraInfo info = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) { wahl = i; break; }
            }
            Camera.getCameraInfo(wahl, info);
            kamera = Camera.open(wahl);
            Camera.Parameters pa = kamera.getParameters();
            Camera.Size beste = null;
            for (Camera.Size s : pa.getSupportedPreviewSizes()) {                    // gross genug zum Lesen, klein genug fuer fluessiges Lesen
                if (s.width > 1280 || s.height > 960) continue;
                if (beste == null || s.width * s.height > beste.width * beste.height) beste = s;
            }
            if (beste != null) pa.setPreviewSize(beste.width, beste.height);
            List<String> fokus = pa.getSupportedFocusModes();
            if (fokus != null && fokus.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) pa.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            kamera.setParameters(pa);
            vorschauB = kamera.getParameters().getPreviewSize().width;
            vorschauH = kamera.getParameters().getPreviewSize().height;
            int grad = 0;
            switch (getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_90: grad = 90; break;
                case Surface.ROTATION_180: grad = 180; break;
                case Surface.ROTATION_270: grad = 270; break;
                default: grad = 0;
            }
            drehung = info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? (360 - (info.orientation + grad) % 360) % 360 : (info.orientation - grad + 360) % 360;
            kamera.setDisplayOrientation(drehung);
            anpassen();
            kamera.setPreviewTexture(st);
            kamera.setPreviewCallback(this);
            kamera.startPreview();
        } catch (Exception e) {
            Toast.makeText(this, "Die Kamera lässt sich nicht öffnen. Du kannst die Adresse auch eintippen.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /** Vorschau im richtigen Seitenverhaeltnis einpassen (statt gestreckt). */
    private void anpassen() {
        final int b = (drehung % 180 == 0) ? vorschauB : vorschauH, h = (drehung % 180 == 0) ? vorschauH : vorschauB;
        wurzel.post(() -> {
            int w = wurzel.getWidth(), hh = wurzel.getHeight();
            if (w == 0 || hh == 0 || b == 0 || h == 0) return;
            float faktor = Math.min((float) w / b, (float) hh / h);
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(Math.round(b * faktor), Math.round(h * faktor), Gravity.CENTER);
            bild.setLayoutParams(p);
        });
    }

    @Override
    public void onPreviewFrame(final byte[] daten, Camera c) {
        long jetzt = System.currentTimeMillis();
        if (fertig || beschaeftigt || jetzt - letzterVersuch < 150) return;
        letzterVersuch = jetzt;
        beschaeftigt = true;
        final int b = vorschauB, h = vorschauH;
        final byte[] kopie = daten.clone();
        leser.execute(() -> {
            try {
                String text = lesen(kopie, b, h);
                if (text != null && !fertig) {
                    fertig = true;
                    runOnUiThread(() -> { setResult(RESULT_OK, new Intent().putExtra("text", text)); finish(); });
                }
            } catch (Exception ignored) {
                // kein Code im Bild: weiter versuchen
            } finally {
                beschaeftigt = false;
            }
        });
    }

    /** Der QR-Code in einem Kamerabild (NV21), oder null. */
    static String lesen(byte[] nv21, int breite, int hoehe) throws Exception {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        try {
            return new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(new PlanarYUVLuminanceSource(nv21, breite, hoehe, 0, 0, breite, hoehe, false))), hints).getText();
        } catch (com.google.zxing.ReaderException e) {
            return null;
        }
    }

    @Override public void onSurfaceTextureAvailable(SurfaceTexture st, int w, int h) { oeffnen(st); }
    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture st, int w, int h) { }
    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture st) { schliessen(); return true; }
    @Override public void onSurfaceTextureUpdated(SurfaceTexture st) { }

    private void schliessen() {
        if (kamera != null) {
            try { kamera.setPreviewCallback(null); kamera.stopPreview(); } catch (Exception ignored) { }
            try { kamera.release(); } catch (Exception ignored) { }
            kamera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        schliessen();
        if (isFinishing()) leser.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bild != null && bild.isAvailable() && kamera == null) oeffnen(bild.getSurfaceTexture());
    }
}
