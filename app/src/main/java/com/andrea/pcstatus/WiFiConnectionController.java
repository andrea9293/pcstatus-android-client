package com.andrea.pcstatus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by andrea on 26/07/2017.
 */

class WiFiConnectionController {

    private static final String TAG = "WiFiController";
    private MainActivity mainActivity;
    private TimerTask task;
    private boolean firstScan = true;
    private Timer timer;
    private AsyncTask<Void, Integer, String> threadReciveMessage = null;

    WiFiConnectionController(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        checkWifiForStats();
    }

    private class GetIp extends AsyncTask<Void, Integer, String> {

        static final int lower = 1;
        static final int upper = 255;
        static final int timeout = 200;

        @Override
        protected String doInBackground(Void... voids) {

            if (isIpSaved()) {
                return SingletonModel.getInstance().getIp();
            } else {
                String subnet;
                publishProgress();
                subnet = getMyIp();
                for (int i = lower; i <= upper; i++) {
                    URLConnection prova;
                    try {
                        prova = new URL("http://" + subnet + i + ":8080/greeting").openConnection();
                        if (prova != null) {
                            prova.setConnectTimeout(timeout);
                            BufferedReader in = new BufferedReader(new InputStreamReader(prova.getInputStream()));
                        }
                        Log.d(TAG, "riuscito con " + subnet + i);
                        SingletonModel.getInstance().setIp(subnet + i);
                        return subnet + i;
                    } catch (IOException e) {
                        // e.printStackTrace();
                    }
                }
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            createDialog("Searching for server");
        }

        @Override
        protected void onPostExecute(String objects) {
            SingletonModel.getInstance().setIp(objects);
            if (!mainActivity.isConnectionFlag()) {
                scheduleTask();
                mainActivity.setConnectionFlag(true);
            }
            hideDialog();
        }

        private String getMyIp() {
            String subnet = "";
            WifiManager wm = (WifiManager) mainActivity.getApplicationContext().getSystemService(WIFI_SERVICE);
            String myIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            String parts[] = myIp.split("\\.");

            for (int i = 0; i < parts.length - 1; i++) {
                subnet += parts[i] + ".";
            }
            return subnet;
        }
    }

    private class GetStats extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... voids) {
            URLConnection prova;
            try {
                prova = new URL("http://" + SingletonModel.getInstance().getIp() + ":8080/greeting").openConnection();
                prova.setConnectTimeout(200);
                BufferedReader in = new BufferedReader(new InputStreamReader(prova.getInputStream()));
                String inputLine;
                String ris = null;
                while ((inputLine = in.readLine()) != null)
                    ris = inputLine;
                in.close();
                return ris;
            } catch (IOException e) {
                this.cancel(true);
                Log.w(TAG, "non sono connesso al server");
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            SingletonModel.getInstance().setIp("");
            if (firstScan) {
                new GetIp().execute();
                firstScan = false;
            } else {
                taskCancel();
                createErrorDialog();
                mainActivity.setTextView("Insert the Ip address showed on your PC");
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            createDialog("Connection to sever");
        }

        @Override
        protected void onPostExecute(String o) {
            try {
                SingletonBatteryStatus.getInstance().setJsonStr(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!mainActivity.isConnectionFlag()) {
                scheduleTask();
                mainActivity.setConnectionFlag(true);
            }
        }
    }


    private Boolean isIpSaved() {
        String ip = SingletonModel.getInstance().getIp();
        return !ip.equals("");
    }

    private void checkWifiForServer() {
        WifiManager wifi = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled())
            new GetIp().execute();
        else
            createWifiServerError();
    }

    private void checkWifiForStats() {
        WifiManager wifi = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled())
            new GetStats().execute();
        else
            createWifiStatsError();
    }

    private void createWifiServerError() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("Errore")
                .setMessage("Il WiFi risulta spento. Accendere il Wifi Per utilizzare BatteryStatus")
                .setCancelable(false)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        checkWifiForServer();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createWifiStatsError() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("Errore - Il WiFi risulta spento")
                .setMessage("Per utilizzare PCstatus è necessaria una connessione alla stessa rete WiFi del PC oppure una connessione bluetooth con il computer\n\nAccendere il Wifi e riprovare oppure utilizzare il bluetooth?")
                .setCancelable(false)
                .setPositiveButton("WiFi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WifiManager wifi = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        wifi.setWifiEnabled(true);
                        checkWifiForStats();
                        mainActivity.setConnectionFlag(false);
                    }
                }).setNegativeButton("Bluetooth", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mainActivity.setConnectionFlag(false);
                mainActivity.taskCancel();
                mainActivity.startBluetoothClient();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private ProgressDialog dialog;

    private void createDialog(String m) {
        dialog = ProgressDialog.show(mainActivity, "",
                m + ". Please wait...", true);
    }

    private void hideDialog() {
        dialog.hide();
    }

    private void scheduleTask() {
        final Handler handler = new Handler();
        timer = new Timer();
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            if (threadReciveMessage == null || threadReciveMessage.getStatus() == AsyncTask.Status.FINISHED) {
                                threadReciveMessage = new GetStats().execute();
                                Log.d(TAG, "task eseguito ");
                            } else {
                                Log.d(TAG, "task in attesa di input");
                            }
                        }
                    });
                }
            };
            timer.schedule(task, 0, 1500); //it executes this every 1,5 seconds
        }
    }

    public void taskCancel() {
        if (task != null) {
            timer.cancel();
            task.cancel();
            timer = null;
            task = null;
        }
    }

    private void createErrorDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("Non riesco a collegarmi al server")
                .setMessage("Inserimento dell'IP manualmente o scansione automatica (circa un minuto)?")
                .setCancelable(false)
                .setPositiveButton("Auto", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mainActivity.setConnectionFlag(false);
                        new GetIp().execute();
                    }
                }).setNegativeButton("Try with Bluetooth", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mainActivity.setConnectionFlag(false);
                mainActivity.startBluetoothClient();
            }
        }).setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
    }
}
