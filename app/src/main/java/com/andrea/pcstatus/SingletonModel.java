package com.andrea.pcstatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by andre on 26/07/2017.
 */

public class SingletonModel {
    private SharedPreferences sharedPreferences;
    private static final SingletonModel ourInstance = new SingletonModel();

    public static SingletonModel getInstance() {
        return ourInstance;
    }

    private SingletonModel() {
    }

    private static final String PREFS_NAME = "MyPrefsFile";


    public void setSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);
    }
/*
    public void setIp(String ip) {
        String prefServerIp = "prefServerIp";
        sharedPreferences.edit().putString(prefServerIp, ip).apply();
    }

    public String getIp() {
        String prefServerIp = "prefServerIp";
        return sharedPreferences.getString(prefServerIp, "");
    }
*/
    public void setBluetoothAddress(String mac) {
        String prefBluetoothAddress= "prefBluetoothAddress";
        sharedPreferences.edit().putString(prefBluetoothAddress, mac).apply();
    }

    public String getBluetoothAddress(){
        String prefBluetoothAddress= "prefBluetoothAddress";
        return sharedPreferences.getString(prefBluetoothAddress, "");
    }

    public void setUrl(String url) {
        String prefServerUrl = "prefServerUrl";
        sharedPreferences.edit().putString(prefServerUrl, url).apply();
    }

    public String getUrl(){
        String prefServerUrl = "prefServerUrl";
        return sharedPreferences.getString(prefServerUrl, "");
    }

    public void setLatestIp(String ip) {
        String prefServerLatestIp = "prefServerLatestIp";
        Log.d("singletonRoba", ip);
        sharedPreferences.edit().putString(prefServerLatestIp, ip).apply();
    }

    public String getLatestIp() {
        String prefServerLatestIp = "prefServerLatestIp ";
        return sharedPreferences.getString(prefServerLatestIp , "");
    }
}
