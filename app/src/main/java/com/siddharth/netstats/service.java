package com.siddharth.netstats;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

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
        boolean boot,app;

        Context ctx = getApplicationContext();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        //String g= String.valueOf(prefs.contains("start_at_boot"));
        //Toast.makeText(this, "ss "+ g, Toast.LENGTH_LONG).show();

        SharedPreferences.Editor editor = prefs.edit();
        if(!prefs.contains("start_at_boot"))
            editor.putBoolean("start_at_boot",false);
        if(!prefs.contains("is_app_open"))
            editor.putBoolean("is_app_open",true);
        editor.commit();

        boot=prefs.getBoolean("start_at_boot",false);
        app=prefs.getBoolean("is_app_open",true);
        //g= String.valueOf(boot);
        //Toast.makeText(this,  "ee "+g, Toast.LENGTH_LONG).show();
        if(boot==true && app==false)
        {
            Intent intents = new Intent(getBaseContext(), MainActivity.class);
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intents);
            Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onStart");
        }
    }
}
