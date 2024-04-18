package com.gdxydgyhlw.heyuan.eventbus;

import com.gdxydgyhlw.heyuan.http.SensorTypesData;

import java.util.List;

public class FetchSensorTypesCountSuccessEvent {
    private List<SensorTypesData> data;
    public FetchSensorTypesCountSuccessEvent(List<SensorTypesData> d){
        this.data = d;
    }
    public List<SensorTypesData> getData(){
        return this.data;
    }
}
