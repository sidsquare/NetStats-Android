package com.siddharth.netstats;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;

public class cfrag5 extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.cfrag5, container, false);
    }

    public void go(ArrayList<String> xVals, PieDataSet dataSets)
    {


    }
}
