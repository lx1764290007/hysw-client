package com.gdxydgyhlw.heyuan.eventbus;

import com.gdxydgyhlw.heyuan.http.Error;

public class UnknownErrorEvent {
    private Error responseError;
    public UnknownErrorEvent(Error req){
        this.responseError = req;
    }
    public Error getUnknownError() {
        return responseError;
    }
}
