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
import com.andrea.pcstatus.R;
import com.andrea.pcstatus.SingletonBatteryStatus;
import com.andrea.pcstatus.SingletonModel;

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

import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_ERROR;
import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_WIFI_OR_BLUETOOTH;

/**
 * Created by andre on 14/03/2018.
 *
 */

public class BluetoothController {
    private static final String TAG = "BluetoothConnection";
    private static MainController mainController;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static BluetoothAdapter btAdapter = null;
    private static boolean firstBoot;
    private static ProgressDialog dialog;
    private static BluetoothSocket btSocket = null;
    private static TimerTask task;
    private static Timer timer;
    private static int counter;
    private static AsyncTask<String, Integer, String> threadReciveMessageBluetooth = null;
    //private static boolean read;


    public BluetoothController(MainController mainController) {
        BluetoothController.mainController = mainController;
        firstBoot = true;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "attivazione client bluetooth");
        // Check for Bluetooth support and then check to make sure it is turned on // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            AlertDialogManager.alertBox(mainController.getMainActivity().getString(R.string.error),
                    mainController.getMainActivity().getString(R.string.bluetooth_not_supported), REQUEST_ERROR);
        } else {
            if (!btAdapter.isEnabled()) {
                mainController.enableBluetooth();
            } else {
                new FirstConnection().execute();
            }
        }
    }

    private static void createDialog() {
        AlertDialogManager.progressBarDialog(mainController.getMainActivity().getString(R.string.searching_bluetooth_server));
    }

    private static void hideDialog() {
        AlertDialogManager.hideProgressBarDialog();
    }

    private static class FirstConnection extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... voids) {
            publishProgress();
            String address = getAddress();
            if (address != null) {
                return address;
            } else {
                this.cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            hideDialog();
            taskCancel();
            createErrorDialog();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            createDialog();
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                threadReciveMessageBluetooth = new ReciveMessageBluetooth().execute(s);
            }
        }
    }

    private static String getAddress() {
        List<BluetoothDevice> arrayListPairedBluetoothDevices = new ArrayList<>();

        Set<BluetoothDevice> pairedDevice = btAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            arrayListPairedBluetoothDevices.addAll(pairedDevice);
        }

        for (int i = 0; i < arrayListPairedBluetoothDevices.size(); i++) {
            // Establish the connection.  This will block until it connects.
            try {
                BluetoothDevice device = btAdapter.getRemoteDevice(arrayListPairedBluetoothDevices.get(i).getAddress());
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

                // Discovery is resource intensive.  Make sure it isn't going on
                // when you attempt to connect and pass your message.
                btAdapter.cancelDiscovery();
                btSocket.connect();
                Log.d(TAG, "connesso a " + device.getName());
                SingletonModel.getInstance().setBluetoothAddress(device.getAddress());
                return device.getAddress();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    private static class ReciveMessageBluetooth extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String address = strings[0];
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            try {
                Log.d(TAG, "connesso a " + device.getName());
                InputStream inStream;
                inStream = btSocket.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                long startTime = System.currentTimeMillis();
               // read = false;
                /*Thread timerThread = new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!read) {
                        threadReciveMessageBluetooth.cancel(true);
                    }
                });
                timerThread.start();*/
                String messageFromServer = bReader.readLine();
               // read = true;
                //timerThread.interrupt();
                long stopTime = System.currentTimeMillis();
                Log.e("time to execute code", stopTime - startTime + ""); //circa 2.7 secondi

                // Create a data stream so we can talk to server.
                String message = "Message from phone\n";
                OutputStream outStream = btSocket.getOutputStream();
                byte[] msgBuffer = message.getBytes();
                outStream.write(msgBuffer);

                return messageFromServer;
            } catch (IOException e) {
                e.printStackTrace();
                threadReciveMessageBluetooth.cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            hideDialog();
            taskCancel();
            createErrorDialog();
        }

        @Override
        protected void onPostExecute(String s) {
            if (firstBoot) {
                hideDialog();
            }

            if (s != null && !s.isEmpty()) {
                SingletonBatteryStatus.getInstance().setJsonStr(s);
                mainController.setConnectionFlag(true);
                if (firstBoot) {
                    scheduleTask();
                }
                firstBoot = false;
            }
        }
    }

    private static void scheduleTask() {
        final Handler handler = new Handler();
        counter = 0;
        timer = new Timer();
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    handler.post(() -> {
                        if (threadReciveMessageBluetooth == null || threadReciveMessageBluetooth.getStatus() == AsyncTask.Status.FINISHED) {
                            counter = 0;
                            threadReciveMessageBluetooth = new ReciveMessageBluetooth().execute(SingletonModel.getInstance().getBluetoothAddress());
                        } else {
                            if (counter < 10) {
                                Log.d(TAG, "task in attesa di input");
                                counter++;
                            } else {
                                taskCancel();
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
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createErrorDialog() {
        SingletonModel.getInstance().setBluetoothAddress("");
        mainController.setConnectionFlag(false);
        AlertDialogManager.alertBox(mainController.getMainActivity().getString(R.string.error),
                mainController.getMainActivity().getString(R.string.bluetooth_server_not_found), REQUEST_WIFI_OR_BLUETOOTH);
    }
}
