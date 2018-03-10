package com.andrea.pcstatus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

/**
 * Created by andre on 07/03/2018.
 *
 */

public class MainController {
    private static final int REQUEST_ENABLE_BT = 1;
    private MainActivity mainActivity;

    MainController(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setConnectionFlag(boolean b) {
        if (b) {
            mainActivity.isConnected(false);
        } else {
            mainActivity.isConnected(true);
        }
    }

    Context getApplicationContext(){
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
