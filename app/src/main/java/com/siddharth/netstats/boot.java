package com.siddharth.netstats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class boot extends BroadcastReceiver {
    public void onReceive(Context arg0, Intent arg1) {
        if (Intent.ACTION_SHUTDOWN.equalsIgnoreCase(arg1.getAction())) {
            Log.i("Boot Receiver ", "- Shutdown event");
            Intent intent = new Intent(arg0, ShutdownService.class);
            arg0.startService(intent);
        }

        if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(arg1.getAction())) {
            Log.i("Boot Receiver ", "- Boot up event");
            Intent intent = new Intent(arg0, service.class);
            arg0.startService(intent);
        }
    }
}