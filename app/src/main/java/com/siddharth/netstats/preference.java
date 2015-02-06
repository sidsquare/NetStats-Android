package com.siddharth.netstats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class preference extends PreferenceFragment
{
    PreferenceManager prefMgr;
    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName("setting");
        prefMgr.setSharedPreferencesMode(Context.MODE_PRIVATE);
        prefs = prefMgr.getSharedPreferences();
        addPreferencesFromResource(R.xml.preference);
        prefs.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener()
                {
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
                    {
                    }
                }
        );
    }
}