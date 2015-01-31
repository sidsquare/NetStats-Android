package com.siddharth.netstats;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends ActionBarActivity
{
    boolean cfrag1_is_enabled = false,first_time=true;
    String[] menu;
    String temp1="0 KB", temp2="0 KB", temp3="0 KBPS", temp4="0 KBPS",date;
    public Handler handler = new Handler();
    public long rx, tx, temp_rx, temp_tx;
    DrawerLayout dLayout;
    ListView dList;
    ArrayAdapter<String> adapter;
    cfrag1 frag;cfrag2 frag2;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //intialize the view
        cfrag1 newFragment = new cfrag1();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, newFragment, "cfrag1");
        //ft.addToBackStack("cfrag1");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
        cfrag1_is_enabled = true;
        frag = (cfrag1) getSupportFragmentManager().findFragmentByTag("cfrag1");

        //intialize the left drawer
        menu = new String[]{"Home", "Charts"};
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        dList = (ListView) findViewById(R.id.left_drawer);
        adapter = new ArrayAdapter<String>(this, R.layout.nav_menu, R.id.textview, menu);
        dList.setAdapter(adapter);
        dList.setSelector(android.R.color.holo_blue_dark);

        //drawer click event
        dList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
            {
                dLayout.closeDrawers();
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
                        frag.go(temp1, temp2, temp3, temp4);
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
        //call main function
        prog();
    }

    public void setdata()
    {
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("select down_transfer from transfer_day order by date(date) asc limit 7;", null);


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
        while (i<c.getCount())
        {
            yVals1.add(new BarEntry((float)c.getInt(0)/1024, i));
            c.moveToNext();
            i++;
        }

        BarDataSet set1 = new BarDataSet(yVals1, "Data Usage in MB");
        set1.setBarSpacePercent(35f);
        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        frag2.go(xVals, dataSets);
    }

    private void prog()
    {
        //open the database
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS transfer_day('date' VARCHAR NOT NULL UNIQUE,'down_transfer' integer);");

        //get today's date and create entry
        Time now = new Time();
        now.setToNow();
        date= new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Cursor c=db.rawQuery("select * from transfer_day where date=\"" + date + "\";",null);
        if(c.getCount()==0)
            db.execSQL("insert into transfer_day values(\""+date+"\",0);");

        handler.postDelayed(runnable, 1000);
    }

    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            //intialize fragment at startup
            if(first_time==true)
            {
                rx = TrafficStats.getTotalRxBytes();
                rx = rx / (1024);
                tx = TrafficStats.getTotalTxBytes();
                tx = tx / (1024);
                temp_tx = tx;
                temp_rx = rx;
                frag.go(temp1, temp2, temp3, temp4);
                first_time=false;
            }
            try
            {
                //get and set current stats
                if (cfrag1_is_enabled == true && frag != null)
                    frag.go(temp1, temp2, temp3, temp4);
                long rx1 = TrafficStats.getTotalRxBytes();
                rx1 = rx1 / (1024);
                long tx1 = TrafficStats.getTotalTxBytes();
                tx1 = tx1 / (1024);
                long down_speed = rx1 - temp_rx, up_speed = tx1 - temp_tx, down_data = rx1 - rx, up_data = tx1 - tx;

                temp1 = Long.toString(down_data) + " KB";
                temp2 = Long.toString(up_data) + " KB";
                temp3 = Long.toString(down_speed) + " KBPS";
                temp4 = Long.toString(up_speed) + " KBPS";

                temp_tx = tx1;
                temp_rx = rx1;
                //Log.v("sfs","update transfer_day set down_transfer=down_transfer+"+down_speed+" where date = '"+date+"';");
                db.execSQL("update transfer_day set down_transfer=down_transfer+"+down_speed+" where date = '"+date+"';");

                handler.postDelayed(this, 1000);
            }
            catch (NullPointerException n)
            {
                Log.v("piss","off");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_exit:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}