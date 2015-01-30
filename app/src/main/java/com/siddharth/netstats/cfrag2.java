package com.siddharth.netstats;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class cfrag2 extends ActionBarActivity {
    private ListView listView;private DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag2);

        String[] countryArray = {"Data", "Charts"};
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.nav_menu,R.id.textview, countryArray);
        listView = (ListView) findViewById(R.id.left_drawer);
        listView.setAdapter(adapter);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        listView.setOnItemClickListener(new DrawerItemClickListener());
    }
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
            mDrawerLayout.closeDrawer(listView);
            listView.setVisibility(View.INVISIBLE);
        }
    }

    private void selectItem(int position)
    {
        if (position == 0)
        {

            Intent t = new Intent(this, MainActivity.class);
            startActivity(t);

            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }
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