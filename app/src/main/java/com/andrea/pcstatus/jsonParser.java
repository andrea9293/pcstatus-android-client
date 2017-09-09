package com.andrea.pcstatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class jsonParser {
    private String TAG = "JsonParser";

    public jsonParser(String jsonStr) throws JSONException {
        JSONObject jsonObj;
        String[] strings;
        jsonObj = new JSONObject(jsonStr); //assegnazione della stringa ad un oggetto JSONObject

        JSONArray jsonArray = jsonObj.getJSONArray("batteryInfo");
        strings = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            strings[i] = jsonArray.getString(i);
        }
        SingletonBatteryStatus.getInstance().setBattery(strings);

        jsonArray = jsonObj.getJSONArray("cpuInfo");
        strings = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            strings[i] = jsonArray.getString(i);
        }
        SingletonBatteryStatus.getInstance().setCpu(strings);

        jsonArray = jsonObj.getJSONArray("numericAvaibleFileSystem");
        strings = new String[jsonArray.length()];
        for(int i =0;i<jsonArray.length();i++){
            strings[i] = jsonArray.getString(i);
        }
        SingletonBatteryStatus.getInstance().setAvaibleFileSystem(strings);

        strings = jsonObj.getString("disks").split("\n");
        SingletonBatteryStatus.getInstance().setDisks(strings);


        strings = jsonObj.getString("computerInfo").split("\n");
        SingletonBatteryStatus.getInstance().setComputerInfo(strings);


        strings = jsonObj.getString("miscellaneous").split("\n");
        SingletonBatteryStatus.getInstance().setMiscellaneous(strings);

        String string;
        string = jsonObj.getString("numericCpuLoad");
        SingletonBatteryStatus.getInstance().setCpuLoad(string);

        string = jsonObj.getString("numericFreeRam");
        SingletonBatteryStatus.getInstance().setFreeRam(string);

        string = jsonObj.getString("numericPercPerThread");
        SingletonBatteryStatus.getInstance().setPercPerThread(string);




        SingletonBatteryStatus.getInstance().notifyMyObservers();
    }

}


