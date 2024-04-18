package com.gdxydgyhlw.heyuan.http;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.gdxydgyhlw.heyuan.eventbus.FetchSensorListSuccessEvent;
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

public class SensorFetch {
    Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    private int deviceId = 0;

    public SensorFetch(int deviceId) {
        this.deviceId = deviceId;
    }
    public SensorFetch() {

    }
    public void getSensorList(OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Service.getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        // 生成ApiService接口代理
        ApiService service = retrofit.create(ApiService.class);

        Call<List<SensorData>> call = service.getSensorList(this.deviceId>0? this.deviceId:null);
        call.enqueue(new Callback<List<SensorData>>() {
            @Override
            public void onResponse(@NonNull Call<List<SensorData>> call, @Nullable Response<List<SensorData>> response) {
                if (response != null) {
                    List<SensorData> res = response.body();
                    if (res != null) {
                        EventBus.getDefault().post(new FetchSensorListSuccessEvent(res));
                    } else {
                        Error responseError = new Error();
                        responseError.message = "未知错误";
                        responseError.type = "未知错误";
                        EventBus.getDefault().post(new ResponseErrorEvent(responseError));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SensorData>> call, @NonNull Throwable t) {
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
