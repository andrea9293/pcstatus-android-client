package com.andrea.pcstatus.connectionPackage;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.andrea.pcstatus.AlertDialogManager;
import com.andrea.pcstatus.ClientManager;
import com.andrea.pcstatus.MainActivity;
import com.andrea.pcstatus.SingletonBatteryStatus;
import com.andrea.pcstatus.SingletonModel;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_WIFI_OR_BLUETOOTH;

/**
 * Created by andrea on 26/07/2017.
 */

public class WiFiConnectionController {

    private static final String TAG = "WiFiController";
    @SuppressLint("StaticFieldLeak") //todo vedere bene cosa fa
    private static MainActivity mainActivity;
    private static TimerTask task;
    private static boolean firstScan = true;
    private static Timer timer;
    private static String ip;
    private static AsyncTask<Void, Integer, String> threadReciveMessage = null;

    public WiFiConnectionController(MainActivity mainActivity, String ip) {
        WiFiConnectionController.mainActivity = mainActivity;
        WiFiConnectionController.ip = ip;
        checkWifiForStats();
    }

    private static class GetIp extends AsyncTask<Void, Integer, String> {

        static final int lower = 8080;
        static final int upper = 8091;
        static final int timeout = 200;

        @Override
        protected String doInBackground(Void... voids) {

            if (isIpSaved()) {
                return SingletonModel.getInstance().getUrl();
            } else {
                publishProgress();
                for (int i = lower; i < upper; i++) {
                    URLConnection urlConnection;
                    try {
                        String url = "http://" + ip + ":"+ i + "/greeting";
                        Log.d(TAG, "provo con" + url);
                        urlConnection = new URL(url).openConnection();
                        if (urlConnection != null) {
                            urlConnection.setConnectTimeout(timeout);
                            new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        }
                        Log.d(TAG, "riuscito con " + url);
                        SingletonModel.getInstance().setUrl(url);
                        SingletonModel.getInstance().setIp(ip);
                        SingletonModel.getInstance().setLatestIp(ip);
                        return url;
                    } catch (IOException e) {
                        if (i == 8090)
                            ClientManager.startWifiClient(ip);
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
            SingletonModel.getInstance().setUrl(objects);
            if (!mainActivity.isConnectionFlag()) {
                scheduleTask();
                mainActivity.setConnectionFlag(true);
            }
            hideDialog();
        }

      /*  private String getMyIp() {
            String subnet = "";
            WifiManager wm = (WifiManager) mainActivity.getApplicationContext().getSystemService(WIFI_SERVICE);
            String myIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            String parts[] = myIp.split("\\.");

            for (int i = 0; i < parts.length - 1; i++) {
                subnet += parts[i] + ".";
            }
            return subnet;
        }*/
    }

    private static class GetStats extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... voids) {
            URLConnection urlConnection;
            try {
                urlConnection = new URL(SingletonModel.getInstance().getUrl()).openConnection();
                urlConnection.setConnectTimeout(200);
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
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
            SingletonModel.getInstance().setUrl("");
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


    public static Boolean isIpSaved() {
        String ip = SingletonModel.getInstance().getIp();
        return !ip.equals("");
    }

    private void checkWifiForServer() {
        WifiManager wifi = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null && wifi.isWifiEnabled())
            new GetIp().execute();
        else
            createWifiServerError();
    }

    private void checkWifiForStats() {
        WifiManager wifi = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null && wifi.isWifiEnabled())
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
                        if (wifi != null) {
                            wifi.setWifiEnabled(true);
                        }
                        checkWifiForStats();
                        mainActivity.setConnectionFlag(false);
                    }
                }).setNegativeButton("Bluetooth", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mainActivity.setConnectionFlag(false);
                ClientManager.taskCancel();
                ClientManager.startBluetoothClient();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private static ProgressDialog dialog;

    private static void createDialog(String m) {
        dialog = ProgressDialog.show(mainActivity, "",
                m + ". Please wait...", true);
    }

    private static void hideDialog() {
        dialog.hide();
    }

    private static void scheduleTask() {
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

    public static void taskCancel() {
        if (task != null) {
            timer.cancel();
            task.cancel();
            timer = null;
            task = null;
        }
        mainActivity.setConnectionFlag(false);
    }

    private static void createErrorDialog() {
        AlertDialogManager.alertBox("Non riesco a collegarmi al server", "PCstatus need a connection via WiFi or Bluetooth\n" +
                "What you want to use?", REQUEST_WIFI_OR_BLUETOOTH);

    }
}


