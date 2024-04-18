package com.gdxydgyhlw.heyuan.eventbus;

public class FetchDeviceSearchSuccessEvent<T> {
    private final T data;
    public FetchDeviceSearchSuccessEvent(T d){
        this.data = d;
    }
    public T getData(){
        return this.data;
    }
}
