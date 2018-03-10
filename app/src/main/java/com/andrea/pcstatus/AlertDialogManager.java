package com.andrea.pcstatus;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_ERROR;
import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_ERROR_BLUETOOTH;
import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_WIFI_OR_BLUETOOTH;

/**
 * Created by andre on 15/11/2017.
 */

public class AlertDialogManager {
    static MainController mainController;

    public enum AlertRequest {
        REQUEST_ERROR_BLUETOOTH,
        REQUEST_WIFI_OR_BLUETOOTH,
        REQUEST_ERROR
    }

    public static void alertBox(String title, String message, AlertRequest alertRequest) {
        AlertDialog alertDialog = null;
        if (alertRequest == REQUEST_WIFI_OR_BLUETOOTH) {
            alertDialog = new AlertDialog.Builder(mainController.getMainActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Bluetooth", (arg0, arg1) ->
                            ClientManager.startBluetoothClient()).setNegativeButton("WiFi", (dialogInterface, i) ->
                            wifiIpRequest()).setCancelable(false).create();
        } else if (alertRequest == REQUEST_ERROR_BLUETOOTH) {
            alertDialog = new AlertDialog.Builder(mainController.getMainActivity())
                    .setTitle(title)
                    .setMessage(message + " " + mainController.getMainActivity().getString(R.string.press_ok_try_again))
                    .setPositiveButton("OK", (arg0, arg1) ->
                            ClientManager.startBluetoothClient()).setNegativeButton(mainController.getMainActivity().getString(R.string.cancel_try_wifi),
                            (dialogInterface, i) ->
                            wifiIpRequest()).setCancelable(false).create();
        } else if (alertRequest == REQUEST_ERROR) {
            alertDialog = new AlertDialog.Builder(mainController.getMainActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", (arg0, arg1) -> {

                    }).setNegativeButton(mainController.getMainActivity().getString(R.string.cancel), (dialogInterface, i) -> {
                        //do nothing
                    }).setCancelable(false).create();
        }
        if (alertDialog != null) {
            alertDialog.show();
        }
    }

    public static void aboutApplication(){
        LinearLayout linearLayout = new LinearLayout(mainController.getApplicationContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(70, 70,70,70);

        TextView about = new TextView(mainController.getApplicationContext());
        about.setText(mainController.getMainActivity().getString(R.string.pc_status_about));
        about.setTextSize(16f);

        TextView chart = new TextView(mainController.getApplicationContext());
        chart.setText("MPAndroidChart");
        chart.setTextColor(Color.BLUE);
        chart.setPaintFlags(chart.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        chart.setOnClickListener(v ->
                mainController.getMainActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/PhilJay/MPAndroidChart"))));
        chart.setTextSize(16f);

        TextView barcodescanner = new TextView(mainController.getApplicationContext());
        barcodescanner.setText("Dm7-Barcodescanner");
        barcodescanner.setTextColor(Color.BLUE);
        barcodescanner.setPaintFlags(barcodescanner.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        barcodescanner.setOnClickListener(v ->
                mainController.getMainActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dm77/barcodescanner"))));
        barcodescanner.setTextSize(16f);

        TextView icons = new TextView(mainController.getApplicationContext());
        icons.setText("Icons8");
        icons.setTextColor(Color.BLUE);
        icons.setPaintFlags(icons.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        icons.setOnClickListener(v ->
                mainController.getMainActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://icons8.com"))));
        icons.setTextSize(16f);

        linearLayout.addView(about);
        linearLayout.addView(chart);
        linearLayout.addView(barcodescanner);
        linearLayout.addView(icons);

        AlertDialog alertDialog = new AlertDialog.Builder(mainController.getMainActivity())
                .setTitle(mainController.getMainActivity().getString(R.string.about))
                .setView(linearLayout)
                .setPositiveButton("OK", (dialog, which) -> {
                    // hide dialog
                }).create();

        alertDialog.show();
    }

    private static void wifiIpRequest() {
        new ScanController(mainController);
    }
}
