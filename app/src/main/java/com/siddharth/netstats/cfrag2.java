package com.siddharth.netstats;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.XLabels;

import java.util.ArrayList;


public class cfrag2 extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cfrag2, container, false);
    }

    public void chartIt(ArrayList<String> xVals, ArrayList<BarDataSet> dataSets) {

        BarChart b = (BarChart) getView().findViewById(R.id.chart);
        b.set3DEnabled(true);
        b.setDrawBarShadow(false);
        b.setDepth((float) 2.1);
        //b.setDrawHorizontalGrid(false);
        b.setDrawVerticalGrid(false);
        b.animateXY(1000, 1000);
        b.setDescription("Last 7 Days");
        b.setDrawLegend(true);

        b.setDrawYLabels(false);
        XLabels xl = b.getXLabels();
        xl.setPosition(XLabels.XLabelPosition.BOTTOM);
        xl.setTextSize(10f);
        xl.setCenterXLabelText(true);
        xl.setSpaceBetweenLabels(0);


        BarData data = new BarData(xVals, dataSets);
        b.setData(data);
        Legend l = b.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
    }
}