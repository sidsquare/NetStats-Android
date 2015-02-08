package com.siddharth.netstats;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.utils.XLabels;
import com.github.mikephil.charting.utils.YLabels;

import java.util.ArrayList;

/**
 * Created by Siddharth on 08-02-2015.
 */
public class cfrag3 extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.cfrag3, container, false);
    }

    public void go(ArrayList<String> xVals, ArrayList<BarDataSet> dataSets)
    {
        BarChart b = (BarChart) getView().findViewById(R.id.chart);
        b.set3DEnabled(true);
        b.setDrawBarShadow(false);
        b.setDepth((float) 2.1);
        //b.setDrawHorizontalGrid(false);
        b.setDrawVerticalGrid(false);
        b.animateXY(1000,1000);
        b.setDescription("");

        b.setDrawYLabels(false);
        
        XLabels xl = b.getXLabels();
        xl.setPosition(XLabels.XLabelPosition.BOTTOM);
        xl.setTextSize(8f);
        xl.setCenterXLabelText(true);
        xl.setSpaceBetweenLabels(0);


        YLabels yl = b.getYLabels();
        yl.mDecimals=1;
        yl.setPosition(YLabels.YLabelPosition.LEFT);


        BarData data = new BarData(xVals, dataSets);
        b.setData(data);
    }
}
