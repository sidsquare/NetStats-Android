package com.siddharth.netstats;

import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity
{

    boolean cfrag1_is_enabled = false, first_time = true;
    String temp1 = "0 KB", temp2 = "0 KB", temp3 = "0 KBPS", temp4 = "0 KBPS", temp5 = "0 KB", temp6 = "0 KB", date;
    private Handler handler = new Handler();
    private long rx, tx, temp_rx, temp_tx, d_offset = 0, u_offset = 0, rx1, tx1;
    cfrag1 frag;
    cfrag2 frag2;
    cfrag3 frag3;
    cfrag4 frag4;
    cfrag5 frag5;
    preference prefe;
    SQLiteDatabase db;
    SharedPreferences sharedPref;
    private String[] mDrawerItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    Notification notification, n2;
    NotificationManager notificationManger, nm2;
    Notification.Builder builder, builder1;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        builder = new Notification.Builder(getApplicationContext());

        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);

        editor = sharedPref.edit();
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
            editor.putString("limit", "");
        if (!sharedPref.contains("dnd"))
            editor.putBoolean("dnd", false);
        editor.putBoolean("is_app_open", true);
        editor.putBoolean("dnd", false);

        editor.commit();
        Log.v(sharedPref.getString("limit", ""), String.valueOf(sharedPref.getLong("flimit", 0)));
        d_offset = sharedPref.getLong("d_today", 0);
        u_offset = sharedPref.getLong("u_today", 0);
        editor.putLong("d_today", 0);
        editor.putLong("u_today", 0);
        editor.putBoolean("noti_visible2", false);
        editor.commit();

        stopService(new Intent(this, service.class));
        startService(new Intent(this, service.class));

        //initialize the view
        cfrag1 newFragment = new cfrag1();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, newFragment, "cfrag1");
        //ft.addToBackStack("cfrag1");
        ft.commit();
        getFragmentManager().executePendingTransactions();
        cfrag1_is_enabled = true;
        frag = (cfrag1) getFragmentManager().findFragmentByTag("cfrag1");

        //initialize the left drawer
        mDrawerItems = new String[]{"Data", "Weekly Chart", "Hourly Chart", "App Chart", "App Data Usage", "Settings"};
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.nav_menu, mDrawerItems));

        //drawer click event
        mDrawerList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
            {
                mDrawerLayout.closeDrawers();
                switch (position)
                {
                    case 0:
                        //switch fragments
                        cfrag1 newFragment = new cfrag1();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, newFragment, "cfrag1");
                        //ft.addToBackStack("cfrag1");
                        ft.commit();
                        getFragmentManager().executePendingTransactions();   //fucking important

                        //initialize the view
                        frag = (cfrag1) getFragmentManager().findFragmentByTag("cfrag1");
                        if (frag != null)
                        {
                            cfrag1_is_enabled = true;
                            frag.go(temp1, temp2, temp3, temp4, temp5, temp6);
                        }
                        break;
                    case 1:
                        cfrag1_is_enabled = false;

                        //switch fragments
                        cfrag2 newFragment1 = new cfrag2();
                        ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, newFragment1, "cfrag2");
                        ft.commit();
                        getFragmentManager().executePendingTransactions();   //fucking important

                        //initialize the view
                        frag2 = (cfrag2) getFragmentManager().findFragmentByTag("cfrag2");
                        if (frag != null)
                        {
                            setdata();
                        }
                        break;
                    case 2:
                        cfrag1_is_enabled = false;

                        //switch fragments
                        cfrag3 newFragment3 = new cfrag3();
                        ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, newFragment3, "cfrag3");
                        ft.commit();
                        getFragmentManager().executePendingTransactions();   //fucking important

                        //initialize the view
                        frag3 = (cfrag3) getFragmentManager().findFragmentByTag("cfrag3");
                        if (frag != null)
                        {
                            setdata2();
                        }
                        break;
                    case 3:
                        cfrag1_is_enabled = false;

                        //switch fragments
                        cfrag4 newFragment4 = new cfrag4();
                        ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, newFragment4, "cfrag4");
                        ft.commit();
                        getFragmentManager().executePendingTransactions();   //fucking important

                        //initialize the view
                        frag4 = (cfrag4) getFragmentManager().findFragmentByTag("cfrag4");
                        if (frag != null)
                        {
                            setdata3();
                        }
                        break;
                    case 4:
                        cfrag1_is_enabled = false;

                        //switch fragments
                        cfrag5 newFragment5 = new cfrag5();
                        ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, newFragment5, "cfrag5");
                        ft.commit();
                        getFragmentManager().executePendingTransactions();   //fucking important

                        //initialize the view
                        frag5 = (cfrag5) getFragmentManager().findFragmentByTag("cfrag5");
                        if (frag != null)
                        {
                            setdata4();
                        }
                        break;
                    case 5:
                        cfrag1_is_enabled = false;

                        //switch fragments
                        preference newFragment2 = new preference();
                        ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, newFragment2, "prefe");
                        ft.commit();
                        getFragmentManager().executePendingTransactions();   //fucking important

                        //initialize the view
                        prefe = (preference) getFragmentManager().findFragmentByTag("prefe");
                        break;

                }
            }
        });

        //Set the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
        {
            @Override
            public void onDrawerClosed(View drawerView)
            {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //mDrawerList.setItemChecked(0, true);
        if (savedInstanceState == null)
        {
        }

        // notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(this, notification.class);
        PendingIntent pendintIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        builder.setDeleteIntent(pendintIntent);
        builder.setContentTitle("NetStats");
        builder.setContentText("Down : 0 KBPS         " + "Up : 0 KBPS");
        builder.setSmallIcon(R.drawable.no);
        builder.setAutoCancel(true);
        builder.setPriority(0);
        if (sharedPref.getBoolean("not_pers", false))
            builder.setOngoing(true);
        builder.setContentIntent(pendingIntent);
        notification = builder.build();
        notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(1, notification);
        editor.putBoolean("noti_visible", true);
        editor.commit();

        //call main function
        prog();
    }

    private void setdata4()
    {

    }


    //class for storing app information
    class MyClass
    {
        Long rx;
        String package_name;
        String app;
    }

    //comparion function
    public class MyIntComparable implements Comparator<MyClass>
    {
        @Override
        public int compare(MyClass o1, MyClass o2)
        {
            return (o1.rx > o2.rx ? -1 : (o1.rx == o2.rx ? 0 : 1));
        }
    }

    private void setdata3()
    {
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> yVals1 = new ArrayList<>();
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int uid;
        long uid_rx, uid_tx;
        ArrayList<MyClass> temp_f = new ArrayList<>();
        ArrayList<Long> temp_uid_tx = new ArrayList<>();

        //cycle through installed apps to get net usage
        for (ApplicationInfo packageInfo : packages)
        {

            MyClass tempe = new MyClass();
            //get app uid and package
            tempe.package_name = packageInfo.packageName;
            uid = packageInfo.uid;
            try
            {
                //get app name
                ApplicationInfo app = pm.getApplicationInfo(tempe.package_name, 0);
                tempe.app = String.valueOf(pm.getApplicationLabel(app));
            }
            catch (Exception e)
            {
                Log.v("Exception", "ex");
            }
            //get stats since boot
            uid_rx = TrafficStats.getUidRxBytes(uid) / (1024);
            uid_tx = TrafficStats.getUidTxBytes(uid) / (1024);
            //add stored stats
            Cursor c = db.rawQuery("select * from app where package=\"" + tempe.package_name + "\";", null);
            if (c.getCount() == 0)
            {
                db.execSQL("insert into app values(\"" + tempe.package_name + "\",0,0);");
                tempe.rx = (long) 0 + uid_rx;
            }
            else
            {
                c.moveToFirst();
                tempe.rx = Long.valueOf(c.getInt(1)) + uid_rx;
            }
            temp_f.add(tempe);
        }
        //sort to get top 5 apps
        Collections.sort(temp_f, new MyIntComparable());
        for (int x = 0; x < 5; x++)
        {
            MyClass ex = temp_f.get(x);
            xVals.add(ex.app);
            yVals1.add(new Entry((float) ex.rx / 1024, x));
        }

        //charting
        PieDataSet set1 = new PieDataSet(yVals1, "");
        set1.setSliceSpace(7f);

        ArrayList<Integer> colors = new ArrayList<Integer>();

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
        frag4.go(xVals, set1);
    }

    private void setdata2()
    {
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("select hour,down,up from transfer_hour order by CAST(hour AS INTEGER);", null);
        int count = 24;
        //charting
        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            xVals.add("" + i);
        }
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        c.moveToFirst();
        int i = 0, u = 0;
        while (i < 24)
        {
            if (c.getCount() != u)
            {//Log.v(String.valueOf(c.getInt(0)),String.valueOf(i));

                if (c.getInt(0) == i)
                {
                    yVals1.add(new BarEntry(new float[]{(float) c.getInt(1) / 1024, (float) c.getInt(2) / 1024}, i));
                    c.moveToNext();
                    u++;
                }
                else
                    yVals1.add(new BarEntry(new float[]{0, 0}, i));
            }
            else
                yVals1.add(new BarEntry((float) 0, i));
            i++;
        }

        BarDataSet set1 = new BarDataSet(yVals1, "Data Usage in MB");


        set1.setBarSpacePercent(0);
        set1.setColors(ColorTemplate.VORDIPLOM_COLORS);
        set1.setStackLabels(new String[]{"Download", "Upload"});
        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        frag3.go(xVals, dataSets);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void setdata()
    {
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("select down_transfer from transfer_week order by date(date) asc limit 7;", null);


        //charting
        ArrayList<String> xVals = new ArrayList<>();
        int count = 7;
        for (int i = 0; i < count; i++)
        {
            xVals.add("" + i);
        }
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        c.moveToFirst();
        int i = 0;
        while (i < c.getCount())
        {
            yVals1.add(new BarEntry((float) c.getInt(0) / 1024, i));
            c.moveToNext();
            i++;
        }

        BarDataSet set1 = new BarDataSet(yVals1, "Data Usage in MB");
        set1.setBarSpacePercent(0);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setBarSpacePercent(35f);
        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        frag2.go(xVals, dataSets);
    }


    private void prog()
    {
        //open the database
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS transfer_week('date' VARCHAR NOT NULL UNIQUE,'down_transfer' integer,'up_transfer' integer);");
        db.execSQL("create table if not exists transfer_hour('hour' integer not null unique,'down' integer,'up' integer);");
        db.execSQL("create table if not exists this_month('package' varchar not null ,'date' varchar not null,'app' varchar,'down' integer,'up' integer);");

        //get today's date and create entry
        Time now = new Time();
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

            if (first_time)
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
                frag.go(temp1, temp2, temp3, temp4, temp5, temp6);
                first_time = false;
            }
            try
            {
                //get and set current stats
                if (cfrag1_is_enabled && frag != null)
                    frag.go(temp1, temp2, temp3, temp4, temp5, temp6);
                rx1 = TrafficStats.getTotalRxBytes();
                rx1 = rx1 / (1024);
                tx1 = TrafficStats.getTotalTxBytes();
                tx1 = tx1 / (1024);
                long down_speed = rx1 - temp_rx, up_speed = tx1 - temp_tx, down_data = rx1 - rx, up_data = tx1 - tx;
                d_offset += down_speed;
                u_offset += up_speed;

                //assigning current stat
                temp1 = Long.toString(down_data) + " KB";
                temp2 = Long.toString(up_data) + " KB";
                temp3 = Long.toString(down_speed) + " KBPS";
                temp4 = Long.toString(up_speed) + " KBPS";
                temp5 = Long.toString(d_offset) + " KB";
                temp6 = Long.toString(u_offset) + " KB";


                temp_tx = tx1;
                temp_rx = rx1;

                //automatic date change
                Time now = new Time();
                now.setToNow();
                String temp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                int temp2 = now.hour;
                int temp3 = now.month + 1;
                if (temp.compareTo(date) != 0)
                {
                    date = temp;
                    d_offset = u_offset = 0;
                    db.execSQL("insert into transfer_week values(\"" + temp + "\",0,0);");//changing date back to original --- handle sql exception later
                }
                db.execSQL("update transfer_week set down_transfer=down_transfer+" + down_speed + " , up_transfer=up_transfer+" + up_speed + " where date = '" + date + "';");
                Cursor c = db.rawQuery("select * from transfer_hour where hour=\"" + String.valueOf(temp2) + "\";", null);
                if (c.getCount() == 0)
                    db.execSQL("insert into transfer_hour values(\"" + String.valueOf(temp2) + "\",0,0);");
                db.execSQL("update transfer_hour set down=down+" + down_speed + " , up=up+" + up_speed + " where hour = '" + String.valueOf(temp2) + "';");

                //limit
                editor.putLong("flimit", sharedPref.getLong("flimit", 0) + down_speed + up_speed);
                editor.commit();

                if (sharedPref.getLong("flimit", 0) >= (Long.parseLong(sharedPref.getString("limit", "0")) * 1024) && sharedPref.getBoolean("noti_visible2", false) == false)
                {
                    builder1 = new Notification.Builder(getApplicationContext());
                    builder1.setContentTitle("Warning");
                    builder1.setContentText("You have reached the Monthly limit");
                    builder1.setSmallIcon(R.drawable.no);
                    builder1.setAutoCancel(true);
                    builder1.setPriority(0);
                    builder1.setOngoing(false);
                    n2 = builder1.build();
                    nm2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm2.notify(2, n2);
                    editor.putBoolean("noti_visible2", true);
                }

                //reseting data used on month change
                if (temp3 != sharedPref.getInt("cur_month", 0))
                {
                    editor.putLong("flimit", 0);
                    editor.putInt("cur_month", temp3);
                    editor.putBoolean("noti_visible2", false);
                    editor.commit();
                    nm2.cancel(2);
                }

                if (!sharedPref.getBoolean("not_pers", false))
                    builder.setOngoing(false);
                else
                {
                    builder.setOngoing(true);
                    editor.putBoolean("noti_visible", true);
                    editor.commit();
                }

                if (sharedPref.getBoolean("noti_visible", false))
                {
                    builder.setContentText("Down : " + down_speed + " KBPS         " + "Up : " + up_speed + " KBPS");
                    notificationManger.notify(1, builder.build());
                }

                handler.postDelayed(this, 1000);
            }
            catch (NullPointerException n)
            {
                Log.v("piss", "off");
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    //prevent exit on back press
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            this.moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //Action Bar left Menu actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.action_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy()
    {
        Log.v("cluster", "fuck");
        super.onDestroy();
        notificationManger.cancel(1);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("dnd", true);

        editor.putLong("d_today", d_offset);
        editor.putLong("u_today", u_offset);
        editor.commit();
        Intent serviceIntent = new Intent(this, service.class);
        startService(serviceIntent);
    }
}