package com.gdxydgyhlw.heyuan.http;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ClientBuilder {

    public static OkHttpClient getClient(SharedPreferences sp, Context context) {
        OkHttpClient.Builder okBuilder = (new OkHttpClient.Builder()).connectTimeout(5L, TimeUnit.SECONDS);
        okBuilder.addInterceptor(new RequestInterceptor(sp, context));
        okBuilder.addInterceptor(new RespondInterceptor(sp));
        return okBuilder.build();
    }
}
