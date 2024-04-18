package com.gdxydgyhlw.heyuan;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.gdxydgyhlw.heyuan.eventbus.ResponseErrorEvent;
import com.gdxydgyhlw.heyuan.eventbus.UnauthorizedEvent;
import com.gdxydgyhlw.heyuan.eventbus.UnknownErrorEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.gdxydgyhlw.heyuan.databinding.ActivityMainBinding;
import com.gdxydgyhlw.heyuan.libs.Cache;
import com.gdxydgyhlw.heyuan.libs.Permission;
import com.gdxydgyhlw.heyuan.tools.Notification;
import com.hjq.toast.ToastParams;
import com.hjq.toast.Toaster;
import com.hjq.toast.style.CustomToastStyle;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.Objects;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EventBus.getDefault().register(this);
        Toaster.init(getApplication());
        // BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_setting)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        hideSystemUI();
        Objects.requireNonNull(getSupportActionBar()).hide();
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        initSocket();
        initNotification();
    }

    private void openLoginPage() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, Login.class);
        startActivity(intent);
    }

    private String createNotificationChannel(String channelID, String channelNAME, int level) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);
            manager.createNotificationChannel(channel);
            return channelID;
        } else {
            return null;
        }
    }

    private void initNotification() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Notification.requestNotificationPermission(getApplicationContext());
        } else {
            Intent intent = new Intent(this, MainActivity.class).putExtra("to_notification", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            String channelId = createNotificationChannel(Notification.NOTIFY_ID, Notification.NOTIFY_NAME, NotificationManager.IMPORTANCE_HIGH);
            notification = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("设备采集到异常数据")
                    .setContentText("点击查看详情")
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.alert)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);
            notificationManager = NotificationManagerCompat.from(this);
        }

    }
    @SuppressLint("MissingPermission")
    private void showNotification(){
        if(notificationManager != null && notification != null){
            notificationManager.notify(100, notification.build());
        }
    }


     private void initSocket() {
        Runnable runnable = this::showNotification;
         new Thread(() -> {
             //需要在子线程中处理的逻辑
             try {
                 WebSocketClient client = new WebSocketClient(new URI("wss://itao-tech.com/api/connectWebSocket/homePage")) {
                     @Override
                     public void onOpen(ServerHandshake handshake) {
                         // 连接建立成功，启动心跳定时器
                         Log.i("onOpen", "onOpen");
                     }
                     @Override
                     public void onMessage(String message) {
                         Log.i("onMessage", String.valueOf(message));
                         runnable.run();
                     }
                     @Override
                     public void onClose(int code, String reason, boolean remote) {
                         // 连接关闭，停止心跳定时器
                         Log.i("handshake", String.valueOf(reason));
                         TimerTask task = new TimerTask() {
                             @Override
                             public void run() {
                                 // 逻辑处理
                                 initSocket();
                             }
                         };
                         Timer timer = new Timer();
                         timer.schedule(task, 5000); // 此处delay为0表示没有延迟，立即执行一次task
                     }
                     @Override
                     public void onError(Exception ex) {
                         // 发生异常，停止心跳定时器
                         Log.i("onError",  String.valueOf(ex));
                     }
                 };
                 client.connect();
             } catch (URISyntaxException e) {
                 Log.i("RuntimeException",  String.valueOf(e.getMessage()));

             }
         }).start();

     }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResponseError(ResponseErrorEvent event) {
        Log.v("onResponseError", event.getResponseError().message);
        ToastParams params = new ToastParams();
        params.text = event.getResponseError().message;
        params.style = new CustomToastStyle(R.layout.toast_error);
        Toaster.show(params);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnauthorized(UnauthorizedEvent event) {
        ToastParams params = new ToastParams();
        params.text = event.getError().message;
        params.style = new CustomToastStyle(R.layout.toast_error);
        Toaster.show(params);
        openLoginPage();
        Log.v("onUnauthorized", event.getError().message);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnknownError(UnknownErrorEvent event) {
        ToastParams params = new ToastParams();
        params.text = event.getUnknownError().message;
        params.style = new CustomToastStyle(R.layout.toast_error);
        Toaster.show(params);
    }
    private void removeCache(){
        Cache cache = new Cache(this);
        cache.deleteCache();
        Log.i("removeCache","removeCache");
    }
    @Override
    protected void onDestroy() {
        // 取消订阅
        EventBus.getDefault().unregister(this);
        // 删除缓存
        removeCache();
        super.onDestroy();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                // remove the following flag for version < API 19
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    @Override
    protected void onResume() {
        if(!new Permission().hasInternetAccess(getApplicationContext())){
            startActivity(new Intent(getApplicationContext(), NetworkPermissions.class));
        }
        super.onResume();

    }

}