package com.siddharth.netstats;


import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;

import java.util.ArrayList;


public class cfrag2 extends Fragment {

    SQLiteDatabase db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag2, container, false);
    }
    public void go(ArrayList<String> xVals,ArrayList<BarDataSet> dataSets)
    {

        BarChart b=(BarChart)getView().findViewById(R.id.chart);



        BarData data = new BarData(xVals, dataSets);


        b.setData(data);
    }

}