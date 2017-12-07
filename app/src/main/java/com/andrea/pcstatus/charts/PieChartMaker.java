package com.andrea.pcstatus.charts;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;

import com.andrea.pcstatus.MainActivity;
import com.andrea.pcstatus.SingletonBatteryStatus;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by andre on 09/09/2017.
 */

public class PieChartMaker implements  Observer, InterfaceChart {

    private PieChart mChart;
    private MainActivity mainActivity;

    public PieChartMaker(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mChart = createChart();
        SingletonBatteryStatus.getInstance().addingObserver(this);
    }


    private PieChart createChart() {
        mChart = new PieChart(mainActivity);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);
        mChart.setDragDecelerationFrictionCoef(0.95f);
        mChart.setCenterTextTypeface(Typeface.createFromAsset(mainActivity.getAssets(), "OpenSans-Light.ttf"));
        mChart.setCenterTextSize(17);
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);
        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);
        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);
        mChart.setDrawCenterText(true);
        mChart.setRotationAngle(0);

        // disable rotation of the chart by touch
        mChart.setRotationEnabled(false);
        mChart.setHighlightPerTapEnabled(true);
        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(rgb("#140500"));
        mChart.setEntryLabelTypeface(Typeface.createFromAsset(mainActivity.getAssets(), "OpenSans-Regular.ttf"));
        mChart.setEntryLabelTextSize(12f);

        mChart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return mChart;
    }

    private void setData(int count, float range) {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        entries.add(new PieEntry(SingletonBatteryStatus.getInstance().getAvaibleFileSystem()[0], "Unused space"));
        entries.add(new PieEntry(100f - SingletonBatteryStatus.getInstance().getAvaibleFileSystem()[0], "Used space"));
        mChart.setCenterText("Drive " + SingletonBatteryStatus.getInstance().getFirstFilesystemLabel() + "\n" +
                "Free space: " + SingletonBatteryStatus.getInstance().getAvaibleFileSystem()[0] +" %");
        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(rgb("#bbdefb"));
        colors.add(rgb("#78909c"));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        data.setValueTypeface(Typeface.createFromAsset(mainActivity.getAssets(), "OpenSans-Regular.ttf"));
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);
        mChart.invalidate();
    }

    private static int rgb(String hex) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        return Color.rgb(r, g, b);
    }

    @Override
    public void addEntry() {
        setData(2,100);
    }

    @Override
    public View getView() {
        return mChart;
    }

    @Override
    public void animate() {
        mChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
    }

    @Override
    public void update(Observable observable, Object o) {
        addEntry();
    }
}
