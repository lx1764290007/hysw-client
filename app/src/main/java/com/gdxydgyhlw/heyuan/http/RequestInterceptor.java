package com.gdxydgyhlw.heyuan.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.gdxydgyhlw.heyuan.libs.Permission;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

//第二步
//发送拦截器  添加Cookie到请求头，判断有无网络权限，如无则取消请求
public class RequestInterceptor implements Interceptor {
    SharedPreferences sp;
    @Nullable Context cont;
    public RequestInterceptor(SharedPreferences s, @Nullable Context context){

        this.sp = s;
        this.cont = context;
    }
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if(cont != null && !new Permission().hasInternetAccess(cont)){
          chain.call().cancel();
        }
        final Request.Builder builder = chain.request().newBuilder();
        //添加Cookie
        if(!TextUtils.isEmpty(sp.getString("cookie",""))){
            builder.addHeader("Cookie", sp.getString("cookie",""));
            builder.addHeader("Content-Type", "application/json;charset=utf-8");
        }
        return chain.proceed(builder.build());
    }
};