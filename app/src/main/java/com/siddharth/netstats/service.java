package com.siddharth.netstats;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
    boolean first_time = true;
    String temp1 = "0 KB", temp2 = "0 KB", temp3 = "0 KBPS", temp4 = "0 KBPS", temp5 = "0 KB", temp6 = "0 KB", date;
    private Handler handler = new Handler();
    private long rx, tx, temp_rx, temp_tx, d_offset = 0, u_offset = 0;
    SQLiteDatabase db;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onDestroy()
    {
        //Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        //Log.d(TAG, "onDestroy");
    }
    @Override
    public void onStart(Intent intent, int startid)
    {
        boolean boot, app,dnb;

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        //String g= String.valueOf(prefs.contains("start_at_boot"));
        //Toast.makeText(this, "ss "+ g, Toast.LENGTH_LONG).show();

        SharedPreferences.Editor editor = prefs.edit();
        if (!prefs.contains("start_at_boot"))
            editor.putBoolean("start_at_boot", false);
        if (!prefs.contains("is_app_open"))
            editor.putBoolean("is_app_open", true);
        if (!prefs.contains("do_not_disturb"))
            editor.putBoolean("do_not_disturb", true);
        if (!prefs.contains("temp1"))
        {editor.putString("temp1", "0 KB");Log.v("gone","gone");}
        if (!prefs.contains("temp2"))
            editor.putString("temp2", "0 KB");
        if (!prefs.contains("temp3"))
            editor.putString("temp3", "0 KBPS");
        if (!prefs.contains("temp4"))
            editor.putString("temp4", "0 KBPS");
        if (!prefs.contains("temp5"))
            editor.putString("temp5", "0 KB");
        if (!prefs.contains("temp6"))
            editor.putString("temp6", "0 KB");

        editor.commit();

        boot = prefs.getBoolean("start_at_boot", false);
        app = prefs.getBoolean("is_app_open", true);
        dnb=prefs.getBoolean("do_not_disturb",true);
        temp1=prefs.getString("temp1","0");
        temp2=prefs.getString("temp2","0");
        temp3=prefs.getString("temp3","0");
        temp4=prefs.getString("temp4","0");
        temp5=prefs.getString("temp5","0");
        temp6=prefs.getString("temp6","0");

        //g= String.valueOf(boot);
        //Toast.makeText(this,  "ee "+g, Toast.LENGTH_LONG).show();
        if (boot && !app && dnb==false)
        {
            Intent intents = new Intent(getBaseContext(), MainActivity.class);
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intents);
            //Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
            //Log.d(TAG, "onStart");
        }
        prog();
    }

    private void prog()
    {
        //open the database
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS transfer_week('date' VARCHAR NOT NULL UNIQUE,'down_transfer' integer,'up_transfer' integer);");

        //get today's date and create entry
        Time now = new Time();
        now.setToNow();
        date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Cursor c = db.rawQuery("select * from transfer_week where date=\"" + date + "\";", null);
        if (c.getCount() == 0)
            db.execSQL("insert into transfer_week values(\"" + date + "\",0,0);");

        handler.postDelayed(runnable, 1000);
    }

    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            //intialize fragment at startup
            if (first_time == true)
            {
                Cursor c = db.rawQuery("select down_transfer,up_transfer from transfer_week where date=\"" + date + "\";", null);
                if (c.getCount() != 0)
                {
                    c.moveToFirst();
                    d_offset = c.getInt(0);
                    u_offset = c.getInt(1);
                    temp5 = String.valueOf(d_offset) + " KB";
                    temp6 = String.valueOf(u_offset) + " KB";
                }
                rx = TrafficStats.getTotalRxBytes();
                rx = rx / (1024);
                tx = TrafficStats.getTotalTxBytes();
                tx = tx / (1024);
                temp_tx = tx;
                temp_rx = rx;
                first_time = false;
            }
            try
            {
                long rx1 = TrafficStats.getTotalRxBytes();
                rx1 = rx1 / (1024);
                long tx1 = TrafficStats.getTotalTxBytes();
                tx1 = tx1 / (1024);
                long down_speed = rx1 - temp_rx, up_speed = tx1 - temp_tx;
                d_offset += down_speed;
                u_offset += up_speed;


                //assigning current stat
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                if(prefs.getBoolean("do_not_disturb",true)==true)
                {
                    rx=rx1;tx=tx1;
                }
                long  down_data = rx1 - rx, up_data = tx1 - tx;

                temp1 = Long.toString(down_data) + " KB";editor.putString("temp1", temp1);
                temp2 = Long.toString(up_data) + " KB";editor.putString("temp2", temp2);
                temp3 = Long.toString(down_speed) + " KBPS";editor.putString("temp3", temp3);
                temp4 = Long.toString(up_speed) + " KBPS";editor.putString("temp4", temp4);
                temp5 = Long.toString(d_offset) + " KB";editor.putString("temp5", temp5);
                temp6 = Long.toString(u_offset) + " KB";editor.putString("temp6", temp6);
                editor.commit();

                temp_tx = tx1;
                temp_rx = rx1;

                //automatic date change
                Time now = new Time();
                now.setToNow();
                String temp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                if (temp.compareTo(date) != 0)
                {
                    date = temp;
                    d_offset = u_offset = 0;
                    db.execSQL("insert into transfer_week values(\"" + temp + "\",0,0);");//handle sql exception later
                }

                db.execSQL("update transfer_week set down_transfer=down_transfer+" + down_speed + " , up_transfer=up_transfer+" + up_speed + " where date = '" + date + "';");

                handler.postDelayed(this, 1000);
            }
            catch (NullPointerException n)
            {
                Log.v("piss", "off");
            }
        }
    };
}
