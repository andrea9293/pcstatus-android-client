package com.andrea.pcstatus.connectionPackage;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.andrea.pcstatus.AlertDialogManager;
import com.andrea.pcstatus.MainController;
import com.andrea.pcstatus.SingletonBatteryStatus;
import com.andrea.pcstatus.SingletonModel;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_BASIC;
import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_WIFI_OR_BLUETOOTH;

/**
 * Created by andre on 29/07/2017.
 *
 */

public class BluetoothConnectionController {
    private static final String TAG = "BluetoothConnection";
    private static BluetoothAdapter btAdapter = null;
    private static BluetoothSocket btSocket = null;
    // Well known SPP UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private static MainActivity mainActivity;
    private static MainController mainController;
    private static TimerTask task;
    private AsyncTask<Void, Integer, String> threadReciveMessage = null;
    private static boolean firstBoot = true;
    private static Timer timer;
    private int counter;

    public BluetoothConnectionController(MainController mainController) {
        BluetoothConnectionController.mainController = mainController;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "attivazione client bluetooth");
        // Check for Bluetooth support and then check to make sure it is turned on // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            AlertDialogManager.alertBox("Fatal Error", "Bluetooth Not supported. Aborting.", REQUEST_BASIC);
        } else {
            if (!btAdapter.isEnabled()) {
                mainController.enableBluetooth();
            } else {
                scheduleTask();
            }
        }
    }

    private static class ReciveMessage extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... voids) {
            if (firstBoot) {
                publishProgress();
            }
            if (!isAddressSaved()) {
                Log.d(TAG, "non è salvato");
                getAddress();
                return reciveMessage(this);
            } else {
                if (firstBoot){
                    Log.d(TAG, "è salvato e sono al primo avvio");
                    return reciveMessage(this, SingletonModel.getInstance().getBluetoothAddress());
                }
                else {
                    Log.d(TAG, "è salvato e sono agli avii successivi");
                    return reciveMessage(this);
                }
            }
        }

        @Override
        protected void onCancelled() {
            connectionError();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            createDialog();
        }

        @Override
        protected void onPostExecute(String strings) {
            if (strings != null) {
                try {
                    SingletonBatteryStatus.getInstance().setJsonStr(strings);
                    hideDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideDialog();
                }
                //scheduleTask();
                if (firstBoot)
                    hideDialog();
                firstBoot = false;
                mainController.setConnectionFlag(true);
            }
        }
    }

    private static String reciveMessage(ReciveMessage reciveMessage, String address) {

            Log.d(TAG, "in reciveMessage stampo " + address);
            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                AlertDialogManager.alertBox("1Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".", REQUEST_BASIC);
            }

            // Discovery is resource intensive.  Make sure it isn't going on
            // when you attempt to connect and pass your message.
            btAdapter.cancelDiscovery();

            // Establish the connection.  This will block until it connects.
            try {
                btSocket.connect();
                Log.d(TAG, "connesso a " + device.getName());
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    AlertDialogManager.alertBox("2Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".", REQUEST_BASIC);
                }
            }

        final InputStream inStream;
        final String[] jStr = {null};
        try {
            inStream = btSocket.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            try {
                long startTime = System.currentTimeMillis();
                jStr[0] = bReader.readLine();
                long stopTime = System.currentTimeMillis();
                Log.e("time to execute code", stopTime - startTime + ""); //circa 2.7 secondi
            } catch (IOException e) {
                taskCancel();
                reciveMessage.cancel(true);
                e.printStackTrace();
            }
//todo da vedere l'indirizzo memorizzato
            // Create a data stream so we can talk to server.
            String message = "Message from phone\n";
            OutputStream outStream = btSocket.getOutputStream();
            byte[] msgBuffer = message.getBytes();
            outStream.write(msgBuffer);

        } catch (IOException e) {
            if (address.equals("00:00:00:00:00:00"))
                Log.d(TAG, "Check that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n");
            e.printStackTrace();
            reciveMessage.cancel(true);
        }
        return jStr[0];
    }

    private static String reciveMessage(ReciveMessage reciveMessage) {
        final InputStream inStream;
        final String[] jStr = {null};
        try {
            inStream = btSocket.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            try {
                long startTime = System.currentTimeMillis();
                jStr[0] = bReader.readLine();
                long stopTime = System.currentTimeMillis();
                Log.e("time to execute code", stopTime - startTime + ""); //circa 2.7 secondi
            } catch (IOException e) {
                taskCancel();
                reciveMessage.cancel(true);
                e.printStackTrace();
            }
//todo da vedere l'indirizzo memorizzato
            // Create a data stream so we can talk to server.
            String message = "Message from phone\n";
            OutputStream outStream = btSocket.getOutputStream();
            byte[] msgBuffer = message.getBytes();
            outStream.write(msgBuffer);

        } catch (IOException e) {
            e.printStackTrace();
            reciveMessage.cancel(true);
        }
        return jStr[0];
    }

    private static void stopConnection() {
        try {
            btSocket.close();
        } catch (IOException e2) {
            AlertDialogManager.alertBox("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".", REQUEST_BASIC);
        }
    }

    private static void getAddress() {
        String ris = null;
        List<BluetoothDevice> arrayListPairedBluetoothDevices = new ArrayList<>();

        Set<BluetoothDevice> pairedDevice = btAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            arrayListPairedBluetoothDevices.addAll(pairedDevice);
        }

        for (int i = 0; i < arrayListPairedBluetoothDevices.size(); i++) {
            BluetoothDevice device = btAdapter.getRemoteDevice(arrayListPairedBluetoothDevices.get(i).getAddress());

            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                AlertDialogManager.alertBox("1Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".", REQUEST_BASIC);
            }

            // Discovery is resource intensive.  Make sure it isn't going on
            // when you attempt to connect and pass your message.
            btAdapter.cancelDiscovery();

            // Establish the connection.  This will block until it connects.
            try {
                btSocket.connect();
                Log.d(TAG, "connesso a " + device.getName());
                ris = device.getAddress();
                break;
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    AlertDialogManager.alertBox("2Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".", REQUEST_BASIC);
                }
            }
        }
        Log.d(TAG, "stampo ris " + ris);
        SingletonModel.getInstance().setBluetoothAddress(ris);
    }

    private static ProgressDialog dialog;

    private static void createDialog() {
        dialog = ProgressDialog.show(mainController.getMainActivity(), "",
                "Searching for bluetooth connection with server" + ". Please wait...", true);
    }

    private static void hideDialog() {
        dialog.hide();
    }

    public void scheduleTask() {
        final Handler handler = new Handler();
        counter = 0;
        timer = new Timer();
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            if (threadReciveMessage == null || threadReciveMessage.getStatus() == AsyncTask.Status.FINISHED) {
                                counter = 0;
                                threadReciveMessage = new ReciveMessage().execute();
                            } else {
                                if(counter<10) {
                                    Log.d(TAG, "task in attesa di input");
                                    counter++;
                                }else {
                                    connectionError();
                                }
                            }
                        }
                    });
                }
            };
            timer.schedule(task, 0, 1500); //it executes this every 1 minute
        }
    }

    public static void taskCancel() {
        if (task != null) {
            task.cancel();
            timer.cancel();
            task = null;
            timer = null;
        }
    }

    private static Boolean isAddressSaved() {
        String bluetoothAddress = SingletonModel.getInstance().getBluetoothAddress();
        Log.d(TAG, "stampo in isAddres " + SingletonModel.getInstance().getBluetoothAddress() + " " + bluetoothAddress);
        return !bluetoothAddress.equals("");
    }

    private static void connectionError(){
        taskCancel();
        stopConnection();
        SingletonModel.getInstance().setBluetoothAddress("");
        if (firstBoot) {
            hideDialog();
        }
        AlertDialogManager.alertBox("ERRORE", "Server Bluetooth non trovato", REQUEST_WIFI_OR_BLUETOOTH);
        mainController.setConnectionFlag(false);
    }
}
