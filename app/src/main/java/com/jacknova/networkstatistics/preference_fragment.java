package com.jacknova.networkstatistics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class preference_fragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName("setting");
        prefMgr.setSharedPreferencesMode(Context.MODE_PRIVATE);
        final SharedPreferences prefs = prefMgr.getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                    }
                }
        );
    }
}
