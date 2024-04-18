package com.gdxydgyhlw.heyuan.eventbus;
import com.gdxydgyhlw.heyuan.http.AlarmData;
import com.gdxydgyhlw.heyuan.http.SearchResponse;

import java.util.List;

public class FetchAlarmSuccessEvent {
    private SearchResponse<List<AlarmData>> data;
    public FetchAlarmSuccessEvent(SearchResponse<List<AlarmData>> d){
        this.data = d;
    }
    public SearchResponse<List<AlarmData>> getData(){
        return this.data;
    }
}
