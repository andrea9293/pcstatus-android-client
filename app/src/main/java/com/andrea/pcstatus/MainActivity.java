package com.andrea.pcstatus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andrea.pcstatus.Charts.LineChartMaker;
import com.andrea.pcstatus.Charts.MultipleLineChartMaker;
import com.andrea.pcstatus.Charts.PieChartMaker;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    private TextView mTextMessage;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_WIFI_OR_BLUETOOTH = 2;
    private WiFiConnectionController wiFiConnectionController;
    private BluetoothConnectionController bluetoothConnectionController;
    private BottomNavigationView navigation;
    private LineChartMaker lineChartMaker;
    private LineChart singleLineChart;
    private LineChart multipleLineChart;
    private MultipleLineChartMaker multipleLineChartMaker;
    private MenuItem rescanButton;
    private LinearLayout chartsLinearLayout;
    private PieChartMaker pieChartMaker;
    private PieChart pieChart;
    private boolean connectionFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();


        multipleLineChartMaker = new MultipleLineChartMaker(this);
        multipleLineChart = multipleLineChartMaker.createLineChart();

        pieChartMaker = new PieChartMaker(this);
        pieChart = pieChartMaker.createChart();
        lineChartMaker = new LineChartMaker(this);
        singleLineChart = lineChartMaker.createLineChart();

        chartsLinearLayout = (LinearLayout) findViewById(R.id.chartLayout);
        chartsLinearLayout.addView(singleLineChart);

        SingletonModel.getInstance().setSharedPreferences(getApplicationContext());
        SingletonBatteryStatus.getInstance().addingObserver(MainActivity.this);
        alertBox("Choose wifi or Bluetooth", "PCstatus need a connection via WiFi or bluetooth\n" +
                "What you want to use?", REQUEST_WIFI_OR_BLUETOOTH);

        mTextMessage = (TextView) findViewById(R.id.message);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_miscellaneous:
                    chartsLinearLayout.removeAllViews();
                    chartsLinearLayout.addView(singleLineChart);
                    singleLineChart.animateY(1000);
                    if (SingletonBatteryStatus.getInstance().getMiscellaneous() != null)
                        mTextMessage.setText(SingletonBatteryStatus.getInstance().getMiscellaneous());
                    return true;
                case R.id.navigation_hdd:
                    chartsLinearLayout.removeAllViews();
                    chartsLinearLayout.addView(pieChart);
                    pieChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
                    if (SingletonBatteryStatus.getInstance().getDisks() != null)
                        mTextMessage.setText(SingletonBatteryStatus.getInstance().getDisks());
                    return true;
                case R.id.navigation_pcinfo:
                    chartsLinearLayout.removeAllViews();
                    if (SingletonBatteryStatus.getInstance().getComputerInfo() != null)
                        mTextMessage.setText(SingletonBatteryStatus.getInstance().getComputerInfo());
                    return true;
                case R.id.navigation_battery:
                    chartsLinearLayout.removeAllViews();
                    if (SingletonBatteryStatus.getInstance().getBattery() != null)
                        mTextMessage.setText(SingletonBatteryStatus.getInstance().getBattery());
                    return true;
                case R.id.navigation_cpu:
                    chartsLinearLayout.removeAllViews();
                    LinearLayout secondl = new LinearLayout(getApplicationContext());

                    chartsLinearLayout.addView(multipleLineChart);
                    multipleLineChart.animateY(1000);
                    if (SingletonBatteryStatus.getInstance().getCpu() != null)
                        mTextMessage.setText(SingletonBatteryStatus.getInstance().getCpu());
                    return true;
            }
            return false;
        }

    };


    public void startWifiClient() {
        taskCancel();
        wiFiConnectionController = new WiFiConnectionController(MainActivity.this);
    }

    public void setTextView(String text) {
        mTextMessage.setText(text);
    }

    @Override
    public void update(Observable observable, Object o) {
        refresh();
        updateCharts();
    }

    private void refresh() {
        switch (navigation.getSelectedItemId()) {
            case R.id.navigation_miscellaneous:
                if (SingletonBatteryStatus.getInstance().getMiscellaneous() != null)
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getMiscellaneous());
                break;
            case R.id.navigation_hdd:
                if (SingletonBatteryStatus.getInstance().getDisks() != null)
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getDisks());
                break;
            case R.id.navigation_pcinfo:
                if (SingletonBatteryStatus.getInstance().getComputerInfo() != null)
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getComputerInfo());
                break;
            case R.id.navigation_battery:
                if (SingletonBatteryStatus.getInstance().getBattery() != null)
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getBattery());
                break;
            case R.id.navigation_cpu:
                if (SingletonBatteryStatus.getInstance().getCpu() != null)
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getCpu());
                break;
        }
    }

    public void startBluetoothClient() {
        taskCancel();
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
                        startBluetoothClient();
                    }
                }).setNegativeButton("CANCEL and try with WiFi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startWifiClient();
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
            bluetoothConnectionController.scheduleTask();
        }
        if (resultCode == RESULT_CANCELED) {
            taskCancel();
            alertBox("Choose wifi or Bluetooth", "PCstatus need a connection via WiFi or bluetooth\n" +
                    "What you want to use?", REQUEST_WIFI_OR_BLUETOOTH);
            //do nothing
        }
    }


    public void taskCancel() {
        if (wiFiConnectionController != null) {
            wiFiConnectionController.taskCancel();
            wiFiConnectionController = null;
        }
        if (bluetoothConnectionController != null) {
            bluetoothConnectionController.taskCancel();
            bluetoothConnectionController = null;
        }
    }

    public void alertBox(String title, String message, int REQUEST_WIFI_OR_BLUETOOTH) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Bluetooth", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        startBluetoothClient();
                    }
                }).setNegativeButton("WiFi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startWifiClient();
            }
        }).show();
    }

    @Override
    protected void onDestroy() {
        taskCancel();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_with_scan, menu);
        rescanButton = menu.findItem(R.id.rescanMenuButton);
        rescanButton.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settingsMenuButton:
                //your action
                break;
            case R.id.rescanMenuButton:
                alertBox("Choose wifi or Bluetooth", "PCstatus need a connection via WiFi or bluetooth\n" +
                        "What you want to use?", REQUEST_WIFI_OR_BLUETOOTH);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void setConnectionFlag(boolean b) {
        connectionFlag = b;
        if (connectionFlag) {
            rescanButton.setVisible(false);
        } else {
            rescanButton.setVisible(true);
        }
    }

    public boolean isConnectionFlag() {
        return connectionFlag;
    }

    private void updateCharts(){
        lineChartMaker.addEntry();
        multipleLineChartMaker.addEntry();
        pieChartMaker.setData(2,100);
    }
}
