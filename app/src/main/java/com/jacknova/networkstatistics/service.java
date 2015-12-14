package com.jacknova.networkstatistics;


import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.pow;

public class service extends Service {
    private static String line;
    private static String unit2;
    private final Handler handler = new Handler();
    private static long divisor2;
    private static long d1;
    private static long d2;
    private static long d3;
    private static long d4;
    private static long RX;
    private static long TX;
    private static long RX2;
    private static long TX2;
    private static long tempRX;
    private static long tempTX;
    private static long offsetRX = 0;
    private static long offsetTX = 0;
    private static long offsetRXMob = 0;
    private static long offsetTXMob = 0;
    private static long RXMob;
    private static long TXMob;
    private static long RXMob2;
    private static long TXMob2;
    private static long tempRXMob = 0;
    private static long tempTXMob = 0;
    private static DecimalFormat df2;
    private static BufferedReader reader;
    private static final File procFile = new File("/proc/net/xt_qtaguid/iface_stat_fmt");
    private static String[] values;
    private static int timer = 0;
    private static String date;
    private static final Handler h = new Handler();
    private static Time now = new Time();
    private static Cursor c;
    private static SQLiteDatabase db;
    private static SharedPreferences sharedPref;
    private static Notification notification, n2;
    private static NotificationManager notificationManger, nm2;
    private static Notification.Builder builder, builder1;
    private static SharedPreferences.Editor editor;

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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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

            /*
           notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManger.notify(1, notification);
            editor.putBoolean("noti_visible", true);
            editor.commit();*/

