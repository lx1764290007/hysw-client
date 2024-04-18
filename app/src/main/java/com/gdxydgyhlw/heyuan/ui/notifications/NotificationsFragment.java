package com.gdxydgyhlw.heyuan.ui.notifications;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.createDeviceProtectedStorageContext;
import static com.gdxydgyhlw.heyuan.http.Service.COOKIE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gdxydgyhlw.heyuan.R;
import com.gdxydgyhlw.heyuan.databinding.FragmentNotificationsBinding;
import com.gdxydgyhlw.heyuan.eventbus.FetchAlarmSuccessEvent;
import com.gdxydgyhlw.heyuan.eventbus.FetchDeviceSuccessEvent;
import com.gdxydgyhlw.heyuan.eventbus.FetchSensorListSuccessEvent;
import com.gdxydgyhlw.heyuan.http.AlarmData;
import com.gdxydgyhlw.heyuan.http.AlarmSearchFetch;
import com.gdxydgyhlw.heyuan.http.ClientBuilder;
import com.gdxydgyhlw.heyuan.http.DeviceData;
import com.gdxydgyhlw.heyuan.http.DeviceFetch;
import com.gdxydgyhlw.heyuan.http.SensorData;
import com.gdxydgyhlw.heyuan.http.SensorFetch;
import com.gdxydgyhlw.heyuan.libs.Cache;
import com.gdxydgyhlw.heyuan.myenum.SensorType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;

public class NotificationsFragment extends Fragment {
    private OkHttpClient client;
    private RefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private final int PAGE_SIZE = 10;
    private int pageCurrent = 1;
    private int total = 0;
    private List<DeviceData> deviceDataList = new ArrayList<>();
    private List<AlarmData> alarmDataList = new ArrayList<>();
    private List<SensorData> sensorDataList = new ArrayList<>();
    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Context directBootContext = createDeviceProtectedStorageContext(requireActivity().getApplicationContext());

        SharedPreferences sp = directBootContext.getSharedPreferences(COOKIE, MODE_PRIVATE);
        client = ClientBuilder.getClient(sp, getContext());

        return root;
    }

    private void initRefreshComponent() {
        this.refreshLayout = (RefreshLayout) requireActivity().findViewById(R.id.notify_scrollView);
        this.refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        this.refreshLayout.setEnableLoadMore(true);
        // 禁止上拉加载
        this.refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));
        this.refreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshLayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            refresh();
        });
        this.refreshLayout.setOnLoadMoreListener(refreshlayout -> {
            refreshlayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
            loadMore();
        });
    }

    private void setItem(List<AlarmData> data) {
        if (this.alarmDataList != null && this.alarmDataList.isEmpty()) {
            requireActivity().findViewById(R.id.notify_empty_view).setVisibility(View.VISIBLE);
        } else {
            requireActivity().findViewById(R.id.notify_empty_view).setVisibility(View.GONE);
        }
        recyclerView.setAdapter(new NormalAdaptor(data));
        refreshLayout.setEnableLoadMore(this.alarmDataList != null && this.total > this.alarmDataList.size());
        if (this.progressBar.getVisibility() == View.VISIBLE)
            this.progressBar.setVisibility(View.GONE);
        if (!data.isEmpty()) {
            getSensorData();
            getDeviceData();
        }
    }

    private void getDeviceData() {
        Cache cache = new Cache(getContext());
        String deviceListStr = cache.readFromCache(cache.DEVICES);
        if (deviceListStr != null && !deviceListStr.isEmpty() && !deviceListStr.equals("null")) {
            Type intArrayType = new TypeToken<List<DeviceData>>() {
            }.getType();
            this.deviceDataList = new Gson().fromJson(deviceListStr, intArrayType);
            setDeviceItem();
        } else {
            DeviceFetch deviceFetch = new DeviceFetch();
            deviceFetch.getDevice(client);
        }
    }

    private void getSensorData() {
        SensorFetch sensorFetch = new SensorFetch();
        sensorFetch.getSensorList(client);
    }

    private void getData() {
        AlarmSearchFetch alarmSearchFetch = new AlarmSearchFetch();
        alarmSearchFetch.getData(this.client, this.PAGE_SIZE, this.pageCurrent);
    }

    private void loadMore() {
        if (this.total > this.alarmDataList.size()) {
            this.pageCurrent += 1;
            getData();
        }
    }

    private void refresh() {
        this.pageCurrent = 1;
        this.alarmDataList.clear();
        getData();
    }

    private void initProgressBar() {
        this.progressBar = (ProgressBar) requireActivity().findViewById(R.id.notify_load_progress);
    }

    private void initData() {
        getData();
        showProgressBar();
    }

    private void showProgressBar() {
        Handler handler = new Handler();
        Runnable runnable = this::hideProgressBar;
        this.progressBar.setVisibility(View.VISIBLE);
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(runnable);
                }
            }, 5000);
        } catch (NullPointerException e) {
            Log.i("timer.schedule", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void hideProgressBar() {
            try {
                this.progressBar.setVisibility(View.GONE);
                if(this.alarmDataList == null || (this.alarmDataList != null && this.alarmDataList.isEmpty() && requireActivity().findViewById(R.id.notify_empty_view) != null)) {
                    requireActivity().findViewById(R.id.notify_empty_view).setVisibility(View.VISIBLE);
                }
            }catch (IllegalStateException exception){
                Log.e("NullPointerException", Objects.requireNonNull(exception.getMessage()));
            }

    }

    private void initRecycleView() {
        recyclerView = (RecyclerView) requireActivity().findViewById(R.id.notify_fragment_container_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);
    }

    private void setDeviceItem() {
        for (int i = 0; i < this.alarmDataList.size(); i++) {
            for (DeviceData deviceData : this.deviceDataList) {
                if (deviceData.id == this.alarmDataList.get(i).deviceManageId) {
                    AlarmData alarmData = this.alarmDataList.get(i);
                    alarmData.setDeviceName(deviceData.name);
                    this.alarmDataList.set(i, alarmData);
                }
            }
        }
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemRangeChanged(0, this.alarmDataList.size());
    }

    private void setSensorItem() {
        for (int i = 0; i < this.alarmDataList.size(); i++) {
            for (SensorData sensorData : this.sensorDataList) {
                if (sensorData.id == this.alarmDataList.get(i).sensorManageId) {
                    AlarmData alarmData = this.alarmDataList.get(i);
                    alarmData.setSensorName(SensorType.getName(sensorData.sensorType));
                    this.alarmDataList.set(i, alarmData);
                }
            }
        }
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemRangeChanged(0, this.alarmDataList.size());
    }

    @Subscribe
    public void onFetchListSuccess(FetchAlarmSuccessEvent event) {
        this.total = event.getData().total;
        this.alarmDataList.addAll(event.getData().records);
        setItem(this.alarmDataList);
    }

    @Subscribe
    public void onFetchDeviceListSuccess(FetchDeviceSuccessEvent event) {
        this.deviceDataList = event.getData();
        setDeviceItem();
    }

    @Subscribe
    public void onFetchSensorListSuccess(FetchSensorListSuccessEvent event) {
        this.sensorDataList = event.getData();
        setSensorItem();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        initProgressBar();
        initRefreshComponent();
        initRecycleView();
        initData();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}