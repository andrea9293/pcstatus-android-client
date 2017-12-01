package com.andrea.pcstatus;

import org.json.JSONException;

import java.math.BigDecimal;
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
    private String jsonStr;
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

    public void setJsonStr(String jsonStr) throws JSONException {
        this.jsonStr = jsonStr;
        new JsonParser(jsonStr);
    }

    public String getDisks() {
        return arrayStringToString(disks, "\n");
    }

    public String getCpuInfo() {
        return arrayStringToString(cpu,"");
    }

    public void setCpuInfo(String[] cpu) {
        this.cpu = cpu;
    }

    public String getFirstFilesystemLabel(){
        return firstFileSystemLabel;
    }

    public void setDisks(String[] disks) {
        firstFileSystemLabel = disks[0].substring(disks[0].indexOf("(") + 1);
        firstFileSystemLabel = firstFileSystemLabel.substring(0,Math.min(firstFileSystemLabel.length(),2));
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
        return arrayStringToString(battery,"");
    }

    public void setComputerInfo(String[] strings) {
        this.computerInfo = strings;
    }

    public String getComputerInfo() {
        return arrayStringToString(computerInfo,"");
    }

    public void addingObserver(Observer observer) {
        addObserver(observer);
    }

    public void notifyMyObservers() {
        setChanged();
        notifyObservers();
    }

    public void setMiscellaneous(String[] strings) {
        miscellaneous = strings;
    }

    public String getMiscellaneous() {
        return arrayStringToString(miscellaneous, "");
    }

    private String arrayStringToString(String[] arrayString, String space) {
        if (arrayString != null) {
            StringBuilder toString = new StringBuilder();
            for (int i = 0; i < arrayString.length; i++) {
                toString.append(arrayString[i] + "\n" + space);
            }
            return toString.toString();
        } else
            return null;
    }

    public void setCpuLoad(Float cpuLoad) {
        this.cpuLoad = cpuLoad;
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

    public Float[] getAvaibleFileSystem() {
        return avaibleFileSystem;
    }

    public void setAvaibleFileSystem(Float[] avaibleFileSystem) {
        this.avaibleFileSystem = avaibleFileSystem;
    }

    private static Float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return Float.parseFloat(bd.toString());
    }

    public void setPercPerThread(Float[] percPerThread) {
        this.percPerThread=percPerThread;
    }

    public Float[] getPercPerThread() {
        return percPerThread;
    }

    /**
     * setter for battery percentage
     * @param batteryPerc is percentage of battery
     */
    void setBatteryPerc(String batteryPerc) {
        this.batteryPerc = Integer.parseInt(batteryPerc);
    }

    public Integer getBatteryPerc() {
        return batteryPerc;
    }
}
