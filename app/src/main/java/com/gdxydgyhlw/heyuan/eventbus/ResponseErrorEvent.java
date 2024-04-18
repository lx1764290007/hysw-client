package com.gdxydgyhlw.heyuan.eventbus;
import com.gdxydgyhlw.heyuan.http.Error;
public class ResponseErrorEvent {
    private Error responseError;
    public ResponseErrorEvent(Error req){
        this.responseError = req;
    }
    public Error getResponseError() {
        return responseError;
    }

}
