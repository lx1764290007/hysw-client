package com.gdxydgyhlw.heyuan.ui.setting;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.createDeviceProtectedStorageContext;
import static com.gdxydgyhlw.heyuan.http.Service.COOKIE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.gdxydgyhlw.heyuan.Login;
import com.gdxydgyhlw.heyuan.MainActivity;
import com.gdxydgyhlw.heyuan.R;
import com.gdxydgyhlw.heyuan.databinding.FragmentSettingBinding;
import com.gdxydgyhlw.heyuan.eventbus.LogoutEvent;
import com.gdxydgyhlw.heyuan.http.ClientBuilder;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import com.gdxydgyhlw.heyuan.http.LogoutFetch;
import com.gdxydgyhlw.heyuan.libs.Cache;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import okhttp3.OkHttpClient;

public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;
    private OkHttpClient client;
    private ProgressBar progressBar;
    private TextView textCache;
    private Button logoutButton;
    ImageView imageView;
    AlertDialog.Builder builder;
    private LinearLayout linearLayout;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Context directBootContext = createDeviceProtectedStorageContext(requireActivity().getApplicationContext());
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        SharedPreferences sp = directBootContext.getSharedPreferences(COOKIE, MODE_PRIVATE);
        client = ClientBuilder.getClient(sp, getContext());
        EventBus.getDefault().register(this);
        return binding.getRoot();
    }
    private void computedCache(){
        this.progressBar.setVisibility(View.VISIBLE);

        final String[] cacheSize = {""};
        Handler handler = new Handler();
        Runnable runnable = () -> {
            progressBar.setVisibility(View.GONE);
            textCache.setVisibility(View.VISIBLE);
            textCache.setText(cacheSize[0]);
            imageView.setVisibility(View.VISIBLE);
        };
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Cache cache = new Cache(requireContext());
                        cacheSize[0] = cache.getCacheSize();
                        handler.post(runnable);
                    } catch (IllegalStateException e){
                        Log.i("IllegalStateException", Objects.requireNonNull(e.getMessage()));
                    }

                }
            }, 1000);
        } catch (NullPointerException e) {
            Log.i("timer.schedule", Objects.requireNonNull(e.getMessage()));
        }
    }
    private void initClearCacheHandler(){
        this.linearLayout = (LinearLayout) requireActivity().findViewById(R.id.clear_item);
        this.linearLayout.setOnClickListener(view -> {
            Cache cache = new Cache(requireContext());
            cache.deleteCache();
            textCache.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            computedCache();
        });
    }
    private void modifyButtonState() {
        logoutButton.setText("稍等一会儿...");
        logoutButton.setEnabled(false);
        Handler handler = new Handler();
        Runnable runnable = () -> {
            logoutButton.setText(R.string.logout);
            logoutButton.setEnabled(true);
        };
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        handler.post(runnable);
                    } catch (IllegalStateException e) {
                        Log.i("IllegalStateException", Objects.requireNonNull(e.getMessage()));
                    }
                }
            }, 2000);
        } catch (NullPointerException e) {
            Log.i("timer.schedule", Objects.requireNonNull(e.getMessage()));
        }
    }
    private void initDialog(){
        builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("确定要登出吗？");//设置弹出对话框的标题
        builder.setIcon(R.drawable.alert_icon);//设置弹出对话框的图标
        builder.setMessage("此操作将会让当前登录状态失效并返回到登录界面");//设置弹出对话框的内容
        builder.setCancelable(true);//能否被取消
        builder.setPositiveButton("确定", (dialogInterface, i) -> {
            modifyButtonState();
            LogoutFetch logoutFetch = new LogoutFetch();
            logoutFetch.logout(client);
            dialogInterface.cancel();
        });
        builder.setNegativeButton("取消", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
    }
    private void initLogout(){
        this.logoutButton = (Button) requireActivity().findViewById(R.id.logout);
        this.logoutButton.setOnClickListener(view -> {
            builder.show();
        });
    }
    @Subscribe
    public void onLogout(LogoutEvent event){
        Intent intent = new Intent();
        intent.setClass(requireActivity(), Login.class);
        startActivity(intent);
    }
    @Override
    public void onStart() {
       super.onStart();
       this.progressBar = (ProgressBar) requireActivity().findViewById(R.id.progressBar);
       this.textCache = (TextView) requireActivity().findViewById(R.id.cache_total);
       this.imageView = (ImageView) requireActivity().findViewById(R.id.arrow_right);
       initLogout();
       initDialog();
       initClearCacheHandler();
       computedCache();
   }
   @Override
   public void onStop(){
       super.onStop();
       EventBus.getDefault().unregister(this);
   }
}
