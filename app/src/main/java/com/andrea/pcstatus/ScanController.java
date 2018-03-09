package com.andrea.pcstatus;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.LinearLayout;

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

    ScanController(MainController mainController) {
        this.mainController = mainController;
        checkWifiEnabled();
    }

    private void checkWifiEnabled() {
        WifiManager wifi = (WifiManager) mainController.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null && wifi.isWifiEnabled()) {
            createScannerView();
        } else {
            createWifiError();
        }
    }

    private void createWifiError() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainController.getMainActivity());
        builder.setTitle("Errore - Il WiFi risulta spento")
                .setMessage("Per utilizzare PCstatus è necessaria una connessione alla stessa rete WiFi del PC oppure una connessione bluetooth con il computer\n\nAccendere il Wifi e riprovare oppure utilizzare il bluetooth?")
                .setCancelable(false)
                .setPositiveButton("WiFi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WifiManager wifi = (WifiManager) mainController.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wifi != null) {
                            wifi.setWifiEnabled(true);
                        }
                        checkWifiEnabled();
                    }
                }).setNegativeButton("Bluetooth", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ClientManager.taskCancel();
                ClientManager.startBluetoothClient();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createScannerView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainController.getMainActivity());
        builder.setTitle("Scan Qrcode in PC-status on your PC");
        mScannerView = new ZXingScannerView(mainController.getApplicationContext());   // Programmatically initialize the scanner view
        LinearLayout linearLayout = new LinearLayout(mainController.getApplicationContext());
        linearLayout.addView(mScannerView);
        builder.setView(linearLayout);                // Set the scanner view as the content view
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

            }
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
