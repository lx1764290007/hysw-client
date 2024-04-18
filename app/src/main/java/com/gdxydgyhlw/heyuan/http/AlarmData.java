package com.gdxydgyhlw.heyuan.http;

public class AlarmData {
    public String alarmGrade;
    public String alarmValue;
    public String createTime;
    public int deviceManageId;
    public Boolean handler;
    public int id;
    public int sensorManageId;
    public String triggerRemark;
    public String unit;
    private String deviceName;
    private String sensorName;
    public String getDeviceName(){
        return this.deviceName;
    }
    public String getSensorName(){
        return this.sensorName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public void setSensorName(String sensorName){
        this.sensorName = sensorName;
    }
}
