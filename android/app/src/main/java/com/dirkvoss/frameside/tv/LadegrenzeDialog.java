package com.dirkvoss.frameside.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/** Einstellung der Ladegrenze (Menue "Ladegrenze"): Steckdose suchen oder Adressen eintippen, Bereich waehlen, pruefen, speichern. */
final class LadegrenzeDialog {
    private LadegrenzeDialog() {}

    static void zeigen(final Activity a) {
        final Ladegrenze.Konfig k = Ladegrenze.lesen(a);
        final List<String> punkte = new ArrayList<>();
        punkte.add("Shelly-Steckdose suchen");
        punkte.add("Web-Adressen für Ein und Aus eintippen");
        if (k.aktiv) { punkte.add("Grenzen ändern"); punkte.add("Ladegrenze ausschalten"); }
        String kopf = !k.aktiv ? "Die Ladegrenze ist aus. Mit einer schaltbaren Steckdose im selben WLAN hält Frameside den Akku in einem Bereich (z. B. 20 bis 80 %) und schont ihn so."
                : "Aktiv: " + k.unten + " bis " + k.oben + " %\n" + (Ladegrenze.letzteMeldung().isEmpty() ? "wird gestartet …" : Ladegrenze.letzteMeldung());
        new AlertDialog.Builder(a).setTitle("Ladegrenze").setMessage(kopf).setItems(punkte.toArray(new String[0]), (d, i) -> {
            String p = punkte.get(i);
            if (p.startsWith("Shelly")) suchen(a, k);
            else if (p.startsWith("Web-Adressen")) adressen(a, k);
            else if (p.startsWith("Grenzen")) grenzen(a, k);
            else { Ladegrenze.ausschalten(a); Toast.makeText(a, "Ladegrenze aus. Die Steckdose bleibt eingeschaltet.", Toast.LENGTH_LONG).show(); }
        }).setNegativeButton("Schließen", null).show();
    }

    private static void suchen(final Activity a, final Ladegrenze.Konfig k) {
        final AlertDialog warten = new AlertDialog.Builder(a).setTitle("Suche …").setMessage("Shelly-Steckdosen im WLAN werden gesucht. Das dauert etwa eine halbe Minute.").setCancelable(false).show();
        new Thread(() -> {
            final List<Steckdose.Fund> funde = Steckdose.suchen(a);
            a.runOnUiThread(() -> {
                warten.dismiss();
                final List<String> namen = new ArrayList<>();
                for (Steckdose.Fund f : funde) namen.add(f.toString());
                namen.add("Adresse eintippen");
                new AlertDialog.Builder(a).setTitle(funde.isEmpty() ? "Keine Shelly-Steckdose gefunden" : "Welche Steckdose lädt das Tablet?")
                        .setItems(namen.toArray(new String[0]), (d, i) -> {
                            if (i == namen.size() - 1) { hand(a, k); return; }
                            k.art = "shelly"; k.host = funde.get(i).host; k.id = funde.get(i).id;
                            grenzen(a, k);
                        }).setNegativeButton("Abbrechen", null).show();
            });
        }, "frameside-steckdose-suche").start();
    }

