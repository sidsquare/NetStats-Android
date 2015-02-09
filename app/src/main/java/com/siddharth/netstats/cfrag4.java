package com.siddharth.netstats;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.Legend;

import java.util.ArrayList;

public class cfrag4 extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.cfrag4, container, false);
    }

    public void go(ArrayList<String> xVals,PieDataSet dataSets)
    {
        PieChart b = (PieChart) getView().findViewById(R.id.chart);
        b.animateXY(1000, 1000);
        b.setDescription("");
        b.setUnit(" MB");
        b.setDrawLegend(true);
        b.setDrawXValues(false);
        b.setDrawCenterText(true);
        b.setDrawUnitsInChart(true);
        b.setValueTextColor(Color.BLACK);
        b.setValueTextSize(10f);
        b.setCenterText("Data Usage by App (MB)");

        PieData data = new PieData(xVals, dataSets);
        b.setData(data);
        Legend l = b.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        System.gc();

    }
}
