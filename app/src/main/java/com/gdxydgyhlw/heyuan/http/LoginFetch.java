package com.gdxydgyhlw.heyuan.http;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gdxydgyhlw.heyuan.Login;
import com.gdxydgyhlw.heyuan.eventbus.LoginSuccessEvent;
import com.gdxydgyhlw.heyuan.eventbus.ResponseErrorEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import retrofit2.Callback;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */

public class LoginFetch {
    Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    public void login(String username, String password, Login loginActivity) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Service.getBaseUrl())
                 .client(loginActivity.client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        // 生成ApiService接口代理
        ApiService service = retrofit.create(ApiService.class);
        LoginParams loginParams = new LoginParams();
        loginParams.loginName = username;
        loginParams.password = password;
        Call<LoginData> call = service.login(loginParams);
        call.enqueue(new Callback<LoginData>() {
            @Override
            public void onResponse(@NonNull Call<LoginData> call, @Nullable Response<LoginData> response) {
                if (response != null) {
                    LoginData res = response.body();
                    if (res != null) {
                        EventBus.getDefault().post(new LoginSuccessEvent(res));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginData> call, @NonNull Throwable t) {
              //  loginActivity.onLoginFail(t.getMessage());
                Log.e("login_error", Objects.requireNonNull(t.getMessage()));
                if(t.getMessage() != null && t.getMessage().toLowerCase().startsWith("connection")){
                    Error responseError = new Error();
                    responseError.message = "网络不佳";
                    responseError.type = "未知错误";
                    EventBus.getDefault().post(new ResponseErrorEvent(responseError));
                }
            }

        });

    }


    public void logout() {
        // TODO: revoke authentication
    }
}