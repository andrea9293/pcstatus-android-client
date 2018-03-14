package com.andrea.pcstatus;

import org.json.JSONException;

import java.util.Observable;
import java.util.Observer;

public class SingletonBatteryStatus extends Observable {
    private static SingletonBatteryStatus ourInstance = new SingletonBatteryStatus();

    public static SingletonBatteryStatus getInstance() {
        return ourInstance;
    }

    private String TAG = "SingletonBatteryStatus";
    private String[] battery;
    private String[] cpu;
    private String[] disks;
    private String[] computerInfo;
    private String[] miscellaneous;
    private String firstFileSystemLabel;
    private Float cpuLoad;
    private Float percRam;
    private Float[] avaibleFileSystem;
    private Float[] percPerThread;
    private int batteryPerc;

    private SingletonBatteryStatus() {
    }

    public void setJsonStr(String jsonStr) {
        try {
            new JsonParser(jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    String getDisks() {
        return arrayStringToString(disks, "\n");
    }

    String getCpuInfo() {
        return arrayStringToString(cpu, "");
    }

    void setCpuInfo(String[] cpu) {
        this.cpu = cpu;
    }

    public String getFirstFilesystemLabel() {
        return firstFileSystemLabel;
    }

    void setDisks(String[] disks) {
        firstFileSystemLabel = disks[0].substring(disks[0].indexOf("(") + 1);
        firstFileSystemLabel = firstFileSystemLabel.substring(0, Math.min(firstFileSystemLabel.length(), 2));
        this.disks = disks;
    }

    void setBattery(String[] s) {
        //System.out.println("modificato il valore in\n " + s);
        battery = s;
    }

    String getBattery() {
        return arrayStringToString(battery, "");
    }

    void setComputerInfo(String[] strings) {
        this.computerInfo = strings;
    }

    String getComputerInfo() {
        return arrayStringToString(computerInfo, "");
    }

    public void addingObserver(Observer observer) {
        addObserver(observer);
    }

    void notifyMyObservers() {
        setChanged();
        notifyObservers();
    }

    void setMiscellaneous(String[] strings) {
        miscellaneous = strings;
    }

    String getMiscellaneous() {
        return arrayStringToString(miscellaneous, "");
    }

    private String arrayStringToString(String[] arrayString, String space) {
        if (arrayString != null) {
            StringBuilder toString = new StringBuilder();
            for (String anArrayString : arrayString) {
                toString.append(anArrayString).append("\n").append(space);
            }
            return toString.toString();
        } else
            return null;
    }

    void setCpuLoad(Float cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public Float getCpuLoad() {
        return cpuLoad;
    }

    public Float[] getAvaibleFileSystem() {
        return avaibleFileSystem;
    }

    void setAvaibleFileSystem(Float[] avaibleFileSystem) {
        this.avaibleFileSystem = avaibleFileSystem;
    }

    void setPercPerThread(Float[] percPerThread) {
        this.percPerThread = percPerThread;
    }

    public Float[] getPercPerThread() {
        return percPerThread;
    }

    /**
     * setter for battery percentage
     *
     * @param batteryPerc is percentage of battery
     */
    void setBatteryPerc(String batteryPerc) {
        this.batteryPerc = Integer.parseInt(batteryPerc);
    }

    public Integer getBatteryPerc() {
        return batteryPerc;
    }
}
