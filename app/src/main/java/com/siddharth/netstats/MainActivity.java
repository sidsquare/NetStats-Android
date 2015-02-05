package com.siddharth.netstats;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends ActionBarActivity
{
    boolean cfrag1_is_enabled = false, first_time = true;
    String temp1 = "0 KB", temp2 = "0 KB", temp3 = "0 KBPS", temp4 = "0 KBPS", temp5 = "0 KB", temp6 = "0 KB", date;
    private Handler handler = new Handler();
    private long rx, tx, temp_rx, temp_tx, d_offset = 0, u_offset = 0,rx1,tx1;
    cfrag1 frag;
    cfrag2 frag2;
    SQLiteDatabase db;
    SharedPreferences sharedPref;
    private String[] mDrawerItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        if (!sharedPref.contains("start_at_boot"))
            editor.putBoolean("start_at_boot", false);
        if (!sharedPref.contains("is_app_open"))
            editor.putBoolean("is_app_open", true);
        if (!sharedPref.contains("dnd"))
            editor.putBoolean("dnd", false);
        editor.putBoolean("is_app_open", true);
        editor.putBoolean("dnd", false);

        editor.commit();

        d_offset=sharedPref.getLong("rx1",0);
        u_offset=sharedPref.getLong("tx1",0);
        editor.putLong("rx1",0);
        editor.putLong("tx1",0);
        editor.commit();

        stopService(new Intent(this, service.class));
        startService(new Intent(this, service.class));

        //initialize the view
        cfrag1 newFragment = new cfrag1();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, newFragment, "cfrag1");
        //ft.addToBackStack("cfrag1");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
        cfrag1_is_enabled = true;
        frag = (cfrag1) getSupportFragmentManager().findFragmentByTag("cfrag1");

        //initialize the left drawer
        mDrawerItems = new String[]{"Data", "Charts"};
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.nav_menu, mDrawerItems));

        //drawer click event
        mDrawerList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
            {
                mDrawerLayout.closeDrawers();
                if (position == 0)
                {
                    //switch fragments
                    cfrag1 newFragment = new cfrag1();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, newFragment, "cfrag1");
                    //ft.addToBackStack("cfrag1");
                    ft.commit();
                    getSupportFragmentManager().executePendingTransactions();   //fucking important

                    //intialize the view
                    frag = (cfrag1) getSupportFragmentManager().findFragmentByTag("cfrag1");
                    if (frag != null)
                    {
                        cfrag1_is_enabled = true;
                        frag.go(temp1, temp2, temp3, temp4, temp5, temp6);
                    }
                }
                else
                {
                    cfrag1_is_enabled = false;

                    //switch fragments
                    cfrag2 newFragment = new cfrag2();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, newFragment, "cfrag2");
                    //ft.addToBackStack("cfrag1");
                    ft.commit();
                    getSupportFragmentManager().executePendingTransactions();   //fucking important

                    //intialize the view
                    frag2 = (cfrag2) getSupportFragmentManager().findFragmentByTag("cfrag2");
                    if (frag != null)
                    {
                        setdata();
                    }
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

        //call main function
        prog();
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
        ArrayList<String> xVals = new ArrayList<String>();
        int count = 7;
        for (int i = 0; i < count; i++)
        {
            xVals.add(i + "");
        }
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        c.moveToFirst();
        int i = 0;
        while (i < c.getCount())
        {
            yVals1.add(new BarEntry((float) c.getInt(0) / 1024, i));
            c.moveToNext();
            i++;
        }

        BarDataSet set1 = new BarDataSet(yVals1, "Data Usage in MB");

        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setBarSpacePercent(35f);
        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        frag2.go(xVals, dataSets);
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
                frag.go(temp1, temp2, temp3, temp4, temp5, temp6);
                first_time = false;
            }
            try
            {
                //get and set current stats
                if (cfrag1_is_enabled == true && frag != null)
                    frag.go(temp1, temp2, temp3, temp4, temp5, temp6);
                rx1 = TrafficStats.getTotalRxBytes();
                rx1 = rx1 / (1024);
                tx1 = TrafficStats.getTotalTxBytes();
                tx1 = tx1 / (1024);
                Log.v("rx1", String.valueOf(rx1));
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
                if (temp.compareTo(date) != 0)
                {
                    date = temp;
                    d_offset = u_offset = 0;
                    db.execSQL("insert into transfer_week values(\"" + temp + "\",0,0);");//change date back to original --- handle sql exception later
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        boolean checked = prefs.getBoolean("start_at_boot", false);
        if (checked == true)
            menu.findItem(R.id.start_on_boot).setChecked(checked);
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
            case R.id.action_settings:
                return true;
            case R.id.start_on_boot:
                if (item.isChecked())
                {
                    item.setChecked(false);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("start_at_boot", false);
                    editor.commit();
                }
                else
                {
                    item.setChecked(true);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("start_at_boot", true);
                    editor.commit();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onDestroy()
    {
        Log.v("cluster","fuck");
        super.onDestroy();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("dnd", true);

        rx1 = TrafficStats.getTotalRxBytes();
        rx1 = rx1 / (1024);
        tx1 = TrafficStats.getTotalTxBytes();
        tx1 = tx1 / (1024);

        editor.putLong("rx1", rx1);
        editor.putLong("tx1",tx1);
        editor.commit();
        Log.v("rx1", String.valueOf(rx1));
        Intent serviceIntent = new Intent(this, service.class);
        startService(serviceIntent);
    }
}