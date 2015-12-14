package com.jacknova.networkstatistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;

public class wFrag extends Fragment {
    private static View v;
    private static ImageButton button, button2,button3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.wfrag, container, false);
        if (v != null) {
            Log.v("rock", "you");
        }
        if (v != null) {
            button = (ImageButton) v.findViewById(R.id.imageButton);
        }
        final Animation refAnim = AnimationUtils.loadAnimation(this.getActivity(), R.anim.refresh_animation);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.refreshGraph = true;
                button.startAnimation(refAnim);
            }
        });
        button2 = (ImageButton) v.findViewById(R.id.imageButton2);
        final Animation refAnim2 = AnimationUtils.loadAnimation(this.getActivity(), R.anim.refresh_animation);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.refreshGraph2 = true;
                button2.startAnimation(refAnim2);
            }
        });
        button3 = (ImageButton) v.findViewById(R.id.imageButton3);
        final Animation refAnim3 = AnimationUtils.loadAnimation(this.getActivity(), R.anim.refresh_animation);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.refreshGraph3 = true;
                button3.startAnimation(refAnim3);
            }
        });
        return v;
    }

    public void chartIt(ArrayList<String> xVals, ArrayList<BarDataSet> dataSets) {

        BarChart b = (BarChart) v.findViewById(R.id.chart);
        b.setDrawBarShadow(false);
        b.animateXY(700, 700);
        b.setDescription("Last 7 Days");

        XAxis xl = b.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTextSize(8f);
        xl.setSpaceBetweenLabels(0);
        xl.setDrawGridLines(false);

        YAxis y1=b.getAxisRight();
        y1.setEnabled(false);

        BarData data = new BarData(xVals, dataSets);
        b.setData(data);
        b.setVisibleXRangeMinimum(0.0f);

        Legend l = b.getLegend();
        l.setWordWrapEnabled(true);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        if (MainActivity.refreshGraph)
            button.clearAnimation();
    }

    public void chartIt2(ArrayList<String> xVals, PieDataSet dataSets) {
        PieChart b = (PieChart) v.findViewById(R.id.chart2);

        b.setCenterTextSize(10f);
        b.animateXY(500, 500);
        b.setDescription("");
        b.setDrawCenterText(true);
        b.setDrawSliceText(false);
        b.setCenterText("Data Usage by App (" + MainActivity.unit1 + ")");
        for(int x=0;x<xVals.size();x++)
            Log.v("low",xVals.get(x));
        PieData data = new PieData(xVals, dataSets);
        data.setValueTextSize(9f);
        b.setData(data);

        Legend l = b.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        if (MainActivity.refreshGraph2)
            button2.clearAnimation();
        System.gc();
    }

    public void chartIt3(ArrayList<String> xVals, ArrayList<BarDataSet> dataSets) {
        BarChart b = (BarChart) v.findViewById(R.id.chart3);
        b.setDrawBarShadow(false);
        b.animateXY(1000, 2000);
        b.setDescription("");
        b.setVerticalScrollBarEnabled(true);
        b.setHorizontalScrollBarEnabled(true);

        XAxis xl = b.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTextSize(8f);
        xl.setSpaceBetweenLabels(0);
        xl.setDrawGridLines(false);

        YAxis y1=b.getAxisRight();
        y1.setEnabled(false);

        BarData data = new BarData(xVals, dataSets);
        b.setData(data);
        b.setVisibleXRangeMinimum(0.0f);
        Legend l = b.getLegend();
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);
        if (MainActivity.refreshGraph3)
            button3.clearAnimation();
        System.gc();
    }
    public void go(String temp1, String temp2, String temp3, String temp4, String temp5, String temp6, String temp7, String temp8) {
        if (v != null) {
            Log.v("rock", "you");
            TextView t = (TextView) v.findViewById(R.id.textView12);
            t.setText(temp1);
            t = (TextView) v.findViewById(R.id.textView18);
            t.setText(temp2);
            t = (TextView) v.findViewById(R.id.textView13);
            t.setText(temp3);
            t = (TextView) v.findViewById(R.id.textView19);
            t.setText(temp4);
            t = (TextView) v.findViewById(R.id.textView14);
            t.setText(temp5);
            t = (TextView) v.findViewById(R.id.textView20);
            t.setText(temp6);
            t = (TextView) v.findViewById(R.id.textView16);
            t.setText(temp7);
            t = (TextView) v.findViewById(R.id.textView17);
            t.setText(temp8);
        }
    }
}