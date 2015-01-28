package com.siddharth.netstats;

import android.net.TrafficStats;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        long rx=TrafficStats.getTotalRxBytes();rx=rx/(1024);
        long tx=TrafficStats.getTotalTxBytes();tx=tx/(1024);
        TextView t1=(TextView)findViewById(R.id.tv1);
        String temp;
        temp = "Download "+Long.toString(rx)+" KB";
        t1.setText(temp);
        TextView t2=(TextView)findViewById(R.id.tv2);
        temp = "Upload "+Long.toString(tx)+" KB";
        t2.setText(temp);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
