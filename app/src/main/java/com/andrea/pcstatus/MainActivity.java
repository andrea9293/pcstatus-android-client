package com.andrea.pcstatus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements Observer {

    private TextView mTextMessage;
    private BottomNavigationView navigation;
    private LineChartMaker systemLoadView;
    private LineChartMaker batteryView;
    private MultipleLineChartMaker cpuLoadView;
    private MenuItem rescanButton;
    private MenuItem connected;
    private MenuItem disconnected;
    private LinearLayout chartsLinearLayout;
    private PieChartMaker disksView;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private String TAG = "MainActivity";

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
        AlertDialogManager.alertBox(getString(R.string.choose_wifi_bluetooth), getString(R.string.PC_status_need_connection_via_wifi_or_bluetooth),
                REQUEST_WIFI_OR_BLUETOOTH);

        mTextMessage = findViewById(R.id.message);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //richiesta del permesso per la fotocamera
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
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

    @Override
    protected void onDestroy() {
        ClientManager.taskCancel();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        rescanButton = menu.findItem(R.id.rescanMenuButton);
        connected = menu.findItem(R.id.connected);
        disconnected = menu.findItem(R.id.disconnected);
        connected.setVisible(false);
        disconnected.setVisible(true);
        rescanButton.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.disconnected:
                printToastMessage(getString(R.string.connection_lost));
                break;
            case R.id.connected:
                printToastMessage(getString(R.string.connected_to_pc));
                break;
            case R.id.infoMenuButton:
                AlertDialogManager.aboutApplication();
                break;
            case R.id.rescanMenuButton:
                AlertDialogManager.alertBox(getString(R.string.choose_wifi_bluetooth), getString(R.string.PC_status_need_connection_via_wifi_or_bluetooth),
                        REQUEST_WIFI_OR_BLUETOOTH);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void isConnected(boolean b) {
        rescanButton.setVisible(b);
        connected.setVisible(!b);
        disconnected.setVisible(b);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permessi concessi
                    Log.d(TAG, "permessi concessi correttamente");

                } else {
                    //permessi negati
                    printToastMessage(getString(R.string.camera_permits_not_granted));
                }
            }

        }
    }

    private void printToastMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
