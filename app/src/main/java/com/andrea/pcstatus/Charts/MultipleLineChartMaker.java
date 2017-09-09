package com.andrea.pcstatus.Charts;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.Random;

/**
 * Created by andre on 09/09/2017.
 */

public class MultipleLineChartMaker implements OnChartValueSelectedListener {
    private MainActivity mainActivity;
    private LineChart mChart;

    public MultipleLineChartMaker(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public LineChart createLineChart() {

        //mChart = (LineChart) mainActivity.findViewById(R.id.chart1);
        mChart = new LineChart(mainActivity.getApplicationContext());
        mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText("CPU load");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        ;
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
        l.setForm(Legend.LegendForm.CIRCLE);
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

       /* xl.setTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        xl.setTextColor(Color.DKGRAY);*/
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

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        int color = getRandomColor();
        set.setColor(color);
        set.setCircleColor(color);
        set.setLineWidth(1f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(color);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.DKGRAY);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    public void addEntry() {

        LineData data = mChart.getData();
        Float[] tmp = SingletonBatteryStatus.getInstance().getPercPerThread();

            if (data != null) {
            for (int i = 0; i < tmp.length; i++) {
                //dataSets.add(data.getDataSetByIndex(i));
                ILineDataSet set = data.getDataSetByIndex(i);

                if (set == null) {
                    set = createSet();
                    data.addDataSet(set);
                }


               // LineDataSet d = new LineDataSet(new Entry(set.getEntryCount(), tmp[i]), 0, "DataSet" + (i+1));
                if (tmp[i] == null)
                    set.addEntry(new Entry(set.getEntryCount(), 0));
                else
                    set.addEntry(new Entry(set.getEntryCount(), tmp[i]));
                data.notifyDataChanged();

                // let the chart know it's data has changed

            }
                mChart.notifyDataSetChanged();


                // limit the number of visible entries
                mChart.setVisibleXRangeMaximum(15);
                // mChart.setVisibleYRange(30, AxisDependency.LEFT);

                // move to the latest entry
                mChart.moveViewToX(data.getEntryCount());
        }


      /*  if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

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
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }*/
    }

    public int getRandomColor(){
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

}
