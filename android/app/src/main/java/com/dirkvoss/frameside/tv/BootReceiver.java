package com.dirkvoss.frameside.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

/** Startet Frameside nach dem Einschalten des Tablets von selbst (nur mit der Freigabe "Ueber anderen Apps einblenden", die Android 10+ dafuer verlangt). */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) return;
        if (Build.VERSION.SDK_INT >= 29 && !Settings.canDrawOverlays(c)) return;
        Intent start = new Intent(c, MainActivity.class);
        start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(start);
    }
}
