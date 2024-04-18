package com.gdxydgyhlw.heyuan.http;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gdxydgyhlw.heyuan.eventbus.FetchDeviceSuccessEvent;
import com.gdxydgyhlw.heyuan.eventbus.FetchSensorTypesCountSuccessEvent;
import com.gdxydgyhlw.heyuan.eventbus.ResponseErrorEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

//拉取设备类型（饼图）统计
public class SensorTypesCountFetch {
    Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    public void getSensorTypesCount(OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Service.getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        // 生成ApiService接口代理
        ApiService service = retrofit.create(ApiService.class);

        Call<List<SensorTypesData>> call = service.getSensorTypesCount();
        call.enqueue(new Callback<List<SensorTypesData>>() {
            @Override
            public void onResponse(@NonNull Call<List<SensorTypesData>> call, @Nullable Response<List<SensorTypesData>> response) {
                if (response != null) {
                    List<SensorTypesData> res = response.body();
                    if (res != null) {
                        EventBus.getDefault().post(new FetchSensorTypesCountSuccessEvent((List<SensorTypesData>) res));
                    } else {
                        Error responseError = new Error();
                        responseError.message = "未知错误";
                        responseError.type = "未知错误";
                        EventBus.getDefault().post(new ResponseErrorEvent(responseError));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SensorTypesData>> call, @NonNull Throwable t) {
                //  loginActivity.onLoginFail(t.getMessage());
                Log.e("sensor_type_fetch_error", Objects.requireNonNull(t.getMessage()));
                if(t.getMessage() != null && t.getMessage().toLowerCase().startsWith("connection")){
                    Error responseError = new Error();
                    responseError.message = "网络不佳";
                    responseError.type = "未知错误";
                    EventBus.getDefault().post(new ResponseErrorEvent(responseError));
                }
            }

        });

    }
}
