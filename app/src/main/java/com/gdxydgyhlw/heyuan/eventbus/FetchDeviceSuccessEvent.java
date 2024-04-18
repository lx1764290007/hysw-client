package com.gdxydgyhlw.heyuan.eventbus;

import com.gdxydgyhlw.heyuan.http.DeviceData;
import java.util.List;

public class FetchDeviceSuccessEvent {
    private List<DeviceData> data;
    public FetchDeviceSuccessEvent(List<DeviceData> d){
        this.data = d;
    }
    public List<DeviceData> getData(){
        return this.data;
    }
}
