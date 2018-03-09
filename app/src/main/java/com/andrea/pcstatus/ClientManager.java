package com.andrea.pcstatus;

import com.andrea.pcstatus.connectionPackage.BluetoothConnectionController;
import com.andrea.pcstatus.connectionPackage.WiFiController;

/**
 * Created by andre on 13/11/2017.
 *
 */

class ClientManager {
    private static WiFiController wiFiController;
    private static BluetoothConnectionController bluetoothConnectionController;
    static MainController mainController;

    static void startWifiClient(String ip) {
        taskCancel();
        wiFiController = new WiFiController(ip, mainController);
    }

    static void startBluetoothClient() {
        taskCancel();
        bluetoothConnectionController = new BluetoothConnectionController(mainController);
    }

    static void taskCancel() {
        if (wiFiController != null) {
            WiFiController.taskCancel();
            wiFiController = null;
        }
        if (bluetoothConnectionController != null) {
            BluetoothConnectionController.taskCancel();
            bluetoothConnectionController = null;
        }
    }
}
