package com.gdxydgyhlw.heyuan.eventbus;

import com.gdxydgyhlw.heyuan.http.LoginData;

public class LoginSuccessEvent {
    private LoginData loginData;
    public LoginSuccessEvent(LoginData _loginData){
        this.loginData = _loginData;
    }
    public LoginData getData(){
        return this.loginData;
    }
}
