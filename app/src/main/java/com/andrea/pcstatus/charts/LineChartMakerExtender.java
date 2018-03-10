package com.andrea.pcstatus.charts;

import android.graphics.Color;

import com.andrea.pcstatus.MainActivity;
import com.andrea.pcstatus.R;
import com.andrea.pcstatus.SingletonBatteryStatus;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by andre on 29/11/2017.
 */

public class LineChartMakerExtender extends LineChartMaker implements Observer, InterfaceChart {
    private MainActivity mainActivity;
    public LineChartMakerExtender(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivity = mainActivity;
        mChart.getDescription().setText(mainActivity.getString(R.string.current_battery_level));
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

            if (SingletonBatteryStatus.getInstance().getBatteryPerc() == null)
                data.addEntry(new Entry(set.getEntryCount(), 0), 0);
            else
                data.addEntry(new Entry(set.getEntryCount(), SingletonBatteryStatus.getInstance().getBatteryPerc()), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());
        }

    }

    @Override
    public LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, mainActivity.getString(R.string.current_battery_level));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(139, 195, 74));
        set.setCircleColor(Color.rgb(139, 195, 74));
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(Color.rgb(139, 195, 74));
        set.setDrawFilled(true);
        set.setFillColor(Color.rgb(255,247,173));
        set.setFillAlpha(1000);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.DKGRAY);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void update(Observable observable, Object o) {
        addEntry();
    }
}
