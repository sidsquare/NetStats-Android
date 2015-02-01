package com.siddharth.netstats;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Siddharth on 01-02-2015.
 */
public class service extends Service
{
    private static final String TAG = "MyService";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onDestroy() {
        Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startid)
    {
        boolean temp;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        temp=preferences.getBoolean("start_at_boot",false);
        if(temp==true)
        {
            Intent intents = new Intent(getBaseContext(), MainActivity.class);
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intents);
            Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onStart");
        }
    }
}
