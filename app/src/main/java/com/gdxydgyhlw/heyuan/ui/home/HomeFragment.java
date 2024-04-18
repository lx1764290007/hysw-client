package com.gdxydgyhlw.heyuan.ui.home;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.createDeviceProtectedStorageContext;
import static com.gdxydgyhlw.heyuan.http.Service.COOKIE;

import com.gdxydgyhlw.heyuan.http.AlarmDataFetch;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.gdxydgyhlw.heyuan.R;
import com.gdxydgyhlw.heyuan.databinding.FragmentHomeBinding;
import com.gdxydgyhlw.heyuan.eventbus.FetchAlarmCountSuccessEvent;
import com.gdxydgyhlw.heyuan.eventbus.FetchCollectSuccessEvent;
import com.gdxydgyhlw.heyuan.eventbus.FetchDeviceSuccessEvent;
import com.gdxydgyhlw.heyuan.eventbus.FetchSensorTypesCountSuccessEvent;
import com.gdxydgyhlw.heyuan.http.AlarmCountData;
import com.gdxydgyhlw.heyuan.http.ClientBuilder;
import com.gdxydgyhlw.heyuan.http.CollectData;
import com.gdxydgyhlw.heyuan.http.CollectFetch;
import com.gdxydgyhlw.heyuan.http.DeviceData;
import com.gdxydgyhlw.heyuan.http.DeviceFetch;
import com.gdxydgyhlw.heyuan.http.SensorTypesCountFetch;
import com.gdxydgyhlw.heyuan.http.SensorTypesData;
import com.gdxydgyhlw.heyuan.libs.Cache;
import com.gdxydgyhlw.heyuan.myenum.EnumColor;
import com.gdxydgyhlw.heyuan.myenum.SensorType;
import com.gdxydgyhlw.heyuan.tools.MyValueFormatter;
import com.gdxydgyhlw.heyuan.tools.Utils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private BarChart chart;
    private PieChart pieChart;
    public OkHttpClient client;
    public Spinner spinner;
    private List<DeviceData> deviceDataList;
    private List<CollectData> collectDataList;
    private List<SensorTypesData> sensorTypesDataList;
    private List<AlarmCountData> alarmCountDataList;
    private ProgressBar homeChartProgressBar;
    private LinearLayout emptyImage;
    private RefreshLayout refreshLayout;

    Handler handler = new Handler();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Context directBootContext = createDeviceProtectedStorageContext(requireActivity().getApplicationContext());
        assert directBootContext != null;
        SharedPreferences sp = directBootContext.getSharedPreferences(COOKIE, MODE_PRIVATE);
        client = ClientBuilder.getClient(sp, getContext());

        return root;
    }

    private void fetchDeviceList() {
        DeviceFetch getDeviceFetch = new DeviceFetch();
        getDeviceFetch.getDevice(client);
    }

    private void initSpinner(List<DeviceData> data) {
        spinner = (Spinner) requireActivity().findViewById(R.id.home_chart_spinner);
        initSpinnerChangeHandle();
        List<Map<String, String>> list = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            Map<String, String> map = new HashMap<>();
            map.put("key", data.get(i).name);
            map.put("id", String.valueOf(data.get(i).id));
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(requireActivity(), list, R.layout.spinner_wrapper, new String[]{"key"}, new int[]{R.id.home_chart_spinner_text});
        // 应用Adapter
        spinner.setAdapter(adapter);
        Cache cache = new Cache(getContext());
        //从缓存里读取选中位置
        String position = cache.readFromCache(cache.SPINNER_POSITION);
        if (!position.isEmpty() && this.deviceDataList != null) {
            int p = Integer.parseInt(position);
            if (this.deviceDataList.size() > p) {
                spinner.setSelection(p, true);
            }

        }
    }

    private void initLineBarChart() {
        chart = (BarChart) requireActivity().findViewById(R.id.home_bar_chart);
        chart.setDrawBarShadow(false);
        chart.setNoDataText("加载中...");
        chart.setNoDataTextColor(Color.rgb(20, 20, 30));
        emptyImage = (LinearLayout) requireActivity().findViewById(R.id.home_bar_chart_empty);
        homeChartProgressBar = (ProgressBar) requireActivity().findViewById(R.id.home_chart_progress);
        chart.getDescription().setEnabled(false);
        //隐藏图例
        chart.getLegend().setEnabled(false);
        //取消缩放、点击、高亮效果
        chart.setScaleEnabled(false);
        chart.setClickable(true);
        chart.setHighlightPerDragEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        /* 设置描述位置
         *Description description = chart.getDescription();
         *description.setText("数值");
         *description.setPosition(60,20);
         *description.setYOffset(20);
         *chart.setDescription(description);
         **/
        Description description = new Description();
        description.setEnabled(false);
        chart.setDescription(description);
        //获取X轴
        XAxis xAxis = chart.getXAxis();
        //设置X轴在下方（默认X轴在上方）
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //给X轴添加标签
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"一", "二", "三", "四", "五", "六", "七"}));
        //X轴网格线
        xAxis.setDrawGridLines(false);
        //获取左边Y轴
        YAxis leftYAxis = chart.getAxisLeft();
        //Y轴网格线
        leftYAxis.setDrawGridLines(true);
        //显示左边Y轴的标签
        leftYAxis.setDrawLabels(true);
        //获取右边Y轴
        YAxis rightYAxis = chart.getAxisRight();
        //右边Y轴
        rightYAxis.setEnabled(false);
        rightYAxis.enableGridDashedLine(10f, 10f, 0f);
        //不显示边框
        chart.setDrawBorders(false);
        chart.animateY(1000);
    }

    private void addDataToLineBarChart() {
        //添加数据
        List<BarEntry> entries = new ArrayList<>();
        XAxis xAxis = chart.getXAxis();
        String[] s = new String[10];
        for (int i = 0; i < this.collectDataList.size(); i++) {
            entries.add(new BarEntry(i, Float.parseFloat(this.collectDataList.get(i).analysisValue)));
            s[i] = String.valueOf(SensorType.getName(this.collectDataList.get(i).sensorType));
            // Log.v("sensor", String.valueOf(SensorType.getName(this.collectDataList.get(i).sensorType)));
        }
        //给X轴添加标签
        xAxis.setValueFormatter(new IndexAxisValueFormatter(s));
        //数据添加到数据集
        BarDataSet dataSet = new BarDataSet(entries, "最新的采集数据");
        //柱体颜色
        dataSet.setColor(Color.parseColor("#FF6363"));
        //显示数值
        dataSet.setDrawValues(true);
        //格式化yLabel，加单位
        dataSet.setValueFormatter(new MyValueFormatter(this.collectDataList));
        //数据集赋值给数据对象
        BarData data = new BarData(dataSet);
        //设置柱体宽带
        data.setBarWidth(0.3f);
        chart.setData(data);
        chart.invalidate();
        if (this.collectDataList.isEmpty()) {
            chart.setVisibility(View.GONE);
            emptyImage.setVisibility(View.VISIBLE);
        } else {
            emptyImage.setVisibility(View.GONE);
            chart.setVisibility(View.VISIBLE);
        }
    }

    private void initClickListener() {
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();       //X轴坐标 记得转 int
                // String y = String.valueOf(e.getY());       //当前柱状图Y轴值
                // e.getIcon();    //对应 BarEntry(float x, float y, Drawable icon)
                //e.getData();    //对应 BarEntry(float x, float y, Object data)
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                CollectData d = collectDataList.get(index);

                LinearLayout parent = View.inflate(requireActivity().getApplicationContext(), R.layout.home_marker_view_item, null).findViewById(R.id.home_marker_view_item);
                TextView textViewTitle1 = (TextView) parent.findViewById(R.id.home_marker_view_item_title1);
                TextView textViewTitle2 = (TextView) parent.findViewById(R.id.home_marker_view_item_title2);
                TextView textViewTitle3 = (TextView) parent.findViewById(R.id.home_marker_view_item_title3);
                TextView textViewValue1 = (TextView) parent.findViewById(R.id.home_marker_view_item_value1);
                TextView textViewValue2 = (TextView) parent.findViewById(R.id.home_marker_view_item_value2);
                TextView textViewValue3 = (TextView) parent.findViewById(R.id.home_marker_view_item_value3);
                textViewTitle1.setText("传感器类型");
                textViewTitle2.setText("采集的数据/单位");
                textViewTitle3.setText("采集时间");
                textViewValue1.setText(String.valueOf(SensorType.getName(d.sensorType)));
                textViewValue2.setText(String.valueOf(d.analysisValue + d.unit));
                textViewValue3.setText(d.createTime);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.setView(parent);
                builder.show();
            }

            @Override
            public void onNothingSelected() {
            }
        });
    }

    public void initSpinnerChangeHandle() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //当选中某一个数据项时触发该方法
            /*
             * parent接收的是被选择的数据项所属的 Spinner对象，
             * view参数接收的是显示被选择的数据项的TextView对象
             * position接收的是被选择的数据项在适配器中的位置
             * id被选择的数据项的行号
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //System.out.println(spinner==parent);//true
                //System.out.println(view);
                //String data = adapter.getItem(position);//从适配器中获取被选择的数据项
                //String data = list.get(position);//从集合中获取被选择的数据项
                HashMap<String, String> data = (HashMap<String, String>) spinner.getItemAtPosition(position);//从spinner中获取被选择的数据
                int deviceId = Integer.parseInt(Objects.requireNonNull(data.get("id")));
                //判断当前设备数据是否有缓存，如果有缓存的话直接读取缓存

                if (collectDataList != null && !collectDataList.isEmpty() && collectDataList.get(0).deviceManageId == deviceId) {
                    addDataToLineBarChart();
                } else {
                    fetchCollectDataList(deviceId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void fetchCollectDataList(int deviceId) {
        chartProgressHandle();
        CollectFetch collectFetch = new CollectFetch();
        collectFetch.getCollect(this.client, deviceId, Utils.todayStartTime(), Utils.todayEndTime());
    }

    private void initPieChat() {
        pieChart = (PieChart) requireActivity().findViewById(R.id.home_chart_pie);
        pieChart.setNoDataText("正在加载数据...");
        pieChart.setNoDataTextColor(Color.rgb(20, 20, 30));
        if (this.sensorTypesDataList != null && !this.sensorTypesDataList.isEmpty() && !String.valueOf(this.sensorTypesDataList).equals("null")) {

            addDataToPieChart();
        } else {
            getSensorTypesList();
        }

        Description description = new Description();
        description.setEnabled(false);
        pieChart.setDescription(description);
    }

    private void getSensorTypesList() {
        SensorTypesCountFetch sensorTypesCountFetch = new SensorTypesCountFetch();
        sensorTypesCountFetch.getSensorTypesCount(this.client);
    }

    private void addDataToPieChart() {

        Random random = new Random();
        List<PieEntry> pieEntryList = new ArrayList<>();
        int sum = this.sensorTypesDataList.stream()
                .mapToInt(p -> p.count)
                .sum();
        for (SensorTypesData data : this.sensorTypesDataList) {
            pieEntryList.add(new PieEntry((float) data.count / sum, SensorType.getName(data.sensorType)));
        }

        PieDataSet dataSet = new PieDataSet(pieEntryList, "设备类型统计");
        ArrayList<Integer> colors = new ArrayList<Integer>();
        int p = 0;
        for (PieEntry ignore : pieEntryList) {
            if (p < pieEntryList.size()) {
                colors.add(Color.parseColor(EnumColor.getName(p)));
                p++;
            } else {
                int r = random.nextInt(200);
                int g = random.nextInt(200);
                int b = random.nextInt(200);
                colors.add(Color.rgb(r, g, b));
            }
        }
        dataSet.setColors(colors);
        PieData pieData = new PieData(dataSet);
        pieData.setDrawValues(true);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void addDataToAlarmView() {
        TextView textView1 = (TextView) requireActivity().findViewById(R.id.degree_lever_1);
        TextView textView2 = (TextView) requireActivity().findViewById(R.id.degree_lever_2);
        TextView textView3 = (TextView) requireActivity().findViewById(R.id.degree_lever_3);
        TextView textView4 = (TextView) requireActivity().findViewById(R.id.degree_lever_4);
        for (AlarmCountData alarmCountData : this.alarmCountDataList) {
            switch (alarmCountData.alarmGrade) {
                case "low":
                    textView1.setText(String.valueOf(alarmCountData.count));
                    break;
                case "medium":
                    textView2.setText(String.valueOf(alarmCountData.count));
                    break;
                case "serious":
                    textView3.setText(String.valueOf(alarmCountData.count));
                    break;
                case "emergent":
                    textView4.setText(String.valueOf(alarmCountData.count));
                    break;
            }
        }

    }

    private void initRefreshComponent() {
        this.refreshLayout = (RefreshLayout) requireActivity().findViewById(R.id.scrollView2);
        this.refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        this.refreshLayout.setEnableLoadMore(false);
        // 禁止上拉加载
        //  refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        this.refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                onRefreshHandle();
                refreshLayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            }
        });

    }

    private void initAlarmCount() {

        AlarmDataFetch alarmDataFetch = new AlarmDataFetch();
        alarmDataFetch.getAlarmCount(client);
    }

    private void onRefreshHandle() {
        initAlarmCount();
        if (this.spinner != null && this.spinner.getSelectedItemPosition() >= 0) {
            int i = this.spinner.getSelectedItemPosition();
            fetchCollectDataList(this.deviceDataList.get(i).id);
        } else {
            fetchDeviceList();
        }
        getSensorTypesList();
    }

    private void chartProgressHandle() {
        showChartLoadingBar();
        Runnable runnable = this::hideChartLoadingBar;
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

    private void showChartLoadingBar() {
        homeChartProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideChartLoadingBar() {
        homeChartProgressBar.setVisibility(View.GONE);

    }

    /**
     * 从设备缓存中读取设备列表数据
     *
     * @param data {String} JSON字符串
     */
    public void getDeviceDataFromCache(String data) {
        Type intArrayType = new TypeToken<List<DeviceData>>() {
        }.getType();
        this.deviceDataList = new Gson().fromJson(data, intArrayType);
        initSpinner(this.deviceDataList);
    }

    /**
     * 从设备缓存中读取设备列表数据
     *
     * @param data {String} JSON字符串
     */
    public void getCollectDataFromCache(String data) {
        Type intArrayType = new TypeToken<List<CollectData>>() {
        }.getType();
        this.collectDataList = new Gson().fromJson(data, intArrayType);
    }

    private void getSensorTypeDataFromCache(String data) {
        Type intArrayType = new TypeToken<List<SensorTypesData>>() {
        }.getType();
        this.sensorTypesDataList = new Gson().fromJson(data, intArrayType);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getDeviceData(FetchDeviceSuccessEvent event) {
        this.deviceDataList = event.getData();
        Collections.reverse(this.deviceDataList);
        initSpinner(this.deviceDataList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getCollectData(FetchCollectSuccessEvent event) {
        this.collectDataList = event.getData();
        addDataToLineBarChart();
        hideChartLoadingBar();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getSensorCountList(FetchSensorTypesCountSuccessEvent event) {
        this.sensorTypesDataList = event.getData();
        addDataToPieChart();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getAlarmCountList(FetchAlarmCountSuccessEvent event) {
        this.alarmCountDataList = event.getData();
        addDataToAlarmView();
    }

    private void saveDataToCache() {
        Gson gson = new Gson();
        Cache cache = new Cache(getContext());
        if (this.deviceDataList != null) {
            String deviceListStr = gson.toJson(this.deviceDataList);
            cache.saveToCache(cache.DEVICES, deviceListStr);
        }
        if (this.collectDataList != null) {
            String collectListStr = gson.toJson(this.collectDataList);
            cache.saveToCache(cache.COLLECTS, collectListStr);
        }
        if (this.spinner != null) {
            cache.saveToCache(cache.SPINNER_POSITION, String.valueOf(spinner.getSelectedItemPosition()));
        }
        if (this.sensorTypesDataList != null) {
            String sensorTypesListStr = gson.toJson(this.sensorTypesDataList);
            cache.saveToCache(cache.PIE, sensorTypesListStr);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveDataToCache();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        initLineBarChart();
        initPieChat();
        //barChart添加点击事件
        initClickListener();
        initRefreshComponent();
        initAlarmCount();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        Cache cache = new Cache(getContext());
        @Nullable String deviceListStr = cache.readFromCache(cache.DEVICES);
        @Nullable String collectListStr = cache.readFromCache(cache.COLLECTS);
        @Nullable String sensorTypesListStr = cache.readFromCache(cache.PIE);
        if (deviceListStr != null && !deviceListStr.isEmpty() && !deviceListStr.equals("null")) {
            getDeviceDataFromCache(deviceListStr);
        } else {
            fetchDeviceList();
        }
        if (collectListStr != null && !collectListStr.isEmpty() && !collectListStr.equals("null")) {

            getCollectDataFromCache(collectListStr);
        }
        if (sensorTypesListStr != null && !sensorTypesListStr.isEmpty() && !sensorTypesListStr.equals("null")) {
            getSensorTypeDataFromCache(sensorTypesListStr);
        }
        super.onResume();
    }
}