            //building the notification
            Intent notificationIntent = new Intent(this,MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder = new Notification.Builder(this);
            builder.setDeleteIntent(pendingIntent);
            builder.setContentTitle("Network Statistics Service");
            builder.setContentText("---");
            builder.setSmallIcon(R.drawable.dffd);
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.large);
            builder.setLargeIcon(bm);
            builder.setAutoCancel(true);
            builder.setPriority(Notification.PRIORITY_HIGH);
            if (sharedPref.getBoolean("not_pers", false))
                builder.setOngoing(true);
            builder.setContentIntent(pendingIntent);

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
            c = db.rawQuery("select * from transfer_stats where date=\"" + date + "\";", null);
            if (c.getCount() == 0)
                db.execSQL("insert into transfer_stats values(\"" + date + "\",0,0,0,0);");
            c = db.rawQuery("select down_transfer,up_transfer,down_transfer_mob,up_transfer_mob from transfer_stats where date=\"" + date + "\";", null);
            if (c.getCount() != 0) {
                Log.v(String.valueOf(offsetRX), String.valueOf(sharedPref.getLong("d_today", 0)));

                c.moveToFirst();
                offsetRX = c.getInt(0);
                offsetTX = c.getInt(1);
                offsetRXMob = c.getInt(2);
                offsetTXMob = c.getInt(3);
                Log.v(String.valueOf(offsetRX), String.valueOf(sharedPref.getLong("d_today", 0)));

            }
            c.close();
            //getting current stats from proc file
            try {
                reader = new BufferedReader(new FileReader(procFile));
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    values = line.split(" ");
                    if (values[0].equals("rmnet0")) {
                        Log.v("values are --> " + values[1], values[3]);
                        tempRXMob = Long.parseLong(values[1]);
                        tempTXMob = Long.parseLong(values[3]);
                    } else if (values[0].equals("wlan0")) {
                        Log.v("values are --> " + values[1], values[3]);
                        tempRX = Long.parseLong(values[1]);
                        tempTX = Long.parseLong(values[3]);
                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                Log.v("FileNotFoundException", "iface_stat_fmt not found");
                e.printStackTrace();
            } catch (IOException e) {
                Log.v("IOException", "iface_stat_fmt not opened");
                e.printStackTrace();
            }
            formatUnits();
            System.gc();
            h.postDelayed(runnable, 1000);
        }
    }

    private final Runnable runnable = new Runnable() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {

            try {
                try {
                    reader = new BufferedReader(new FileReader(procFile));
                    reader.readLine();
                    while ((line = reader.readLine()) != null) {
                        values = line.split(" ");
                        if (values[0].equals("rmnet0")) {
                            Log.v("values are --> " + values[1], values[3]);
                            RXMob = Long.parseLong(values[1]);
                            TXMob = Long.parseLong(values[3]);
                        } else if (values[0].equals("wlan0")) {
                            Log.v("values are --> " + values[1], values[3]);
                            RX = Long.parseLong(values[1]);
                            TX = Long.parseLong(values[3]);
                        }
                    }
                    reader.close();
                } catch (FileNotFoundException e) {
                    Log.v("FileNotFoundException", "iface_stat_fmt not found");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.v("IOException", "iface_stat_fmt not opened");
                    e.printStackTrace();
                }
                if (wifiEnabled()) {

                    RX2 = RX - tempRX;
                    TX2 = TX - tempTX;

                    offsetRX += RX2;
                    offsetTX += TX2;

                    editor.putLong("d_today", offsetRX);
                    editor.putLong("u_today", offsetTX);

                    d1 = d1 + RX2;
                    d2 = d2 + TX2;

                    tempRX = tempRX + RX2;
                    tempTX = tempTX + TX2;
                } else {

                    //speed calculation
                    RXMob2 = RXMob - tempRXMob;
                    TXMob2 = TXMob - tempTXMob;


                    //storing day offset values
                    offsetRXMob += RXMob2;
                    offsetTXMob += TXMob2;

                    editor.putLong("d_today_mob", offsetRXMob);
                    editor.putLong("u_today_mob", offsetTXMob);

                    d3 = d3 + RXMob2;
                    d4 = d4 + TXMob2;

                    tempRXMob = tempRXMob + RXMob2;
                    tempTXMob = tempTXMob + TXMob2;

                }
                editor.commit();

                //automatic date change
                now = new Time();
                now.setToNow();
                String temp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                //int t2 = now.hour;
                int t3 = now.month + 1;
                if (temp.compareTo(date) != 0) {
                    date = temp;
                    offsetRX = offsetTX = 0;
                    offsetRXMob = offsetTXMob = 0;
                    editor.putLong("d_today", 0);
                    editor.putLong("u_today", 0);
                    editor.putLong("d_today_mob", 0);
                    editor.putLong("u_today_mob", 0);
                    editor.commit();
                    db.execSQL("insert into transfer_stats values(\"" + temp + "\",0,0,0,0);");//changing date back to original --- handle sql exception later
                }
                db.execSQL("update transfer_stats set down_transfer_mob=" + offsetRXMob + " , up_transfer_mob=" + offsetTXMob + " , down_transfer=" + offsetRX + " , up_transfer=" + offsetTX + " where date = '" + date + "';");

                //limit
                if (!(wifiEnabled()) || sharedPref.getBoolean("limit_on_wifi", true)) {
                    editor.putLong("flimit", sharedPref.getLong("flimit", 0) + RX2 + RXMob2 + TX2 + TXMob2);
                    editor.commit();
                    //hardcode below
                    if (sharedPref.getLong("flimit", 0) >= (Long.parseLong(sharedPref.getString("limit", "0")) * 1024) && !sharedPref.getBoolean("noti_visible2", false)) {
                        builder1 = new Notification.Builder(getApplicationContext());
                        builder1.setContentTitle("Warning");
                        builder1.setContentText("You have reached the Monthly limit");
                        builder1.setSmallIcon(R.drawable.dffd);
                        builder1.setAutoCancel(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            builder1.setPriority(Notification.PRIORITY_HIGH);
                        }
                        builder1.setOngoing(false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            n2 = builder1.build();
                        }
                        nm2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nm2.notify(2, n2);
                        editor.putBoolean("noti_visible2", true);
                    }
                }

                //reseting data used on month change
                if (t3 != sharedPref.getInt("cur_month", 0)) {
                    editor.putLong("flimit", 0);
                    editor.putInt("cur_month", t3);
                    editor.putBoolean("noti_visible2", false);
                    editor.commit();
                    nm2.cancelAll();
                }

                if (!sharedPref.getBoolean("not_pers", false))
                    builder.setOngoing(false);
                else {
                    builder.setOngoing(true);
                    editor.putBoolean("noti_visible", true);
                    editor.commit();
                }

                if (sharedPref.getBoolean("noti_visible", false)) {
                    if (wifiEnabled())
                        builder.setContentText("Down : " + df2.format((float) (RXMob2 + RX2) / divisor2) + unit2 + "   " + "Up : " + df2.format((float) (TX2 + TXMob2) / divisor2) + unit2);
                    notificationManger.notify(1, builder.build());
                }


            } catch (Exception n) {
                Log.v("piss off", String.valueOf(n.getMessage()));
            }

            if (timer == 30) {
                System.gc();
                timer = 0;
            }
            timer++;
            handler.postDelayed(runnable, 1000);
        }
    };

    private void formatUnits() {
        //formatting units for display
        int l = Integer.parseInt(sharedPref.getString("listPref2", "1"));
        if (l == 1)
            unit2 = " KBPS";
        else if (l == 2)
            unit2 = " MBPS";
        else
            unit2 = " GBPS";
        divisor2 = (long) pow(1024, l);
        if (l != 1)
            df2 = new DecimalFormat("0.000");
        else
            df2 = new DecimalFormat("0");
    }

    private boolean wifiEnabled() {
        final ConnectivityManager connMgr = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return wifi.isConnectedOrConnecting();
    }
}