package com.siddharth.netstats;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.pow;

public class service extends Service {
    private static String date, temp7, temp8;
    private static int counter = 0, t2, t3;
    private static final Handler h = new Handler();
    private static long speed_rx, speed_tx, speed_rx_mob, speed_tx_mob;
    private static final Time now = new Time();
    private static Cursor c;
    private static long rx, tx, temp_rx, temp_tx, d_offset = 0, u_offset = 0, d_offset_mob = 0, u_offset_mob = 0, rx_mob, tx_mob, temp_rx_mob = 0, temp_tx_mob = 0, d1, d2, d3, d4;
    private static SQLiteDatabase db;
    private static SharedPreferences sharedPref;
    private static Notification notification, n2;
    private static NotificationManager notificationManger, nm2;
    private static Notification.Builder builder, builder1;
    private static SharedPreferences.Editor editor;
    private static boolean mob, first_time = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        try {

            //cutting all links
            Log.v("fsdfsdf", String.valueOf(sharedPref.getLong("d_today", 0)));

            db.close();
            notificationManger.cancel(1);
            h.removeCallbacksAndMessages(null);
        } catch (NullPointerException e) {
        }

    }

    @Override
    public void onStart(Intent intent, int startid) {
        sharedPref = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        boolean boot, app, dnd;
        if (!sharedPref.contains("noti_visible_serv"))
            editor.putBoolean("noti_visible_serv", false);
        if (!sharedPref.contains("start_at_boot"))
            editor.putBoolean("start_at_boot", false);
        if (!sharedPref.contains("dnd"))
            editor.putBoolean("dnd", false);
        if (!sharedPref.contains("is_app_open"))
            editor.putBoolean("is_app_open", true);
        if (!sharedPref.contains("cur_month"))
            editor.putInt("cur_month", 1);
        if (!sharedPref.contains("noti_visible"))
            editor.putBoolean("noti_visible", false);
        if (!sharedPref.contains("noti_visible2"))
            editor.putBoolean("noti_visible2", false);
        if (!sharedPref.contains("not_pers"))
            editor.putBoolean("not_pers", false);
        if (!sharedPref.contains("flimit"))
            editor.putLong("flimit", 0);
        if (!sharedPref.contains("limit"))
            editor.putString("limit", "");


        editor.putBoolean("noti_visible2", false);
        editor.commit();

        dnd = sharedPref.getBoolean("dnd", false);
        boot = sharedPref.getBoolean("start_at_boot", false);
        app = sharedPref.getBoolean("is_app_open", true);


        //only start app if start on boot is enabled
        if (boot && !app && !dnd) {
            Intent intents = new Intent(getBaseContext(), MainActivity.class);
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intents);
        }

        //checking if app has quit and monitoring is required
        if (dnd) {
            Time now = new Time();
            now.setToNow();
            date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            //building the notification
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder = new Notification.Builder(getApplicationContext());
            builder.setContentTitle("NetStats (Service)");
            builder.setContentText("---");
            builder.setSmallIcon(R.drawable.dffd);
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.large);
            builder.setLargeIcon(bm);
            builder.setAutoCancel(true);
            builder.setPriority(Notification.PRIORITY_HIGH);
            Intent intent2 = new Intent(this, notification.class);
            PendingIntent pendintIntent = PendingIntent.getBroadcast(this, 0, intent2, 0);
            builder.setDeleteIntent(pendintIntent);
            if (sharedPref.getBoolean("not_pers", false))
                builder.setOngoing(true);
            builder.setContentIntent(pendingIntent);

            notification = builder.build();

            notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManger.notify(1, notification);
            editor.putBoolean("noti_visible_serv", true);
            //fetching offset values if entry exists

            db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
            db.execSQL("create table if not exists transfer_stats('date' VARCHAR NOT NULL UNIQUE,'down_transfer' integer,'up_transfer' integer,'down_transfer_mob' integer,'up_transfer_mob' integer);");
            date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Cursor c = db.rawQuery("select * from transfer_stats where date=\"" + date + "\";", null);
            if (c.getCount() == 0)
                db.execSQL("insert into transfer_stats values(\"" + date + "\",0,0,0,0);");
            c = db.rawQuery("select down_transfer,up_transfer,down_transfer_mob,up_transfer_mob from transfer_stats where date=\"" + date + "\";", null);
            if (c.getCount() != 0) {
                c.moveToFirst();
                d1 = d_offset = c.getInt(0);
                d2 = u_offset = c.getInt(1);
                d3 = d_offset_mob = c.getInt(2);
                d4 = u_offset_mob = c.getInt(3);
            }

            temp_rx_mob = TrafficStats.getMobileRxBytes();
            temp_tx_mob = TrafficStats.getMobileTxBytes();
            temp_rx = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
            temp_tx = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
            System.gc();
            h.postDelayed(runnable, 1000);
        }
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {

            /*if(sharedPref.getBoolean("purge",false))
            {
                self_destruct();
            }*/
            try {
                //get and set current stats

                rx_mob = TrafficStats.getMobileRxBytes();
                tx_mob = TrafficStats.getMobileTxBytes();
                rx = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
                tx = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();

                //speed calculation
                rx_mob = Math.abs(rx_mob - temp_rx_mob);
                tx_mob = Math.abs(tx_mob - temp_tx_mob);
                rx = Math.abs(rx - temp_rx);
                tx = Math.abs(tx - temp_tx);

                //storing day offset values
                d_offset += rx;
                u_offset += tx;
                d_offset_mob += rx_mob;
                u_offset_mob += tx_mob;

                editor.putLong("d_today", d_offset);
                editor.putLong("u_today", u_offset);
                editor.putLong("d_today_mob", d_offset_mob);
                editor.putLong("u_today_mob", u_offset_mob);
                editor.commit();
                Log.v(String.valueOf(d_offset), String.valueOf(sharedPref.getLong("d_today", 0)));

                //automatic date change
                Time now = new Time();
                now.setToNow();
                String temp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                int t2 = now.hour;
                int t3 = now.month + 1;
                if (temp.compareTo(date) != 0) {
                    date = temp;
                    d_offset = u_offset = 0;
                    d_offset_mob = u_offset_mob = 0;
                    editor.putLong("d_today", 0);
                    editor.putLong("u_today", 0);
                    editor.putLong("d_today_mob", 0);
                    editor.putLong("u_today_mob", 0);
                    editor.commit();
                    db.execSQL("insert into transfer_stats values(\"" + temp + "\",0,0,0,0);");//changing date back to original --- handle sql exception later
                }
                db.execSQL("update transfer_stats set down_transfer_mob=" + d_offset_mob + " , up_transfer_mob=" + u_offset_mob + " , down_transfer=" + d_offset + " , up_transfer=" + u_offset + " where date = '" + date + "';");

                //formatting units for display
                int l = Integer.parseInt(sharedPref.getString("listPref2", "1"));
                String unit2;
                if (l == 1)
                    unit2 = " KBPS";
                else if (l == 2)
                    unit2 = " MBPS";
                else
                    unit2 = " GBPS";
                long divisor2 = (long) pow(1024, l);
                DecimalFormat df2;
                if (l != 1)
                    df2 = new DecimalFormat("0.000");
                else
                    df2 = new DecimalFormat("0");

                d1 += rx;
                d2 += tx;
                d3 += rx_mob;
                d4 += tx_mob;
                //assigning current stat
                if (!mob) {
                    temp7 = df2.format((float) rx / divisor2) + unit2;
                    temp8 = df2.format((float) tx / divisor2) + unit2;

                } else {
                    temp7 = df2.format((float) rx_mob / divisor2) + unit2;
                    temp8 = df2.format((float) tx_mob / divisor2) + unit2;
                }

                temp_rx = temp_rx + rx;
                temp_tx = temp_tx + tx;
                temp_rx_mob = temp_rx_mob + rx_mob;
                temp_tx_mob = temp_tx_mob + tx_mob;

                //limit
                /*if (mob == true || sharedPref.getBoolean("limit_on_wifi", false)) {
                    editor.putLong("flimit", sharedPref.getLong("flimit", 0) + down_speed + up_speed);
                    editor.commit();

                    if (sharedPref.getLong("flimit", 0) >= (Long.parseLong(sharedPref.getString("limit", "0")) * 1024) && !sharedPref.getBoolean("noti_visible2", false)) {
                        builder1 = new Notification.Builder(getApplicationContext());
                        builder1.setContentTitle("Warning");
                        builder1.setContentText("You have reached the Monthly limit");
                        builder1.setSmallIcon(R.drawable.no);
                        builder1.setAutoCancel(true);
                        builder1.setPriority(Notification.PRIORITY_HIGH);
                        builder1.setOngoing(false);
                        n2 = builder1.build();
                        nm2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nm2.notify(2, n2);
                        editor.putBoolean("noti_visible2", true);
                    }
                }*/

                //reseting data used on month change
                if (t3 != sharedPref.getInt("cur_month", 0)) {
                    editor.putLong("flimit", 0);
                    editor.putInt("cur_month", t3);
                    editor.putBoolean("noti_visible2", false);
                    editor.commit();
                    //nm2.cancelAll();
                }

                if (!sharedPref.getBoolean("not_pers", false))
                    builder.setOngoing(false);
                else {
                    builder.setOngoing(true);
                    editor.putBoolean("noti_visible", true);
                    editor.commit();
                }

                if (sharedPref.getBoolean("noti_visible", false)) {
                    builder.setContentText("Down : " + temp7 + "   " + "Up : " + temp8);
                    notificationManger.notify(1, builder.build());
                }

            } catch (Exception n) {
                Log.v("piss off", String.valueOf(n.getMessage()));
            }

            h.postDelayed(this, 1000);
        }
    };

    public boolean wifiEnabled() {
        final ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return wifi.isConnectedOrConnecting();
    }
}