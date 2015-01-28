package com.siddharth.netstats;

import android.net.TrafficStats;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity
{
    private Handler handler = new Handler();
    private long rx,tx;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rx=TrafficStats.getTotalRxBytes();rx=rx/(1024);
        tx=TrafficStats.getTotalTxBytes();tx=tx/(1024);
        TextView t1=(TextView)findViewById(R.id.tv1);
        String temp;
        temp = "Download "+Long.toString(rx)+" KB";
        t1.setText(temp);
        TextView t2=(TextView)findViewById(R.id.tv2);
        temp = "Upload "+Long.toString(tx)+" KB";
        t2.setText(temp);
        prog();
    }

    private void prog()
    {
        handler.postDelayed(runnable, 100);

    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long rx1=TrafficStats.getTotalRxBytes();rx1=rx1/(1024);
            long tx1=TrafficStats.getTotalTxBytes();tx1=tx1/(1024);
            long down_speed=rx1-rx,up_speed=rx1-rx;
            rx=rx1;
            tx=tx1;
            TextView t1=(TextView)findViewById(R.id.tv1);
            String temp;
            temp = "Download Speed "+Long.toString(down_speed)+" KB";
            t1.setText(temp);
            TextView t2=(TextView)findViewById(R.id.tv2);
            temp = "Upload Speed "+Long.toString(up_speed)+" KB";
            t2.setText(temp);
            rx=rx1;tx=tx1;
            handler.postDelayed(this, 1000);
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
