package com.andrea.pcstatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andrea.pcstatus.charts.InterfaceChart;
import com.andrea.pcstatus.charts.LineChartMaker;
import com.andrea.pcstatus.charts.LineChartMakerExtender;
import com.andrea.pcstatus.charts.MultipleLineChartMaker;
import com.andrea.pcstatus.charts.PieChartMaker;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Observable;
import java.util.Observer;

import io.fabric.sdk.android.Fabric;

import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_WIFI_OR_BLUETOOTH;

//todo eliminare le selezioni nei charts
//todo dare un'occhiata al model che sta incasinato secondo me

public class MainActivity extends AppCompatActivity implements Observer {

    private TextView mTextMessage;
    //private static final String TAG = "MainActivity";
//    private static final int REQUEST_ENABLE_BT = 1;
    private BottomNavigationView navigation;
    private LineChartMaker systemLoadView;
    private LineChartMaker batteryView;
    private MultipleLineChartMaker cpuLoadView;
    private MenuItem rescanButton;
    private LinearLayout chartsLinearLayout;
    private PieChartMaker disksView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics.getInstance(this);
        Fabric.with(this, new Crashlytics());
        MainController mainController = new MainController(this);
        AlertDialogManager.mainController = mainController;
        ClientManager.mainController = mainController;

        cpuLoadView = new MultipleLineChartMaker(this);
        disksView = new PieChartMaker(this);
        systemLoadView = new LineChartMaker(this);
        batteryView = new LineChartMakerExtender(this);
        
        chartsLinearLayout = findViewById(R.id.chartLayout);
        changeView(systemLoadView);

        SingletonModel.getInstance().setSharedPreferences(getApplicationContext());
        SingletonBatteryStatus.getInstance().addingObserver(MainActivity.this);
        AlertDialogManager.alertBox("Choose WiFi or Bluetooth", "PCstatus need a connection via WiFi or Bluetooth\n" +
                "What you want to use?", REQUEST_WIFI_OR_BLUETOOTH);

        mTextMessage = findViewById(R.id.message);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_miscellaneous:
                    changeView(systemLoadView);
                    if (SingletonBatteryStatus.getInstance().getMiscellaneous() != null)
                        mTextMessage.setText(SingletonBatteryStatus.getInstance().getMiscellaneous());
                    return true;
                case R.id.navigation_hdd:
                    changeView(disksView);
                    if (SingletonBatteryStatus.getInstance().getDisks() != null)
                        mTextMessage.setText(SingletonBatteryStatus.getInstance().getDisks());
                    return true;
                case R.id.navigation_pcinfo:
                    chartsLinearLayout.removeAllViews();
                    if (SingletonBatteryStatus.getInstance().getComputerInfo() != null)
                        mTextMessage.setText(SingletonBatteryStatus.getInstance().getComputerInfo());
                    return true;
                case R.id.navigation_battery:
                    changeView(batteryView);
                    if (SingletonBatteryStatus.getInstance().getBattery() != null)
                        mTextMessage.setText(SingletonBatteryStatus.getInstance().getBattery());
                    return true;
                case R.id.navigation_cpu:
                    changeView(cpuLoadView);
                    if (SingletonBatteryStatus.getInstance().getCpuInfo() != null)
                        mTextMessage.setText(SingletonBatteryStatus.getInstance().getCpuInfo());
                    return true;
            }
            return false;
        }

    };

    private void changeView(InterfaceChart chart) {
        chartsLinearLayout.removeAllViews();
        chartsLinearLayout.addView(chart.getView());
        chart.animate();
    }

    public void setTextView(String text) {
        mTextMessage.setText(text);
    }

    @Override
    public void update(Observable observable, Object o) {
        refresh();
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
                if (SingletonBatteryStatus.getInstance().getCpuInfo() != null)
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getCpuInfo());
                break;
        }
    }

   /* public void enableBluetooth() {
        //Prompt user to turn on Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ClientManager.bluetoothConnectionController.scheduleTask();
        }
        if (resultCode == RESULT_CANCELED) {
            ClientManager.taskCancel();
            AlertDialogManager.alertBox("Choose wifi or Bluetooth", "PCstatus need a connection via WiFi or bluetooth\n" +
                    "What you want to use?", REQUEST_WIFI_OR_BLUETOOTH);
        }
    }

    @Override
    protected void onDestroy() {
        ClientManager.taskCancel();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        rescanButton = menu.findItem(R.id.rescanMenuButton);
        rescanButton.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.infoMenuButton:
                //your action
                break;
            case R.id.rescanMenuButton:
                AlertDialogManager.alertBox("Choose wifi or Bluetooth", "PCstatus need a connection via WiFi or bluetooth\n" +
                        "What you want to use?", REQUEST_WIFI_OR_BLUETOOTH);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void rescanButtonVisibility(boolean b){
        rescanButton.setVisible(b);
    }
}
