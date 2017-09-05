package com.andrea.pcstatus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Observer, OnChartValueSelectedListener {

    private TextView mTextMessage;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private EditText editTextIP;
    private WiFiConnectionController wiFiConnectionController;
    private TimerTask task;
    private BluetoothConnectionController bluetoothConnectionController;
    private Boolean isBluetoothConnection = false;
    private Context context;
    private BottomNavigationView navigation;
    private LineChart mChart;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_miscellaneous:
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getMiscellaneous());
                    mChart.setVisibility(LineChart.VISIBLE);
                    return true;
                case R.id.navigation_hdd:
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getDisks());
                    mChart.setVisibility(LineChart.INVISIBLE);
                    return true;
                case R.id.navigation_pcinfo:
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getComputerInfo());
                    return true;
                case R.id.navigation_battery:
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getBattery());
                    return true;
                case R.id.navigation_cpu:
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getCpu());
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext();

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(false);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);





        SingletonModel.getInstance().setSharedPreferences(getApplicationContext());
        SingletonBatteryStatus.getInstance().addingObserver(MainActivity.this);
        wiFiConnectionController = new WiFiConnectionController(this);
        //startWifi();
        startBluetoothClient();

        mTextMessage = (TextView) findViewById(R.id.message);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        scheduleTask();
    }

    public void startWifi(){
        wiFiConnectionController.checkWifiForStats();
    }


    public void setTextView(String text) {
        mTextMessage.setText(text);
    }

    @Override
    public void update(Observable observable, Object o) {
        refresh();
       // setTextView(SingletonBatteryStatus.getInstance().getMiscellaneous());
    }

    private void refresh() {
        switch (navigation.getSelectedItemId()) {
            case R.id.navigation_miscellaneous:
                mTextMessage.setText(SingletonBatteryStatus.getInstance().getMiscellaneous());
                break;
            case R.id.navigation_hdd:
                mTextMessage.setText(SingletonBatteryStatus.getInstance().getDisks());
                break;
            case R.id.navigation_pcinfo:
                mTextMessage.setText(SingletonBatteryStatus.getInstance().getComputerInfo());
                break;
            case R.id.navigation_battery:
                mTextMessage.setText(SingletonBatteryStatus.getInstance().getBattery());
                break;
            case R.id.navigation_cpu:
                mTextMessage.setText(SingletonBatteryStatus.getInstance().getCpu());
                break;
        }
    }

    public void startBluetoothClient() {
        bluetoothConnectionController = new BluetoothConnectionController(MainActivity.this);
    }

    public void alertBox(String title, String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message + " Press OK to try again.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
            }
        }).show();
    }

    public void alertBox(String title, String message, BluetoothConnectionController b) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message + " Press OK to try again.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        bluetoothConnectionController.bluetoothConnectionExecute();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startWifi();
            }
        }).show();
    }

    public void enableBluetooth(BluetoothAdapter btAdapter) {
        //Prompt user to turn on Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //bluetoothConnectionController.scheduleTask();
            bluetoothConnectionController.getStatFromServer();
        }
        if (resultCode == RESULT_CANCELED) {
            //do nothing
        }
    }

    private void scheduleTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        Log.d(TAG, "task programmato");
        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        Log.d(TAG, "task eseguito");
                       // wiFiConnectionController.getStatsFromServer();
                        bluetoothConnectionController.getStatFromServer();
                        addEntry();
                    }
                });
            }
        };
        timer.schedule(task, 0, 1500); //it executes this every 1 minute
    }

    private void taskCancel() {
        if (task != null)
            bluetoothConnectionController.stopConnection();
            task.cancel();
    }

    @Override
    public void onBackPressed() {
        taskCancel();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        taskCancel();
        super.onDestroy();
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

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
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
        }
    }
}
