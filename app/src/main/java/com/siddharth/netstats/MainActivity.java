package com.siddharth.netstats;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity {

    private static boolean viewingWifi = false, firstTime = true, freeze = false;
    public static boolean refreshGraph = false;
    private static String unit1, unit2, temp1 = "--", temp2 = "--", temp7 = "--", temp8 = "--", temp3 = "--", temp4 = "--", temp5 = "--", temp6 = "--", date;
    private static Handler handler = new Handler();
    private static long last_save_rx, last_save_tx, divisor1, divisor2, rx, tx, temp_rx, temp_tx, d_offset = 0, u_offset = 0, d_offset_mob = 0, u_offset_mob = 0, rx_mob, tx_mob, temp_rx_mob = 0, temp_tx_mob = 0, d1, d2, d3, d4;
    private static SQLiteDatabase db;
    private static SharedPreferences sharedPref;
    private static Notification notification, n2;
    private static NotificationManager notificationManger, nm2;
    private static Notification.Builder builder, builder1;
    private static SharedPreferences.Editor editor;
    private static DecimalFormat df1, df2;

    wFrag w1;
    mFrag m1;
    ViewPager vPager;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);

        editor = sharedPref.edit();
        setpref();
        editor.putBoolean("dnd", false);
        editor.commit();

        stopService(new Intent(this, service.class));
        startService(new Intent(this, service.class));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Mobile Network"));
        tabLayout.addTab(tabLayout.newTab().setText("WiFi"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        vPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        vPager.setAdapter(adapter);
        vPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vPager.setCurrentItem(tab.getPosition());
                Log.v("dfd", String.valueOf(vPager.getCurrentItem()));
                switch (vPager.getCurrentItem()) {
                    case 0:
                        m1 = (mFrag) adapter.getItem(vPager.getCurrentItem());
                        temp1 = temp2 = temp3 = temp4 = temp5 = temp6 = temp7 = temp8 = "--";
                        viewingWifi = false;
                        break;
                    case 1:
                        w1 = (wFrag) adapter.getItem(vPager.getCurrentItem());
                        temp1 = temp2 = temp3 = temp4 = temp5 = temp6 = temp7 = temp8 = "--";
                        viewingWifi = true;
                        break;
                }
                setdata();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        vPager.setCurrentItem(0);
        m1 = (mFrag) adapter.getItem(vPager.getCurrentItem());
        getSupportFragmentManager().executePendingTransactions();
        formatUnits();
        // notification

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(this, notification.class);
        PendingIntent pendintIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        builder = new Notification.Builder(this);
        builder.setDeleteIntent(pendintIntent);
        builder.setContentTitle("NetStats");
        builder.setContentText("---");
        builder.setSmallIcon(R.drawable.dffd);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.large);
        builder.setLargeIcon(bm);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_HIGH);
        if (sharedPref.getBoolean("not_pers", false))
            builder.setOngoing(true);
        builder.setContentIntent(pendingIntent);
        notification = builder.build();
        notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(1, notification);
        editor.putBoolean("noti_visible", true);
        editor.commit();

        //open the database
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("create table if not exists transfer_stats('date' VARCHAR NOT NULL UNIQUE,'down_transfer' integer,'up_transfer' integer,'down_transfer_mob' integer,'up_transfer_mob' integer);");
        //db.execSQL("create table if not exists transfer_hour('hour' integer not null unique,'down' integer,'up' integer,'down_mob' integer,'up_mob' integer);");
        //db.execSQL("create table if not exists this_month('package' varchar not null ,'date' varchar not null,'app' varchar,'down' integer,'up' integer,'down_mob' integer,'up_mob' integer);");
        //db.execSQL("create table if not exists app('package' varchar not null ,'down' integer,'up' integer,'down_mob' integer,'up_mob' integer);");

        //get today's date and create entry
        date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Cursor c = db.rawQuery("select * from transfer_stats where date=\"" + date + "\";", null);
        if (c.getCount() == 0)
            db.execSQL("insert into transfer_stats values(\"" + date + "\",0,0,0,0);");

        handler.postDelayed(runnable, 1000);
    }


    //class for storing app information
    class MyClass {
        Long rx;
        String package_name;
        String app;
    }

    //comparion function
    public class MyIntComparable implements Comparator<MyClass> {
        @Override
        public int compare(MyClass o1, MyClass o2) {
            return (o1.rx > o2.rx ? -1 : (o1.rx.equals(o2.rx) ? 0 : 1));
        }
    }

    private void setdata3() {
        //db.beginTransaction();
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> yVals1 = new ArrayList<>();
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int uid;
        long uid_rx, uid_tx;
        ArrayList<MyClass> temp_f = new ArrayList<>();

        //cycle through installed apps to get net usage
        for (ApplicationInfo packageInfo : packages) {

            MyClass tempe = new MyClass();
            //get app uid and package
            tempe.package_name = packageInfo.packageName;
            uid = packageInfo.uid;
            try {
                //get app name
                ApplicationInfo app = pm.getApplicationInfo(tempe.package_name, 0);
                tempe.app = String.valueOf(pm.getApplicationLabel(app));
            } catch (Exception e) {
                Log.v("Exception", "ex");
            }
            Log.v(tempe.app, String.valueOf(uid));
            //get stats since boot
            uid_rx = TrafficStats.getUidRxBytes(uid) / (1024);
            uid_tx = TrafficStats.getUidTxBytes(uid) / (1024);
        }
        //add stored stats
            /*Cursor c = db.rawQuery("select * from app where package=\"" + tempe.package_name + "\";", null);
            if (c.getCount() == 0) {
                db.execSQL("insert into app values(\"" + tempe.package_name + "\",0,0,0,0);");
                tempe.rx = (long) 0 + uid_rx;
            } else {
                c.moveToFirst();
                if (!sharedPref.getBoolean("mobile_en", false))
                    tempe.rx = (long) c.getInt(1) + uid_rx + (long) c.getInt(2) + uid_tx;
                else
                    tempe.rx = (long) c.getInt(3) + uid_rx + (long) c.getInt(4) + uid_tx;
            }
            if (tempe.rx > 0)
                temp_f.add(tempe);
        }
        db.endTransaction();
        //sort to get top 5 apps
        Collections.sort(temp_f, new MyIntComparable());
        try {

            for (int x = 0; x < 5; x++) {
                MyClass ex = temp_f.get(x);
                xVals.add(ex.app);
                yVals1.add(new Entry((float) ex.rx / 1024, x));
            }
        } catch (IndexOutOfBoundsException gh) {

        }

        //charting
        PieDataSet set1 = new PieDataSet(yVals1, "");
        set1.setSliceSpace(7f);

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c1 : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c1);

        for (int c1 : ColorTemplate.JOYFUL_COLORS)
            colors.add(c1);

        for (int c1 : ColorTemplate.COLORFUL_COLORS)
            colors.add(c1);

        for (int c1 : ColorTemplate.LIBERTY_COLORS)
            colors.add(c1);

        for (int c1 : ColorTemplate.PASTEL_COLORS)
            colors.add(c1);

        colors.add(ColorTemplate.getHoloBlue());
        set1.setColors(colors);
        //frag4.go(xVals, set1);
*/
    }

    private void setdata2() {
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.beginTransaction();
        Cursor c;
        if (!sharedPref.getBoolean("mobile_en", false))
            c = db.rawQuery("select hour,down,up from transfer_hour order by CAST(hour AS INTEGER);", null);
        else
            c = db.rawQuery("select hour,down_mob,up_mob from transfer_hour order by CAST(hour AS INTEGER);", null);
        int count = 24;
        //charting
        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            xVals.add("" + i);
        }
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        c.moveToFirst();
        int i = 0, u = 0;
        while (i < 24) {
            if (c.getCount() != u) {//Log.v(String.valueOf(c.getInt(0)),String.valueOf(i));

                if (c.getInt(0) == i) {
                    yVals1.add(new BarEntry(new float[]{(float) c.getInt(1) / 1024, (float) c.getInt(2) / 1024}, i));
                    c.moveToNext();
                    u++;
                } else
                    yVals1.add(new BarEntry(new float[]{0, 0}, i));
            } else
                yVals1.add(new BarEntry((float) 0, i));
            i++;
        }

        BarDataSet set1 = new BarDataSet(yVals1, "Data Usage in MB");


        set1.setBarSpacePercent(0);
        set1.setColors(ColorTemplate.VORDIPLOM_COLORS);
        set1.setStackLabels(new String[]{"Download", "Upload"});
        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        db.endTransaction();
        // frag3.go(xVals, dataSets);
    }

    public void setdata() {
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.beginTransaction();
        Cursor c;
        if (viewingWifi)
            c = db.rawQuery("select down_transfer,up_transfer,date from transfer_stats order by date(date) desc limit 7;", null);
        else
            c = db.rawQuery("select down_transfer_mob,up_transfer_mob,date from transfer_stats order by date(date) desc limit 7;", null);
        //charting
        ArrayList<String> xVals = new ArrayList<>();
        int count = 7;
        c.moveToLast();
        for (int i = 0; i < count; i++) {
            try {
                xVals.add("" + c.getString(2).substring(5).replace('-', '/'));
            } catch (Exception r) {
                xVals.add("");
            }
            c.moveToPrevious();
        }
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        c.moveToLast();
        int i = 0;
        while (i < c.getCount()) {
            yVals1.add(new BarEntry((float) (c.getInt(0) + c.getInt(1)) / divisor1, i));
            c.moveToPrevious();
            i++;
        }

        BarDataSet set1 = new BarDataSet(yVals1, "Data Usage in" + unit1);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setBarSpacePercent(12f);
        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        c.close();

        db.endTransaction();
        if (viewingWifi)
            w1.chartIt(xVals, dataSets);
        else
            m1.chartIt(xVals, dataSets);
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //initialize fragment at startup
            if (firstTime) {
                //fetching offset values if entry exists
                setdata();
                Cursor c = db.rawQuery("select down_transfer,up_transfer,down_transfer_mob,up_transfer_mob from transfer_stats where date=\"" + date + "\";", null);
                if (c.getCount() != 0) {
                    Log.v(String.valueOf(d_offset), String.valueOf(sharedPref.getLong("d_today", 0)));

                    c.moveToFirst();
                    d_offset = c.getInt(0);
                    u_offset = c.getInt(1);
                    d_offset_mob = c.getInt(2);
                    u_offset_mob = c.getInt(3);
                    Log.v(String.valueOf(d_offset), String.valueOf(sharedPref.getLong("d_today", 0)));

                }

                temp_rx_mob = TrafficStats.getMobileRxBytes();
                temp_tx_mob = TrafficStats.getMobileTxBytes();
                if (temp_rx_mob == 0) {
                    temp_rx = TrafficStats.getTotalRxBytes() - last_save_rx;
                    temp_tx = TrafficStats.getTotalTxBytes() - last_save_tx;
                } else {
                    temp_rx = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
                    temp_tx = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
                }
                firstTime = false;
            }
            /*if(sharedPref.getBoolean("purge",false))
            {
                self_destruct();
            }*/
            else {
                try {
                    //get and set current stats
                    getSupportFragmentManager().executePendingTransactions();
                    switch (vPager.getCurrentItem()) {
                        case 0:
                            m1.go(temp1, temp2, temp3, temp4, temp5, temp6, temp7, temp8);
                            break;
                        case 1:
                            w1.go(temp1, temp2, temp3, temp4, temp5, temp6, temp7, temp8);
                            break;
                        default:
                            break;
                    }

                    //fix for the broken mobile data fetching bug in 5.0+
                    if (TrafficStats.getMobileRxBytes() == 0 && !freeze)
                        freeze = true;
                    else if (TrafficStats.getMobileRxBytes() != 0 && freeze) {
                        freeze = false;
                        temp_rx_mob = TrafficStats.getMobileRxBytes();
                        temp_tx_mob = TrafficStats.getMobileTxBytes();
                    }

                    if (wifiEnabled()) {

                        rx = TrafficStats.getTotalRxBytes() - last_save_rx;
                        tx = TrafficStats.getTotalTxBytes() - last_save_rx;

                        rx = rx - temp_rx;
                        tx = tx - temp_tx;

                        Log.v(String.valueOf(TrafficStats.getTotalRxBytes()), String.valueOf(temp_rx));
                        Log.v("dsfsdfs" + last_save_rx, String.valueOf(d_offset));
                        d_offset += rx;
                        u_offset += tx;

                        editor.putLong("d_today", d_offset);
                        editor.putLong("u_today", u_offset);

                        d1 = d1 + rx;
                        d2 = d2 + tx;
                        if (d1 < 0)
                            Log.v("-------------------", "eeeeeeeeeeeeeeeeeeeeeee");
                        temp_rx = temp_rx + rx;
                        temp_tx = temp_tx + tx;
                    } else if (!freeze) {
                        last_save_rx = rx_mob = TrafficStats.getMobileRxBytes();
                        last_save_tx = tx_mob = TrafficStats.getMobileTxBytes();
                        editor.putLong("last_save_rx", last_save_rx);
                        editor.putLong("last_save_tx", last_save_tx);

                        //speed calculation
                        rx_mob = rx_mob - temp_rx_mob;
                        tx_mob = tx_mob - temp_tx_mob;


                        //storing day offset values
                        d_offset_mob += rx_mob;
                        u_offset_mob += tx_mob;

                        editor.putLong("d_today_mob", d_offset_mob);
                        editor.putLong("u_today_mob", u_offset_mob);

                        d3 = d3 + rx_mob;
                        d4 = d4 + tx_mob;

                        temp_rx_mob = temp_rx_mob + rx_mob;
                        temp_tx_mob = temp_tx_mob + tx_mob;

                    }
                    editor.commit();

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

                    formatUnits();

                    //assigning current stat
                    if (viewingWifi) {
                        temp1 = df1.format((float) d1 / divisor1) + unit1;
                        temp2 = df1.format((float) d2 / divisor1) + unit1;
                        temp3 = df1.format((float) d_offset / divisor1) + unit1;
                        temp4 = df1.format((float) u_offset / divisor1) + unit1;
                        temp5 = df1.format((float) (TrafficStats.getTotalRxBytes() - last_save_rx) / divisor1) + unit1;
                        temp6 = df1.format((float) (TrafficStats.getTotalTxBytes() - last_save_tx) / divisor1) + unit1;
                        temp7 = df2.format((float) rx / divisor2) + unit2;
                        temp8 = df2.format((float) tx / divisor2) + unit2;
                    } else {
                        temp1 = df1.format((float) d3 / divisor1) + unit1;
                        temp2 = df1.format((float) d4 / divisor1) + unit1;
                        temp3 = df1.format((float) d_offset_mob / divisor1) + unit1;
                        temp4 = df1.format((float) u_offset_mob / divisor1) + unit1;
                        temp5 = df1.format((float) last_save_rx / divisor1) + unit1;
                        temp6 = df1.format((float) last_save_tx / divisor1) + unit1;
                        temp7 = df2.format((float) rx_mob / divisor2) + unit2;
                        temp8 = df2.format((float) tx_mob / divisor2) + unit2;
                    }


                    //refreshing graph if needed
                    if (refreshGraph) {
                        setdata();
                        refreshGraph = false;
                    }

                    //limit
                    if (!(wifiEnabled()) || sharedPref.getBoolean("limit_on_wifi", true)) {
                        editor.putLong("flimit", sharedPref.getLong("flimit", 0) + rx + rx_mob + tx + tx_mob);
                        editor.commit();

                        if (sharedPref.getLong("flimit", 0) >= (Long.parseLong(sharedPref.getString("limit", "0")) * 1024) && !sharedPref.getBoolean("noti_visible2", false)) {
                            builder1 = new Notification.Builder(getApplicationContext());
                            builder1.setContentTitle("Warning");
                            builder1.setContentText("You have reached the Monthly limit");
                            builder1.setSmallIcon(R.drawable.dffd);
                            builder1.setAutoCancel(true);
                            builder1.setPriority(Notification.PRIORITY_HIGH);
                            builder1.setOngoing(false);
                            n2 = builder1.build();
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
                            builder.setContentText("Down : " + df2.format((float) (rx_mob + rx) / divisor2) + unit2 + "   " + "Up : " + df2.format((float) (tx + tx_mob) / divisor2) + unit2);
                        notificationManger.notify(1, builder.build());
                    }

                } catch (Exception n) {
                    Log.v("piss off", String.valueOf(n.getMessage()));
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void formatUnits() {
        //formatting units for display
        int l = Integer.parseInt(sharedPref.getString("listPref", "1"));
        l = 2;//hardcoded change
        if (l == 1)
            unit1 = " KB";
        else if (l == 2)
            unit1 = " MB";
        else if (l == 3)
            unit1 = " GB";
        else
            unit1 = " TB";
        divisor1 = (long) pow(1024, l);
        if (l != 1)
            df1 = new DecimalFormat("0.000");
        else
            df1 = new DecimalFormat("0");

        l = Integer.parseInt(sharedPref.getString("listPref2", "1"));
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

    /* private void self_destruct()
     {
         db.execSQL("delete from 'transfer_week';");
         db.execSQL("delete from 'transfer_hour';");
         db.execSQL("delete from 'this_month';");
         db.execSQL("delete from 'app'");
         editor.putBoolean("purge",false);
         editor.commit();
         //get today's date and create entry
         date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
         Cursor c = db.rawQuery("select * from transfer_week where date=\"" + date + "\";", null);
         if (c.getCount() == 0)
             db.execSQL("insert into transfer_week values(\"" + date + "\",0,0,0,0);");
     }*/
    public boolean wifiEnabled() {
        final ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnectedOrConnecting())
            return true;
        else
            return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    //prevent exit on back press
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            this.moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //Action Bar left Menu actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_exit:
                finish();
                return true;
            case R.id.settings:

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        Log.v("cluster", "fuck");
        super.onDestroy();
        notificationManger.cancel(1);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("dnd", true);
        editor.putLong("d_today", d_offset);
        Log.v("47777", String.valueOf(sharedPref.getLong("d_today", 0)));
        editor.putLong("u_today", u_offset);
        editor.putLong("d_today_mob", d_offset);
        editor.putLong("u_today_mob", u_offset);

        editor.commit();
        Intent serviceIntent = new Intent(this, service.class);
        //startService(serviceIntent);
    }

    public void setpref() {
        if (!sharedPref.contains("cur_month"))
            editor.putInt("cur_month", 1);
        if (!sharedPref.contains("start_at_boot"))
            editor.putBoolean("start_at_boot", false);
        if (!sharedPref.contains("noti_visible"))
            editor.putBoolean("noti_visible", false);
        if (!sharedPref.contains("noti_visible2"))
            editor.putBoolean("noti_visible2", false);
        if (!sharedPref.contains("not_pers"))
            editor.putBoolean("not_pers", false);
        if (!sharedPref.contains("flimit"))
            editor.putLong("flimit", 0);
        if (!sharedPref.contains("is_app_open"))
            editor.putBoolean("is_app_open", true);
        if (!sharedPref.contains("limit"))
            editor.putString("limit", "1111110");
        if (!sharedPref.contains("dnd"))
            editor.putBoolean("dnd", false);
        if (!sharedPref.contains("mobile_en"))
            editor.putBoolean("mobile_en", false);
        if (!sharedPref.contains("limit_on_wifi"))
            editor.putBoolean("limit_on_wifi", false);
        if (!sharedPref.contains("d_today"))
            editor.putLong("d_today", 0);
        if (!sharedPref.contains("u_today"))
            editor.putLong("u_today", 0);
        if (!sharedPref.contains("d_today_mob"))
            editor.putLong("d_today_mob", 0);
        if (!sharedPref.contains("u_today_mob"))
            editor.putLong("u_today_mob", 0);
        if (!sharedPref.contains("last_save_rx"))
            editor.putLong("last_save_rx", 0);
        if (!sharedPref.contains("last_save_tx"))
            editor.putLong("last_save_tx", 0);
        if (!sharedPref.contains("purge"))
            editor.putBoolean("purge", false);
        editor.putBoolean("is_app_open", true);
        editor.putBoolean("dnd", false);

        editor.commit();
        Log.v(sharedPref.getString("limit", ""), String.valueOf(sharedPref.getLong("flimit", 0)));

        d_offset = sharedPref.getLong("d_today", 0);
        u_offset = sharedPref.getLong("u_today", 0);
        d_offset_mob = sharedPref.getLong("d_today_mob", 0);
        u_offset_mob = sharedPref.getLong("u_today_mob", 0);
        last_save_rx = sharedPref.getLong("last_save_rx", 0);
        last_save_tx = sharedPref.getLong("last_save_tx", 0);

        Log.v(String.valueOf(d_offset), String.valueOf(sharedPref.getLong("d_today", 0)));
        editor.putLong("d_today", 0);
        editor.putLong("u_today", 0);
        editor.putLong("d_today_mob", 0);
        editor.putLong("u_today_mob", 0);

        editor.putBoolean("purge", false);
        editor.putBoolean("noti_visible2", false);
        editor.commit();
    }
}