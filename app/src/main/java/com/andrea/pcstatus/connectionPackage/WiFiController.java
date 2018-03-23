package com.andrea.pcstatus.connectionPackage;

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
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_WIFI_OR_BLUETOOTH;


/**
 * Created by andre on 09/03/2018.
 */

public class WiFiController {
    private static final String TAG = "WiFiController";
    private static MainController mainController;
    private static TimerTask task;
    private static Timer timer;
    private static String url;
    private static AsyncTask<Void, Integer, String> threadReciveMessage = null;

    public WiFiController(String url, MainController mainController) {
        WiFiController.mainController = mainController;
        WiFiController.url = url;
        new VerifyIP().execute();
    }

    private static class VerifyIP extends AsyncTask<Void, Integer, String> {
        private boolean connect = false;
        private BufferedReader bufferedReader = null;

        @Override
        protected String doInBackground(Void... voids) {
            publishProgress();
            URLConnection urlConnection;
            try {
                String url = WiFiController.url;
                Log.d(TAG, "try url: " + url);
                urlConnection = new URL(url).openConnection();

                if (urlConnection != null) {
                    Log.w(TAG, urlConnection.getReadTimeout() + " " + urlConnection.getConnectTimeout());
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!connect) {
                            this.cancel(true);
                        }
                    }).start();
                    bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    connect = true;
                }
                Log.d(TAG, mainController.getMainActivity().getString(R.string.connected_to) + url);
                SingletonModel.getInstance().setUrl(url);
                return url;
            } catch (IOException e) {
                this.cancel(true);
                return "";
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            createDialog();
        }

        @Override
        protected void onCancelled() {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            hideDialog();
            SingletonModel.getInstance().setUrl("");
            mainController.setConnectionFlag(false);
            AlertDialogManager.alertBox(mainController.getMainActivity().getString(R.string.error),
                    mainController.getMainActivity().getString(R.string.pc_not_found), REQUEST_WIFI_OR_BLUETOOTH);
        }

        @Override
        protected void onPostExecute(String objects) {
            if (!objects.isEmpty()) {
                mainController.setConnectionFlag(true);
                scheduleTask();
                hideDialog();
            }
        }
    }

    private static void createDialog() {
        AlertDialogManager.progressBarDialog(mainController.getMainActivity().getString(R.string.searching_wifi_server));
    }

    private static void hideDialog() {
        AlertDialogManager.hideProgressBarDialog();
    }

    public static void taskCancel() {
        if (task != null) {
            timer.cancel();
            task.cancel();
            timer = null;
            task = null;
        }
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
                            if (threadReciveMessage == null || (threadReciveMessage.getStatus() == AsyncTask.Status.FINISHED)) {
                                threadReciveMessage = new GetStats().execute();
                                //Log.d(TAG, "task eseguito nel thread " + Thread.currentThread().getName());
                            } else {
                                //Log.d(TAG, "task in attesa di input nel thread " + Thread.currentThread().getName());
                            }
                        }
                    });
                }
            };
            timer.schedule(task, 0, 1500); //it executes this every 1,5 seconds
        }
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
            taskCancel();
            mainController.setConnectionFlag(false);
            AlertDialogManager.alertBox(mainController.getMainActivity().getString(R.string.error_connection_lost),
                    mainController.getMainActivity().getString(R.string.connection_lost),
                    REQUEST_WIFI_OR_BLUETOOTH);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            createDialog();
        }

        @Override
        protected void onPostExecute(String o) {
            SingletonBatteryStatus.getInstance().setJsonStr(o);
        }
    }
}
