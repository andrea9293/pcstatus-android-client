package com.andrea.pcstatus;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Observable;

/**
 * Created by andre on 26/07/2017.
 */

class SingletonModel extends Observable {
    SharedPreferences sharedPreferences;
    private static final SingletonModel ourInstance = new SingletonModel();

    static SingletonModel getInstance() {
        return ourInstance;
    }

    private SingletonModel() {
    }

    private static final String PREFS_NAME = "MyPrefsFile";
    private String content;

    /*public void setContent(String content) throws JSONException {
        setChanged();
        notifyObservers(new JSONObject(content).getString("content"));
    }*/

    public String getContent() {
        return content;
    }

    public void setSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);
    }

    public void observerAdd(MainActivity mainActivity) {
        addObserver(mainActivity);
    }

    public void setIp(String ip) {
        String prefServerIp = "prefServerIp";
        sharedPreferences.edit().putString(prefServerIp, ip).apply();
    }

    public String getIp() {
        String prefServerIp = "prefServerIp";
        return sharedPreferences.getString(prefServerIp, "");
    }

    public void setBluetoothAddress(String mac) {
        String prefBluetoothAddress= "prefBluetoothAddress";
        sharedPreferences.edit().putString(prefBluetoothAddress, mac).apply();
    }

    public String getBluetoothAddress(){
        String prefBluetoothAddress= "prefBluetoothAddress";
        return sharedPreferences.getString(prefBluetoothAddress, "");
    }
}
