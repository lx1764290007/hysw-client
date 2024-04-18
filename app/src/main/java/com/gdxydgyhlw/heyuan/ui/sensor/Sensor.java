package com.gdxydgyhlw.heyuan.ui.sensor;

import static com.gdxydgyhlw.heyuan.http.Service.COOKIE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gdxydgyhlw.heyuan.R;
import com.gdxydgyhlw.heyuan.eventbus.FetchSensorListSuccessEvent;
import com.gdxydgyhlw.heyuan.http.ClientBuilder;
import com.gdxydgyhlw.heyuan.http.SensorData;
import com.gdxydgyhlw.heyuan.http.SensorFetch;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;

public class Sensor extends Activity {
    private OkHttpClient client;
    private RecyclerView recyclerView;
    private List<SensorData> sensorDataList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        Context directBootContext = createDeviceProtectedStorageContext();
        SharedPreferences sp = directBootContext.getSharedPreferences(COOKIE, MODE_PRIVATE);
        client = ClientBuilder.getClient(sp, this);
        hideSystemUI();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        EventBus.getDefault().register(this);
        initRecycleView();

    }

    private void setItem(List<SensorData> data) {
        if (this.sensorDataList != null && this.sensorDataList.isEmpty()) {
            findViewById(R.id.sensor_empty_view).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.sensor_empty_view).setVisibility(View.GONE);
        }
        recyclerView.setAdapter(new NormalAdaptor(data));
        hideProgressbar();
    }

    private void showProgressbar() {
        if (this.progressBar.getVisibility() == View.GONE) {
            Runnable runnable = this::hideProgressbar;
            Handler handler = new Handler();
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
            this.progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressbar() {
        if (this.progressBar.getVisibility() == View.VISIBLE) {
            this.progressBar.setVisibility(View.GONE);
        }
    }

    private void getData() {
        showProgressbar();
        Intent intent = getIntent();
        if (intent != null) {
            int value = intent.getIntExtra("deviceManageId", 0);

            String prefix = getString(R.string.notify_title_prefix);
            String title = prefix + intent.getStringExtra("deviceName");
            ((TextView) findViewById(R.id.title_template)).setText(title);
            SensorFetch sensorFetch = new SensorFetch(value);
            sensorFetch.getSensorList(client);
        } else {
            SensorFetch sensorFetch = new SensorFetch();
            sensorFetch.getSensorList(client);
        }

    }

    private void initRecycleView() {
        this.progressBar = (ProgressBar) findViewById(R.id.sensor_load_progress);
        this.recyclerView = (RecyclerView) findViewById(R.id.sensor_main_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        this.recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                // remove the following flag for version < API 19
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Subscribe
    public void getSensorListSuccess(FetchSensorListSuccessEvent event) {
        this.sensorDataList = event.getData();
        setItem(this.sensorDataList);
    }

    @Override
    public void onStart() {
        super.onStart();
        getData();
        ImageButton imageButton = (ImageButton) findViewById(R.id.action_image);
        imageButton.setOnClickListener(event -> {
            finish();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}