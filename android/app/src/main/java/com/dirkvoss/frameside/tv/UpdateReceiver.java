package com.dirkvoss.frameside.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.widget.Toast;

/** Antwort des Paketinstallers: Android will vor dem Aktualisieren einmal nachfragen (dann den Dialog zeigen); Fehler melden. */
public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        int status = i.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent frage = i.getParcelableExtra(Intent.EXTRA_INTENT);
            if (frage != null) { frage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); c.startActivity(frage); }
        } else if (status != PackageInstaller.STATUS_SUCCESS) {
            String m = i.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            Toast.makeText(c, "Das Update ging nicht" + (m == null ? "" : ": " + m), Toast.LENGTH_LONG).show();
        }
    }
}
