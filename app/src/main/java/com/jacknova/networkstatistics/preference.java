package com.jacknova.networkstatistics;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class preference extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new preference_fragment()).commit();
    }
}
