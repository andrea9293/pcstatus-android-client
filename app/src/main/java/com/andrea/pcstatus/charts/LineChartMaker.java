package com.andrea.pcstatus.charts;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;

import com.andrea.pcstatus.MainActivity;
import com.andrea.pcstatus.SingletonBatteryStatus;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by andre on 08/09/2017.
 */

public class LineChartMaker implements Observer, InterfaceChart {
    private MainActivity mainActivity;
    LineChart mChart;

    public LineChartMaker(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        mChart = createLineChart();
        SingletonBatteryStatus.getInstance().addingObserver(this);
    }

    private LineChart createLineChart(){
        mChart = new LineChart(mainActivity.getApplicationContext());

        // enable description text
        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText("CPU load");

        // enable touch gestures
        mChart.setTouchEnabled(true);


        // enable scaling and dragging
        mChart.setDragEnabled(true);;
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(Typeface.createFromAsset(mainActivity.getAssets(), "OpenSans-Light.ttf"));
        l.setTextColor(Color.DKGRAY);
        l.setTextSize(15);

        XAxis xl = mChart.getXAxis();

        //hide values in top of LineChart
        xl.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return "";
            }
        });


        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(Typeface.createFromAsset(mainActivity.getAssets(), "OpenSans-Light.ttf"));
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return mChart;
    }

    public LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Current CPU load");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.RED);
        set.setCircleColor(Color.RED);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(Color.RED);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.DKGRAY);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void addEntry() {
        LineData data = mChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            if (SingletonBatteryStatus.getInstance().getCpuLoad() == null)
                data.addEntry(new Entry(set.getEntryCount(), 0), 0);
            else
                data.addEntry(new Entry(set.getEntryCount(), SingletonBatteryStatus.getInstance().getCpuLoad()), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();


            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(15);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());
        }
    }

    @Override
    public View getView() {
        return mChart;
    }

    @Override
    public void animate(){
        mChart.animateY(1000);
    }

    private static int rgb(String hex) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        return Color.rgb(r, g, b);
    }

    @Override
    public void update(Observable observable, Object o) {
        addEntry();
    }
}
