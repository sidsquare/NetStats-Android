package com.siddharth.netstats;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class service extends Service
{
    private String TAG = "MyService", date;
    int counter = 0;

    SQLiteDatabase db;
    Handler h = new Handler();

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onDestroy()
    {
        db.close();
        h.removeCallbacksAndMessages(null);
        Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startid)
    {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        boolean boot, app, dnd;
        if (!prefs.contains("start_at_boot"))
            editor.putBoolean("start_at_boot", false);
        if (!prefs.contains("dnd"))
            editor.putBoolean("dnd", false);
        if (!prefs.contains("is_app_open"))
            editor.putBoolean("is_app_open", true);
        editor.commit();

        dnd = prefs.getBoolean("dnd", false);
        boot = prefs.getBoolean("start_at_boot", false);
        app = prefs.getBoolean("is_app_open", true);


        Log.v("Date","lklkl");
        if (boot == true && app == false && dnd == false)
        {
            Intent intents = new Intent(getBaseContext(), MainActivity.class);
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intents);
        }
        if (dnd == true)
        {
            Time now = new Time();
            now.setToNow();
            date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS transfer_week('date' VARCHAR NOT NULL UNIQUE,'down_transfer' integer,'up_transfer' integer);");
            Log.v("Date",date);
            System.gc();
            h.postDelayed(runnable, 1000);
        }
    }

    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            Log.v("run","ning");
            if (counter == 60)
            {
                System.gc();
                counter = 0;
            }
            else
                counter++;
            Time now = new Time();
            now.setToNow();
            String temp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            if (temp.compareTo(date) != 0)
            {
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                Log.v("change","date");
                long rx = TrafficStats.getTotalRxBytes();
                rx = rx / (1024);
                long tx = TrafficStats.getTotalTxBytes();
                tx = tx / (1024);
                long rx1 = prefs.getLong("rx1", 0);
                long tx1 = prefs.getLong("tx1", 0);
                rx1 = rx - rx1;
                tx1 = tx - tx1;
                db.execSQL("update transfer_week set down_transfer=down_transfer+" + rx1 + " , up_transfer=up_transfer+" + tx1 + " where date = '" + date + "';");

                editor.putLong("rx1", rx);
                editor.putLong("tx1", tx);
                editor.commit();
                date = temp;

                System.gc();
                counter = 0;
            }
            h.postDelayed(this, 1000);
        }
    };
}
