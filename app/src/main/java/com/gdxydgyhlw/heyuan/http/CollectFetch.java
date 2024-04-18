package com.gdxydgyhlw.heyuan.http;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gdxydgyhlw.heyuan.eventbus.FetchCollectSuccessEvent;
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

public class CollectFetch {
    Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    public void getCollect(OkHttpClient client, int deviceId, String start, String end) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Service.getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        // 生成ApiService接口代理
        ApiService service = retrofit.create(ApiService.class);

        Call<SearchResponse<List<CollectData>>> call = service.getCollectList(deviceId, 1, 10);
        call.enqueue(new Callback<SearchResponse<List<CollectData>>>() {
            @Override
            public void onResponse(@NonNull Call<SearchResponse<List<CollectData>>> call, @Nullable Response<SearchResponse<List<CollectData>>> response) {
                if (response != null) {
                    SearchResponse<List<CollectData>> res = response.body();
                    if (res != null) {
                      EventBus.getDefault().post(new FetchCollectSuccessEvent((List<CollectData>) res.records));
                    } else {
                        Error responseError = new Error();
                        responseError.message = "未知错误";
                        responseError.type = "未知错误";
                        EventBus.getDefault().post(new ResponseErrorEvent(responseError));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SearchResponse<List<CollectData>>> call, @NonNull Throwable t) {
                Log.e("collect_fetch_error", Objects.requireNonNull(t.getMessage()));
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
