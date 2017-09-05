package com.andrea.pcstatus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Observer {

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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_miscellaneous:
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getMiscellaneous());
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getComputerInfo());
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(SingletonBatteryStatus.getInstance().getDisks());
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
            case R.id.navigation_dashboard:
                mTextMessage.setText(SingletonBatteryStatus.getInstance().getComputerInfo());
                break;
            case R.id.navigation_notifications:
                mTextMessage.setText(SingletonBatteryStatus.getInstance().getDisks());
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
}
