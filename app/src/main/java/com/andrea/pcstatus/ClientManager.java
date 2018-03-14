package com.andrea.pcstatus;

import com.andrea.pcstatus.connectionPackage.BluetoothController;
import com.andrea.pcstatus.connectionPackage.WiFiController;

/**
 * Created by andre on 13/11/2017.
 *
 */

class ClientManager {
    private static WiFiController wiFiController;
    private static BluetoothController bluetoothController;
    static MainController mainController;

    static void startWifiClient(String ip) {
        taskCancel();
        wiFiController = new WiFiController(ip, mainController);
    }

    static void startBluetoothClient() {
        taskCancel();
        bluetoothController = new BluetoothController(mainController);
    }

    static void taskCancel() {
        if (wiFiController != null) {
            WiFiController.taskCancel();
            wiFiController = null;
        }
        if (bluetoothController != null) {
            BluetoothController.taskCancel();
            bluetoothController = null;
        }
    }
}
