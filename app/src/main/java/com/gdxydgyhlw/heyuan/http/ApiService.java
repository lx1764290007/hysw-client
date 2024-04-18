package com.gdxydgyhlw.heyuan.http;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ApiService {
    @POST("login")
    Call<LoginData> login(@Body LoginParams jsonObject);
    @GET("device/manage/list")
    Call<List<DeviceData>> getDeviceList();
    @GET("collect/search")
    Call<SearchResponse<List<CollectData>>> getCollectList(@Query("deviceManageId") int deviceManageId, @Query("currentPage") int currentPage, @Query("size") int size);
    @GET("sensor/mange/type/count")
    Call<List<SensorTypesData>> getSensorTypesCount();
    @GET("sensor/trigger/record/alarm/count")
    Call<List<AlarmCountData>> getAlarmCount();
    @GET("device/manage/search")
    Call<SearchResponse<List<DeviceData>>> getDeviceDataSearchList(@Query("size") int size, @Query("currentPage") int currentPage);
    @GET("sensor/mange/list")
    Call<List<SensorData>> getSensorList(@Query("deviceManageId") int deviceManageId);
    @POST("login/out")
    Call<ResponseBody> logout();

    @GET("sensor/trigger/record/search")
    Call<SearchResponse<List<AlarmData>>> getAlarmDataSearchList(@Query("size") int size, @Query("currentPage") int currentPage);
}
