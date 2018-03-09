package com.andrea.pcstatus;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.GridLayout;
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
                    .setPositiveButton("Bluetooth", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            ClientManager.startBluetoothClient();
                        }
                    }).setNegativeButton("WiFi", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    wifiIpRequest();
                }
            }).setCancelable(false).create();
        } else if (alertRequest == REQUEST_ERROR_BLUETOOTH) {
            alertDialog = new AlertDialog.Builder(mainController.getMainActivity())
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
            }).setCancelable(false).create();
        } else if (alertRequest == REQUEST_ERROR) {
            alertDialog = new AlertDialog.Builder(mainController.getMainActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //do nothing
                }
            }).setCancelable(false).create();
        }
        if (alertDialog != null) {
            alertDialog.show();
        }
    }

   /* private static void wifiIpRequest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainController.getMainActivity());
        builder.setTitle("IP address");
        builder.setMessage("Insert ip address showed in top bar on PCstatus on your PC");

        // Set up the input
        final EditText input = new EditText(mainController.getMainActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
       // if (WiFiConnectionControllerOld.isIpSaved()) {
            //input.setText(SingletonModel.getInstance().getIp());
            input.setText("");
        //}
       /* CheckBox checkBox = new CheckBox(mainController.getMainActivity());
        checkBox.setText(R.string.latest_ip_address_saved);

        checkBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            if (!SingletonModel.getInstance().getLatestIp().equals(""))
                                input.setText(SingletonModel.getInstance().getLatestIp());
                    }
                }
        );*/
       /* EditText portInput = new EditText(mainController.getMainActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        TextView ipTextView = new TextView(mainController.getApplicationContext());
        ipTextView.setText("IP:");
        TextView portTextView = new TextView(mainController.getApplicationContext());
        portTextView.setText("Port:");

        GridLayout gridLayout = new GridLayout(mainController.getApplicationContext());
        gridLayout.setColumnCount(2);
        gridLayout.addView(ipTextView);
        gridLayout.addView(input);
        gridLayout.addView(portTextView);
        gridLayout.addView(portInput);

        builder.setCancelable(false);
        builder.setView(gridLayout);

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
    }*/

    private static void wifiIpRequest() {
        new ScanController(mainController);
    }
}
