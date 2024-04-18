package com.gdxydgyhlw.heyuan.eventbus;

import com.gdxydgyhlw.heyuan.http.AlarmCountData;

import java.util.List;

public class FetchAlarmCountSuccessEvent {
    private List<AlarmCountData> data;
    public FetchAlarmCountSuccessEvent(List<AlarmCountData> d){
        this.data = d;
    }
    public List<AlarmCountData> getData(){
        return this.data;
    }
}
