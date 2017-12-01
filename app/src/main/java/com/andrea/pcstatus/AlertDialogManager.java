package com.andrea.pcstatus;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.andrea.pcstatus.connectionPackage.WiFiConnectionController;

import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_BASIC;
import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_ERROR_BLUETOOTH;
import static com.andrea.pcstatus.AlertDialogManager.AlertRequest.REQUEST_WIFI_OR_BLUETOOTH;

/**
 * Created by andre on 15/11/2017.
 */

public class AlertDialogManager {
    public static MainActivity mainActivity;

    public enum AlertRequest {
        REQUEST_ERROR_BLUETOOTH,
        REQUEST_WIFI_OR_BLUETOOTH,
        REQUEST_BASIC
    }

    public static void alertBox(String title, String message, AlertRequest alertRequest) {

        if (alertRequest == REQUEST_WIFI_OR_BLUETOOTH) {
            new AlertDialog.Builder(mainActivity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Bluetooth", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            ClientManager.startBluetoothClient();
                        }
                    }).setNegativeButton("WiFi", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    wifiIpRequest();
                }
            }).show();
        } else if (alertRequest == REQUEST_ERROR_BLUETOOTH) {
            new AlertDialog.Builder(mainActivity)
                    .setTitle(title)
                    .setMessage(message + " Press OK to try again.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            ClientManager.startBluetoothClient();
                        }
                    }).setNegativeButton("CANCEL and try with WiFi", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    wifiIpRequest();
                }
            }).show();
        } else if (alertRequest == REQUEST_BASIC) {
            new AlertDialog.Builder(mainActivity)
                    .setTitle(title)
                    .setMessage(message + " Press OK to try again.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //do nothing
                }
            }).show();
        }

    }

    private static void wifiIpRequest() {
        //final String[] m_Text = {""};
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("IP address");
        builder.setMessage("Insert ip address showed in top bar on PCstatus on your PC");

        // Set up the input
        final EditText input = new EditText(mainActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        if (WiFiConnectionController.isIpSaved())
            input.setText(SingletonModel.getInstance().getIp());

        CheckBox checkBox = new CheckBox(mainActivity);
        checkBox.setText(R.string.latest_ip_address_saved);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    if(isChecked)
                                                        if (!SingletonModel.getInstance().getLatestIp().equals(""))
                                                            input.setText(SingletonModel.getInstance().getLatestIp());
                                                }
                                            }
        );

        LinearLayout layout = new LinearLayout(mainActivity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
       // LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layout.addView(input);
        layout.addView(checkBox);

        builder.setCancelable(false);
        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClientManager.startWifiClient(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
