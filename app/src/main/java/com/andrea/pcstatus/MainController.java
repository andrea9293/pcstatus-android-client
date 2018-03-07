package com.andrea.pcstatus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

/**
 * Created by andre on 07/03/2018.
 *
 */

public class MainController {
    private boolean connectionFlag = false;
    private static final int REQUEST_ENABLE_BT = 1;
    private MainActivity mainActivity;

    MainController(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setConnectionFlag(boolean b) {
        connectionFlag = b;
        if (connectionFlag) {
            //rescanButton.setVisible(false);
            mainActivity.rescanButtonVisibility(false);
        } else {
            //rescanButton.setVisible(true);
            mainActivity.rescanButtonVisibility(true);
        }
    }

    public boolean isConnectionFlag() {
        return connectionFlag;
    }

    public void setTextView(String s) {
        mainActivity.setTextView(s);
    }

    public Context getApplicationContext(){
        return mainActivity.getApplicationContext();
    }

    public MainActivity getMainActivity(){
        return mainActivity;
    }


    public void enableBluetooth() {
        //Prompt user to turn on Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mainActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
}
