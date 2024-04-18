package com.gdxydgyhlw.heyuan.ui.dashboard;

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
import com.gdxydgyhlw.heyuan.databinding.FragmentDashboardBinding;
import com.gdxydgyhlw.heyuan.eventbus.FetchDeviceSearchSuccessEvent;
import com.gdxydgyhlw.heyuan.http.ClientBuilder;
import com.gdxydgyhlw.heyuan.http.DeviceData;
import com.gdxydgyhlw.heyuan.http.DeviceSearchFetch;
import com.gdxydgyhlw.heyuan.http.SearchResponse;
import com.gdxydgyhlw.heyuan.libs.Cache;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;

public class DashboardFragment extends Fragment {
    private OkHttpClient client;
    private FragmentDashboardBinding binding;
    private RefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private final int PAGE_SIZE = 10;
    private int pageCurrent = 1;
    private int total = 0;
    private List<DeviceData> deviceDataList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Context directBootContext = createDeviceProtectedStorageContext(requireActivity().getApplicationContext());
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        SharedPreferences sp = directBootContext.getSharedPreferences(COOKIE, MODE_PRIVATE);
        client = ClientBuilder.getClient(sp, getContext());

        return binding.getRoot();
    }

    private void initRefreshComponent() {
        this.refreshLayout = (RefreshLayout) requireActivity().findViewById(R.id.scrollView3);
        this.refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        this.refreshLayout.setEnableLoadMore(true);
        // 禁止上拉加载
        this.refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));
        this.refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
                refresh();
            }

        });
        this.refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
                loadMore();
            }
        });
    }

    private void setItem(List<DeviceData> data) {
        if (this.deviceDataList != null && this.deviceDataList.isEmpty()) {
            requireActivity().findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        } else {
            requireActivity().findViewById(R.id.empty_view).setVisibility(View.GONE);
        }
        recyclerView.setAdapter(new NormalAdaptor(data));
        refreshLayout.setEnableLoadMore(this.deviceDataList != null && this.total > this.deviceDataList.size());
        if (this.progressBar.getVisibility() == View.VISIBLE)
            this.progressBar.setVisibility(View.GONE);
    }

    private void getData() {
        DeviceSearchFetch deviceSearchFetch = new DeviceSearchFetch();
        deviceSearchFetch.getDeviceSearch(this.client, this.PAGE_SIZE, this.pageCurrent);
    }

    private void loadMore() {
        if (this.total > this.deviceDataList.size()) {
            this.pageCurrent += 1;
            getData();
        }
    }

    private void refresh() {
        this.pageCurrent = 1;
        this.deviceDataList.clear();
        getData();
    }

    private void initProgressBar() {
        this.progressBar = (ProgressBar) requireActivity().findViewById(R.id.load_progress);
    }

    private void initData() {
        Cache cache = new Cache(getContext());
        String deviceListStr = cache.readFromCache(cache.DEVICE_SEARCH_PAGE_DATA);
        String currentStr = cache.readFromCache(cache.DEVICE_SEARCH_PAGE_CURRENT);
        String totalStr = cache.readFromCache(cache.DEVICE_SEARCH_PAGE_TOTAL);
        if (deviceListStr != null && !deviceListStr.isEmpty() && !deviceListStr.equals("null")) {
            getDeviceDataFromCache(deviceListStr, totalStr, currentStr);
            setItem(this.deviceDataList);
        } else {
            getData();
            showProgressBar();
        }
    }

    /**
     * 从设备缓存中读取设备列表数据
     *
     * @param data {String} JSON字符串
     */
    public void getDeviceDataFromCache(String data, String totalStr, String currentStr) {
        Type intArrayType = new TypeToken<List<DeviceData>>() {
        }.getType();
        this.deviceDataList = new Gson().fromJson(data, intArrayType);
        this.total = Integer.parseInt(totalStr);
        this.pageCurrent = Integer.parseInt(currentStr);

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
            if (this.deviceDataList == null || (this.deviceDataList != null && this.deviceDataList.isEmpty()) && requireActivity().findViewById(R.id.empty_view) != null) {
                requireActivity().findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            }
        } catch (IllegalStateException exception) {
            Log.e("NullPointerException", Objects.requireNonNull(exception.getMessage()));
        }
    }

    private void initRecycleView() {
        recyclerView = (RecyclerView) requireActivity().findViewById(R.id.fragment_container_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);

    }

    private void saveDataToCache() {
        Gson gson = new Gson();
        Cache cache = new Cache(getContext());
        if (this.deviceDataList != null && !this.deviceDataList.isEmpty()) {
            String deviceListStr = gson.toJson(this.deviceDataList);
            cache.saveToCache(cache.DEVICE_SEARCH_PAGE_DATA, deviceListStr);
            cache.saveToCache(cache.DEVICE_SEARCH_PAGE_CURRENT, String.valueOf(this.pageCurrent));
            cache.saveToCache(cache.DEVICE_SEARCH_PAGE_TOTAL, String.valueOf(this.total));
        }

    }

    @Subscribe
    public void onFetchListSuccess(FetchDeviceSearchSuccessEvent<SearchResponse<List<DeviceData>>> event) {
        this.total = event.getData().total;
        this.deviceDataList.addAll(event.getData().records);
        setItem(this.deviceDataList);

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
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        saveDataToCache();
        binding = null;
    }
}