package com.andrea.pcstatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * this class parses a json string downloaded from URL server.
 *
 * @author Andrea Bravaccino
 */
class JsonParser {

    /**
     * the constructor analyzes the string and extracts the information to store it in the model
     *
     * @param jsonStr json string downloaded from URL server
     * @throws JSONException Exception required for json parser
     * @see org.json.JSONException
     */
    JsonParser(String jsonStr) throws JSONException {
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
        SingletonBatteryStatus.getInstance().setCpuInfo(strings);

        try {
            jsonArray = jsonObj.getJSONArray("numericAvaibleFileSystem");
            Float[] floats = new Float[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                floats[i] = Float.valueOf(jsonArray.getString(i));
            }
            SingletonBatteryStatus.getInstance().setAvaibleFileSystem(floats);
        } catch (JSONException e) {
            Float[] floats = new Float[jsonArray.length()];
            floats[0] = 0f;
            SingletonBatteryStatus.getInstance().setAvaibleFileSystem(floats);
        }

        strings = jsonObj.getString("disks").split("\n");
        SingletonBatteryStatus.getInstance().setDisks(strings);

        strings = jsonObj.getString("computerInfo").split("\n");
        SingletonBatteryStatus.getInstance().setComputerInfo(strings);

        strings = jsonObj.getString("miscellaneous").split("\n");
        SingletonBatteryStatus.getInstance().setMiscellaneous(strings);

        String string;
        string = jsonObj.getString("numericCpuLoad");
        SingletonBatteryStatus.getInstance().setCpuLoad(Float.parseFloat(string));

        string = jsonObj.getString("numericBatteryPerc");
        if (!string.equals(""))
            SingletonBatteryStatus.getInstance().setBatteryPerc(string);

        string = jsonObj.getString("numericPercPerThread");
        String[] tmpStr = string.split("\n");
        Float[] tmpFlo = new Float[tmpStr.length];
        for (int i = 0; i < tmpStr.length; i++) {
            tmpFlo[i] = Float.valueOf(tmpStr[i]);
        }
        SingletonBatteryStatus.getInstance().setPercPerThread(tmpFlo);

        SingletonBatteryStatus.getInstance().notifyMyObservers();
    }
}





