package com.andrea.pcstatus.connectionPackage;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.LinearLayout;

import com.andrea.pcstatus.ClientManager;
import com.andrea.pcstatus.MainController;
import com.andrea.pcstatus.R;
import com.andrea.pcstatus.SingletonModel;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by andre on 09/03/2018.
 */

public class ScanController implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private String TAG = "ScanController";
    private AlertDialog alertDialog;
    private MainController mainController;

    public ScanController(MainController mainController) {
        this.mainController = mainController;
        checkWifiEnabled();
    }

    private void checkWifiEnabled() {
        WifiManager wifi = (WifiManager) mainController.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null && wifi.isWifiEnabled()) {
            if (SingletonModel.getInstance().getUrl().isEmpty()) {
                createScannerView();
            }else {
                ClientManager.startWifiClient(SingletonModel.getInstance().getUrl());
            }
        } else {
            createWifiError();
        }
    }

    private void createWifiError() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainController.getMainActivity());
        builder.setTitle(mainController.getMainActivity().getString(R.string.error_wifi_off))
                .setMessage(mainController.getMainActivity().getString(R.string.error_wifi_off_message))
                .setCancelable(false)
                .setPositiveButton("WiFi", (dialog, id) -> {
                    WifiManager wifi = (WifiManager) mainController.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifi != null) {
                        wifi.setWifiEnabled(true);
                        while (!wifi.isWifiEnabled()){
                            Log.d(TAG, "waiting for wifi activation");
                        }
                    }
                    checkWifiEnabled();
                }).setNegativeButton("Bluetooth", (dialogInterface, i) -> {
                    ClientManager.taskCancel();
                    ClientManager.startBluetoothClient();
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createScannerView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainController.getMainActivity());
        builder.setTitle(mainController.getMainActivity().getString(R.string.scan_qr_code));
        mScannerView = new ZXingScannerView(mainController.getApplicationContext());   // Programmatically initialize the scanner view
        LinearLayout linearLayout = new LinearLayout(mainController.getApplicationContext());
        linearLayout.addView(mScannerView);
        builder.setView(linearLayout);                // Set the scanner view as the content view
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", (arg0, arg1) -> {
            // hide alert
        });

        alertDialog = builder.create();
        alertDialog.show();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        Log.v(TAG, result.getText()); // Prints scan results
        ClientManager.startWifiClient(result.getText());
        mScannerView.stopCamera();
        alertDialog.dismiss();
        Log.v(TAG, result.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
    }
}
