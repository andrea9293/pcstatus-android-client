package com.andrea.pcstatus;

import org.json.JSONException;

import java.util.Observable;

public class SingletonBatteryStatus extends Observable {
    private static SingletonBatteryStatus ourInstance = new SingletonBatteryStatus();

    public static SingletonBatteryStatus getInstance() {
        return ourInstance;
    }

    private String[] battery;
    private String[] cpu;
    private String[] disks;
    private String jsonStr;
    private String[] computerInfo;
    private String[] miscellaneous;


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

    private String arrayStringToString(String [] arrayString){
        StringBuilder toString = new StringBuilder();
        for (int i = 0; i < arrayString.length; i++) {
            toString.append(arrayString[i] + "\n");
        }
        return toString.toString();
    }
}
