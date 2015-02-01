package com.siddharth.netstats;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class settings extends ActionBarActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }
    public void destroydb()
    {
        //delete the database

        SQLiteDatabase db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("delete from transfer_day");
    }
}
