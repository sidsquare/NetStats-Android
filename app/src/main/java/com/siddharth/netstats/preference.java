package com.siddharth.netstats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
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
        Preference myPref = (Preference) findPreference("purge");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                SharedPreferences.Editor e = prefs.edit();
                e.putBoolean("purge", true);
                e.commit();
                return false;
            }
        });
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