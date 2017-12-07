package com.andrea.pcstatus;

import android.annotation.SuppressLint;

import com.andrea.pcstatus.connectionPackage.BluetoothConnectionController;
import com.andrea.pcstatus.connectionPackage.WiFiConnectionController;

/**
 * Created by andre on 13/11/2017.
 */

public class ClientManager {
    private static WiFiConnectionController wiFiConnectionController;
    static BluetoothConnectionController bluetoothConnectionController;
    @SuppressLint("StaticFieldLeak")
    static MainActivity mainActivity;

    public static void startWifiClient(String ip) {
        taskCancel();
        wiFiConnectionController = new WiFiConnectionController(mainActivity, ip);
    }

    public static void startBluetoothClient() {
        taskCancel();
        bluetoothConnectionController = new BluetoothConnectionController(mainActivity);
    }

    public static void taskCancel() {
        if (wiFiConnectionController != null) {
            WiFiConnectionController.taskCancel();
            wiFiConnectionController = null;
        }
        if (bluetoothConnectionController != null) {
            bluetoothConnectionController.taskCancel();
            bluetoothConnectionController = null;
        }
    }
}
