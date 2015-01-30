package com.siddharth.netstats;

import android.net.TrafficStats;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
    String[] menu;
    public Handler handler = new Handler();
    public long rx,tx,temp_rx,temp_tx;
    DrawerLayout dLayout;
    ListView dList;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menu = new String[]{"Home","Charts"};
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        dList = (ListView) findViewById(R.id.left_drawer);
        adapter = new ArrayAdapter<String>(this, R.layout.nav_menu,R.id.textview,menu);
        dList.setAdapter(adapter);
        dList.setSelector(android.R.color.holo_blue_dark);
        dList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                dLayout.closeDrawers();
                if(position==0) {
                    cfrag1 newFragment = new cfrag1();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, newFragment);
                    transaction.commit();
                }
                else{
                    cfrag2 newFragment = new cfrag2();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, newFragment);
                    transaction.commit();
                }
            }
        });
        rx=TrafficStats.getTotalRxBytes();rx=rx/(1024);
        tx=TrafficStats.getTotalTxBytes();tx=tx/(1024);
        /*TextView t1=(TextView)findViewById(R.id.tv1);
        t1.setText("0 KB");
        t1=(TextView)findViewById(R.id.tv2);
        t1.setText("0 KB");
        t1=(TextView)findViewById(R.id.tv3);
        t1.setText("0 KBPS");
        t1=(TextView)findViewById(R.id.tv4);
        t1.setText("0 KBPS");*/
        temp_tx=tx;temp_rx=rx;
        //prog();
    }
    private void prog()
    {
        handler.postDelayed(runnable, 1000);
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long rx1=TrafficStats.getTotalRxBytes();rx1=rx1/(1024);
            long tx1=TrafficStats.getTotalTxBytes();tx1=tx1/(1024);
            String temp;
            long down_speed=rx1-temp_rx,up_speed=tx1-temp_tx,down_data=rx1-rx,up_data=tx1-tx;

            TextView t1=(TextView)findViewById(R.id.tv1);
            temp=Long.toString(down_data)+" KB";
            t1.setText(temp);
            t1=(TextView)findViewById(R.id.tv2);
            temp=Long.toString(up_data)+" KB";
            t1.setText(temp);
            t1=(TextView)findViewById(R.id.tv3);
            temp=Long.toString(down_speed)+" KBPS";
            t1.setText(temp);
            t1=(TextView)findViewById(R.id.tv4);
            temp=Long.toString(up_speed)+" KBPS";
            t1.setText(temp);

            temp_tx=tx1;temp_rx=rx1;
            handler.postDelayed(this, 1000);
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}