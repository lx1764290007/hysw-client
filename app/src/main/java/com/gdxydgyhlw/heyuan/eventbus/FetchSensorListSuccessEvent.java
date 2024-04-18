package com.gdxydgyhlw.heyuan.eventbus;

import com.gdxydgyhlw.heyuan.http.SensorData;

import java.util.List;

public class FetchSensorListSuccessEvent {
    private List<SensorData> data;
    public FetchSensorListSuccessEvent(List<SensorData> d){
        this.data = d;
    }
    public List<SensorData> getData(){
        return this.data;
    }
}
