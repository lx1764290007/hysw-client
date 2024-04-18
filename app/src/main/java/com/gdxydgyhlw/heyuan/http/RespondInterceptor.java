package com.gdxydgyhlw.heyuan.http;

import static com.gdxydgyhlw.heyuan.http.Service.COOKIE;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gdxydgyhlw.heyuan.eventbus.ResponseErrorEvent;
import com.gdxydgyhlw.heyuan.eventbus.UnauthorizedEvent;
import com.gdxydgyhlw.heyuan.eventbus.UnknownErrorEvent;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.ResponseBody;

//接收拦截器。如果回复异常则发布消息
public class RespondInterceptor implements Interceptor {
    SharedPreferences sp;
    public RespondInterceptor(SharedPreferences param){
        this.sp = param;
    }
    @NonNull
    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        okhttp3.Response originalResponse = chain.proceed(chain.request());
        List<String> cookies = originalResponse.headers("Set-Cookie");
        if (!cookies.isEmpty()) {
            //解析Cookie
            for (String header : cookies) {
                if(header.contains("SESSION")){
                    String cookie = header.substring(header.indexOf("SESSION"),
                            header.indexOf(";"));
                    //通过SharedPreferences写入本地储存
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(COOKIE, cookie);
                    editor.apply();
                }
            }
        }
        if(originalResponse.code() != 200 && originalResponse.code() != 500) {
            ResponseBody body = originalResponse.body();
            assert body != null;
            @Nullable Error res = null;
            try{
                res = new Gson().fromJson(body.string(), Error.class);
            }catch (IllegalStateException exception){
                Log.e("Gson_ERR", Objects.requireNonNull(exception.getMessage()));
                Error selfError = new Error();
                selfError.message = "未知错误";
                selfError.type = "unknown";
                EventBus.getDefault().post(new UnknownErrorEvent(selfError));
            }
            if(res != null) {
                if(originalResponse.code() == 400) {
                    EventBus.getDefault().post(new ResponseErrorEvent(res));
                } else if(originalResponse.code() == 401) {
                    EventBus.getDefault().post(new UnauthorizedEvent(res));
                } else {
                    Error selfError = new Error();
                    selfError.message = originalResponse.message();
                    selfError.type = "unknown";
                    EventBus.getDefault().post(new UnknownErrorEvent(selfError));
                }
            }
        } else if(originalResponse.code() == 500){
            Error selfError = new Error();
            selfError.message = "未知错误";
            selfError.type = "unknown";
            EventBus.getDefault().post(new UnknownErrorEvent(selfError));
        }

        return originalResponse;
    }
};
