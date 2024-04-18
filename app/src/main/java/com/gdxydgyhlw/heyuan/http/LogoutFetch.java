package com.gdxydgyhlw.heyuan.http;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.gdxydgyhlw.heyuan.eventbus.LogoutEvent;
import com.gdxydgyhlw.heyuan.eventbus.ResponseErrorEvent;
import org.greenrobot.eventbus.EventBus;
import java.util.Objects;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class LogoutFetch {
    public void logout(OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Service.getBaseUrl())
                .client(client)
                .build();
        // 生成ApiService接口代理
        ApiService service = retrofit.create(ApiService.class);

        Call<okhttp3.ResponseBody> call = service.logout();
        call.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call, @Nullable Response<okhttp3.ResponseBody> response) {
                if (response != null) {
                   EventBus.getDefault().post(new LogoutEvent());
                }
            }

            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                Log.e("logout_error", Objects.requireNonNull(t.getMessage()));
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
