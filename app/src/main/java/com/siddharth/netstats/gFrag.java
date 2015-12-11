package com.siddharth.netstats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Siddharth on 23-Sep-15.
 */
public class gFrag extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v("sdfd", "fss");
        return inflater.inflate(R.layout.gfrag, container, false);
    }

    public void go() {
        Log.v("like a", "hurricane");
    }
}
