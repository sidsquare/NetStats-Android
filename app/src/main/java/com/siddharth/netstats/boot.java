package com.siddharth.netstats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class boot extends BroadcastReceiver
{
    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0,service.class);
        arg0.startService(intent);
    }
}