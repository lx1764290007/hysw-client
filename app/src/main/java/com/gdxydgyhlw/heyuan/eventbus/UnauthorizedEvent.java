package com.gdxydgyhlw.heyuan.eventbus;

import com.gdxydgyhlw.heyuan.http.Error;

public class UnauthorizedEvent {
    private Error error;
    public UnauthorizedEvent(Error err){
        this.error = err;
    }

    public Error getError() {
        return error;
    }
}
