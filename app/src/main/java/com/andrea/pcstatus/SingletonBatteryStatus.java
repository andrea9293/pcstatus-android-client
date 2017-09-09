package com.andrea.pcstatus;

import android.util.Log;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.Observable;

public class SingletonBatteryStatus extends Observable {
    private static SingletonBatteryStatus ourInstance = new SingletonBatteryStatus();

    public static SingletonBatteryStatus getInstance() {
        return ourInstance;
    }

    private String TAG = "SingletonBatteryStatus";
    private String[] battery;
    private String[] cpu;
    private String[] disks;
    private String jsonStr;
    private String[] computerInfo;
    private String[] miscellaneous;

    private Float cpuLoad;
    private Float percRam;
    private String[] avaibleFileSystem;
    private Float[] percPerThread;


    private SingletonBatteryStatus() {
    }

    void setJsonStr(String jsonStr) throws JSONException {
        this.jsonStr = jsonStr;
        new jsonParser(jsonStr);
    }

    public String getDisks() {
        return arrayStringToString(disks);
    }

    public String getCpu() {
        return arrayStringToString(cpu);
    }

    public void setCpu(String[] cpu) {
        this.cpu = cpu;
    }

    public void setDisks(String[] disks) {
        this.disks = disks;
    }

    public String getJsonStr() {
        return jsonStr;
    }

    public void setBattery(String[] s) {
        //System.out.println("modificato il valore in\n " + s);
        battery = s;
    }

    public String getBattery() {
        return arrayStringToString(battery);
    }

    public void setComputerInfo(String[] strings) {
        this.computerInfo = strings;
    }

    public String getComputerInfo() {
        return arrayStringToString(computerInfo);
    }

    void addingObserver(MainActivity mainActivity) {
        addObserver(mainActivity);
    }

    public void notifyMyObservers() {
        setChanged();
        notifyObservers();
    }

    public void setMiscellaneous(String[] strings) {
        miscellaneous = strings;
    }

    public String getMiscellaneous() {
        return arrayStringToString(miscellaneous);
    }

    private String arrayStringToString(String[] arrayString) {
        if (arrayString != null) {
            StringBuilder toString = new StringBuilder();
            for (int i = 0; i < arrayString.length; i++) {
                toString.append(arrayString[i] + "\n");
            }
            return toString.toString();
        } else
            return null;
    }

    public void setCpuLoad(String cpuLoad) {
        this.cpuLoad = Float.parseFloat(cpuLoad);
    }

    public Float getCpuLoad() {
        return cpuLoad;
    }

    public void setFreeRam(String freeRam) {
        percRam = Float.valueOf(freeRam);
    }

    public Float getFreeRam() {
        return round(percRam, 2);
    }

    public String[] getAvaibleFileSystem() {
        return avaibleFileSystem;
    }

    public void setAvaibleFileSystem(String[] avaibleFileSystem) {
        this.avaibleFileSystem = avaibleFileSystem;
    }

    private static Float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return Float.parseFloat(bd.toString());
    }

    public void setPercPerThread(String percPerThread) {
        String[] tmpStr = percPerThread.split("\n");
        Float[] tmpFlo = new Float[tmpStr.length];
        for (int i = 0; i < tmpStr.length; i++) {
            tmpFlo[i] = Float.valueOf(tmpStr[i]);
            Log.d(TAG, "stampo perperthread " + tmpFlo[i]);
        }
        this.percPerThread=tmpFlo;
    }

    public Float[] getPercPerThread() {
        return percPerThread;
    }
}
