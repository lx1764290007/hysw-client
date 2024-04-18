package com.gdxydgyhlw.heyuan.eventbus;

import com.gdxydgyhlw.heyuan.http.CollectData;

import java.util.List;

public class FetchCollectSuccessEvent {
    private List<CollectData> data;
    public FetchCollectSuccessEvent(List<CollectData> d){
        this.data = d;
    }
    public List<CollectData> getData(){
        return this.data;
    }
}