    private static void hand(final Activity a, final Ladegrenze.Konfig k) {
        final EditText e = feld(a, "z. B. 192.168.1.50", InputType.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(a).setTitle("Adresse der Shelly-Steckdose").setView(e).setPositiveButton("Weiter", (d, w) -> {
            final String host = e.getText().toString().trim().replace("http://", "").replaceAll("/.*$", "");
            new Thread(() -> {
                final Steckdose.Fund f = host.isEmpty() ? null : Steckdose.shellyPruefen(host, 2500);
                a.runOnUiThread(() -> {
                    if (f == null) { Toast.makeText(a, "Unter dieser Adresse antwortet kein Shelly.", Toast.LENGTH_LONG).show(); return; }
                    k.art = "shelly"; k.host = f.host; k.id = f.id;
                    grenzen(a, k);
                });
            }, "frameside-steckdose-pruefen").start();
        }).setNegativeButton("Abbrechen", null).show();
    }

    private static void adressen(final Activity a, final Ladegrenze.Konfig k) {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(40, 20, 40, 0);
        final EditText an = feld(a, "Adresse zum Einschalten, z. B. http://192.168.1.60/cm?cmnd=Power%20On", InputType.TYPE_TEXT_VARIATION_URI);
        final EditText aus = feld(a, "Adresse zum Ausschalten", InputType.TYPE_TEXT_VARIATION_URI);
        an.setText(k.anUrl); aus.setText(k.ausUrl);
        l.addView(an); l.addView(aus);
        new AlertDialog.Builder(a).setTitle("Steckdose über Web-Adressen").setView(l).setPositiveButton("Weiter", (d, w) -> {
            String u1 = an.getText().toString().trim(), u2 = aus.getText().toString().trim();
            if (!u1.startsWith("http") || !u2.startsWith("http")) { Toast.makeText(a, "Beide Adressen müssen mit http:// beginnen.", Toast.LENGTH_LONG).show(); return; }
            k.art = "url"; k.anUrl = u1; k.ausUrl = u2; k.host = ""; k.id = "";
            grenzen(a, k);
        }).setNegativeButton("Abbrechen", null).show();
    }

    private static void grenzen(final Activity a, final Ladegrenze.Konfig k) {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(40, 20, 40, 0);
        TextView t = new TextView(a);
        t.setText("Bei der Obergrenze wird die Steckdose ausgeschaltet, bei der Untergrenze wieder eingeschaltet. Üblich und schonend: 20 bis 80 %.");
        t.setTextSize(16);
        final EditText unten = feld(a, "Untergrenze in %", InputType.TYPE_CLASS_NUMBER), oben = feld(a, "Obergrenze in %", InputType.TYPE_CLASS_NUMBER);
        unten.setText(String.valueOf(k.unten)); oben.setText(String.valueOf(k.oben));
        l.addView(t); l.addView(unten); l.addView(oben);
        new AlertDialog.Builder(a).setTitle("Akku-Bereich").setView(l).setPositiveButton("Prüfen und speichern", (d, w) -> {
            int u, o;
            try { u = Integer.parseInt(unten.getText().toString().trim()); o = Integer.parseInt(oben.getText().toString().trim()); }
            catch (NumberFormatException e) { Toast.makeText(a, "Bitte zwei Zahlen eingeben.", Toast.LENGTH_LONG).show(); return; }
            String fehler = Ladegrenze.pruefeGrenzen(u, o);
            if (fehler != null) { Toast.makeText(a, fehler, Toast.LENGTH_LONG).show(); return; }
            k.unten = u; k.oben = o;
            pruefenUndSpeichern(a, k);
        }).setNegativeButton("Abbrechen", null).show();
    }

    private static void pruefenUndSpeichern(final Activity a, final Ladegrenze.Konfig k) {
        Toast.makeText(a, "Verbinde mit der Steckdose …", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            final Steckdose s = Ladegrenze.steckdose(k);
            final String ergebnis;
            boolean ok = false;
            if (s == null) ergebnis = "Die Steckdose antwortet nicht. Ist sie im selben WLAN eingeschaltet?";
            else {
                Boolean an = s.istAn();
                if (an == null && !"url".equals(k.art)) ergebnis = "Die Steckdose antwortet nicht richtig.";
                else if ("url".equals(k.art) && !s.schalte(true)) ergebnis = "Die Einschalt-Adresse antwortet nicht.";
                else {
                    boolean gesichert = s.absichern(Ladegrenze.SICHERUNG_SEK);
                    k.aktiv = true;
                    Ladegrenze.speichern(a, k);
                    Ladegrenze.starten(a);
                    ok = true;
                    ergebnis = "Ladegrenze " + k.unten + " bis " + k.oben + " % ist aktiv." + (gesichert ? "" : " (Sicherung in der Steckdose ließ sich nicht setzen.)");
                }
            }
            final boolean fertig = ok;
            a.runOnUiThread(() -> Toast.makeText(a, ergebnis, fertig ? Toast.LENGTH_LONG : Toast.LENGTH_LONG).show());
        }, "frameside-steckdose-test").start();
    }

    private static EditText feld(Activity a, String hinweis, int typ) {
        EditText e = new EditText(a);
        e.setInputType(typ);
        e.setHint(hinweis);
        e.setSingleLine(true);
        e.setTextSize(18);
        return e;
    }
}
