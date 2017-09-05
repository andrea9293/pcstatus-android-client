package com.andrea.pcstatus;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

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

/**
 * Created by andre on 29/07/2017.
 */

public class BluetoothConnectionController {
    private static final String TAG = "BluetoothConnection";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    // Well known SPP UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String address = null;
    private MainActivity mainActivity;
    private TimerTask task;
    private boolean taskHasStarted = false;
    private AsyncTask<Void, Integer, String> threadReciveMessage = null;
    private Timer timer;
    private boolean firstBoot = true;

    BluetoothConnectionController(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "attivazione client bluetooth");
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            mainActivity.alertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (!btAdapter.isEnabled()) {
                mainActivity.enableBluetooth(btAdapter);
            }else {
               //scheduleTask();
                getStatFromServer();
            }
        }
    }

    private class ReciveMessage extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... voids) {
            if (firstBoot)
                publishProgress();
            if (address == null) {
                address = getAddress();
                Log.d(TAG, "64| stampo address " + address);
                return reciveMessage(this, address);
            } else {
                String prova = reciveMessage(this, address);
                Log.d(TAG, "68| stampo il messaggio " + prova);

                return prova;
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "78| sto cancellando il task");
            taskCancel();
            stopConnection();
            if(firstBoot)
                hideDialog();
            mainActivity.alertBox("ERRORE", "Server Bluetooth non trovato", BluetoothConnectionController.this);
            //new BluetoothConnection().execute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            createDialog("Searching for bluetooth connection with server");
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
            }
        }
    }

    private class BluetoothConnection extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            publishProgress();
            if (address == null)
                address = getAddress();
            connect(address);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            createDialog("Searching for a fast bluetooth connection with server");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            hideDialog();
            //scheduleTask();
            getStatFromServer();
        }
    }

    public void bluetoothConnectionExecute(){
        new BluetoothConnection().execute();
    }

    private String reciveMessage(ReciveMessage reciveMessage, String address) {
        final InputStream inStream;
        final String[] jStr = {null};
        try {
            inStream = btSocket.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            try {
                long startTime = System.currentTimeMillis();
                jStr[0] = bReader.readLine();
                long stopTime = System.currentTimeMillis();
                Log.e("time to execute code", stopTime - startTime + "");
                Thread.currentThread().interrupt();
                Log.d(TAG, "141| appena arrivato " + jStr[0]);
                Thread.currentThread().interrupt(); //todo appena aggiunta, da testare
            } catch (IOException e) {
                taskCancel();
                Log.d(TAG, "145| sto per cancellare ");
                reciveMessage.cancel(true);
                e.printStackTrace();
            }
            Log.d(TAG, "148| jStr " + jStr[0]);

            // Create a data stream so we can talk to server.
            Log.d(TAG, "\n...Invio messaggio al server (PC)...");
            String message = "Ciccia Pallina\n";
            Log.d(TAG, "\n\n...Il messaggio che verrà inviato al server (PC) è: " + message);
            OutputStream outStream = btSocket.getOutputStream();
            byte[] msgBuffer = message.getBytes();
            outStream.write(msgBuffer);

        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
            msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            Log.d(TAG, "168| sono nel catch " + msg);
            //AlertBox("4Fatal Error", msg);
            reciveMessage.cancel(true);
            //Log.d("prova", "il dispositivo non è: " + device.getName());
            e.printStackTrace();
        }
        return jStr[0];
    }

    public void reciveBluetoothMessageExecute() {
        new ReciveMessage().execute();
    }


    public void stopConnection() {
        try {
            btSocket.close();
        } catch (IOException e2) {
            mainActivity.alertBox("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void connect(String address) {
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            mainActivity.alertBox("1Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            btSocket.connect();
            Log.d("prova", "connesso a " + device.getName());
        } catch (IOException e) {
            try {
                Log.d("prova", "connessione non riuscita a " + device.getName());
                btSocket.close();
            } catch (IOException e2) {
                mainActivity.alertBox("2Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }
    }

    private String getAddress() {
        String ris = null;
        List<BluetoothDevice> arrayListPairedBluetoothDevices = new ArrayList<>();


        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager=(BluetoothManager) mainActivity.getSystemService(Context.BLUETOOTH_SERVICE);
            arrayListPairedBluetoothDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.STATE_CONNECTED);
        }else {
            Set<BluetoothDevice> pairedDevice = btAdapter.getBondedDevices();
            if (pairedDevice.size() > 0) {
                for (BluetoothDevice device : pairedDevice) {
                    Log.d(TAG, device.getName() + "\n" + device.getAddress() + "\n");
                    arrayListPairedBluetoothDevices.add(device);
                }
            }
        }*/
        Set<BluetoothDevice> pairedDevice = btAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            for (BluetoothDevice device : pairedDevice) {
                Log.d(TAG, device.getName() + "\n" + device.getAddress() + "\n");
                arrayListPairedBluetoothDevices.add(device);
            }
        }

        for (int i = 0; i < arrayListPairedBluetoothDevices.size(); i++) {
            BluetoothDevice device = btAdapter.getRemoteDevice(arrayListPairedBluetoothDevices.get(i).getAddress());

            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                mainActivity.alertBox("1Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
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
                    Log.d(TAG, "connessione non riuscita a " + device.getName());
                    btSocket.close();
                } catch (IOException e2) {
                    mainActivity.alertBox("2Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                }
            }
        }
        Log.d(TAG, "stampo ris " + ris);
        return ris;
    }

    private ProgressDialog dialog;

    private void createDialog(String m) {
        dialog = ProgressDialog.show(mainActivity, "",
                m + ". Please wait...", true);
    }

    private void hideDialog() {
        dialog.hide();
    }

    public void scheduleTask() {
        final Handler handler = new Handler();
        timer = new Timer();
        Log.d(TAG, "task programmato");
        if (task == null || !taskHasStarted()) {
            task = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            if (threadReciveMessage == null || threadReciveMessage.getStatus() == AsyncTask.Status.FINISHED) {
                                Log.d(TAG, "task eseguito");
                                threadReciveMessage = new ReciveMessage().execute();
                            }else {
                                if(threadReciveMessage == null){
                                    Log.d(TAG, "è null");
                                }else {
                                    if(threadReciveMessage.getStatus() == AsyncTask.Status.FINISHED){
                                        Log.d(TAG, "è finito");
                                    }
                                }

                                Log.d(TAG, "task in attesa di input");
                            }
                        }
                    });
                }
            };
            timer.schedule(task, 0, 1500); //it executes this every 1 minute
        }
    }

    private boolean taskHasStarted() {
        return taskHasStarted;
    }

    private void taskCancel() {
        if (task != null) {
            task.cancel();
            taskHasStarted = false;
        }
    }

    public void getStatFromServer(){
        if (threadReciveMessage == null || threadReciveMessage.getStatus() == AsyncTask.Status.FINISHED) {
            Log.d(TAG, "task eseguito");
            threadReciveMessage = new ReciveMessage().execute();
        }else {
            if(threadReciveMessage == null){
                Log.d(TAG, "è null");
            }else {
                Log.d(TAG, String.valueOf(threadReciveMessage.getStatus()));

            }

            Log.d(TAG, "task in attesa di input");
        }
    }
}
