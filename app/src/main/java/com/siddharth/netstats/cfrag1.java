package com.siddharth.netstats;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class cfrag1 extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.frag1, container, false);
    }
    public void go(String temp1,String temp2,String temp3,String temp4)
    {
        Log.v("gdfg", "begin");
        TextView t1=(TextView)getView().findViewById(R.id.tv1);
        t1.setText(temp1);
        t1=(TextView)getView().findViewById(R.id.tv2);
        t1.setText(temp2);
        t1=(TextView)getView().findViewById(R.id.tv3);
        t1.setText(temp3);
        t1=(TextView)getView().findViewById(R.id.tv4);
        t1.setText(temp4);
        Log.v("dsfs", "end");
    }
}