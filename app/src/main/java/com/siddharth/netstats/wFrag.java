package com.siddharth.netstats;

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
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.XLabels;

import java.util.ArrayList;

public class wFrag extends Fragment {
    private static View v;
    private static ImageButton button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.wfrag, container, false);
        if (v != null) {
            Log.v("rock", "you");
        }
        button = (ImageButton) v.findViewById(R.id.imageButton);
        final Animation refAnim = AnimationUtils.loadAnimation(this.getActivity(), R.anim.refresh_animation);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.refreshGraph = true;
                button.startAnimation(refAnim);
            }
        });
        return v;
    }

    public void chartIt(ArrayList<String> xVals, ArrayList<BarDataSet> dataSets) {

        BarChart b = (BarChart) v.findViewById(R.id.chart);
        b.set3DEnabled(true);
        b.setDrawBarShadow(false);
        b.setDepth((float) 2.1);
        //b.setDrawHorizontalGrid(false);
        b.setDrawVerticalGrid(false);
        b.animateXY(700, 700);
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
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        if (MainActivity.refreshGraph)
            button.clearAnimation();
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