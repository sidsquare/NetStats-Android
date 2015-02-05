package com.siddharth.netstats;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class service extends Service
{
    private String date;
    static int counter = 0;
    Notification notification;
    NotificationManager notificationManger;
    Notification.Builder builder;
    SQLiteDatabase db;
    Handler h = new Handler();
    static long speed_rx, speed_tx, rx, tx;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onDestroy()
    {
        //cutting all links
        db.close();
        notificationManger.cancel(01);
        h.removeCallbacksAndMessages(null);
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


        Log.v("Date", "lklkl");

        //only start app if start on boot is enabled
        if (boot == true && app == false && dnd == false)
        {
            Intent intents = new Intent(getBaseContext(), MainActivity.class);
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intents);
        }

        //checking if app has quit and monitoring is required
        if (dnd == true)
        {
            Time now = new Time();
            now.setToNow();
            date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS transfer_week('date' VARCHAR NOT NULL UNIQUE,'down_transfer' integer,'up_transfer' integer);");
            Log.v("Date", date);
            speed_rx = TrafficStats.getTotalRxBytes();
            speed_rx = speed_rx / (1024);
            speed_tx = TrafficStats.getTotalTxBytes();
            speed_tx = speed_tx / (1024);

            //building the notification
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            builder = new Notification.Builder(getApplicationContext());
            builder.setContentTitle("NetStats (Service)");
            builder.setContentText("Down : 0 KBPS         " + "Up : 0 KBPS");
            builder.setSmallIcon(R.drawable.no);
            builder.setAutoCancel(true);
            builder.setPriority(0);
            builder.setOngoing(true);
            builder.setContentIntent(pendingIntent);

            notification = builder.build();

            notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManger.notify(01, notification);

            System.gc();
            h.postDelayed(runnable, 1000);
        }
    }

    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            Log.v("run", "ning");

            //getting current stats
            rx = TrafficStats.getTotalRxBytes();
            rx = rx / (1024);
            tx = TrafficStats.getTotalTxBytes();
            tx = tx / (1024);
            rx = rx - speed_rx;
            tx = tx - speed_tx;
            speed_rx=speed_rx+rx;
            speed_tx=speed_tx+tx;

            //checking for gc
            if (counter == 60)
            {
                System.gc();
                counter = 0;
            }
            else
                counter++;

            //checking if date has changed
            Time now = new Time();
            now.setToNow();
            String temp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            if (temp.compareTo(date) != 0)
            {
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                Log.v("change", "date");

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

            //updating the notification
            builder.setContentText("Down : " + rx + " KBPS         " + "Up : " + tx + " KBPS");
            notificationManger.notify(01, builder.build());

            h.postDelayed(this, 1000);
        }
    };
}
