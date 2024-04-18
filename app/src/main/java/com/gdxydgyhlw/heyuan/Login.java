/**
 * Login Activity 是利用 事件订阅-发布的机制来实现跨进程通信的（okhttp3是异步）
 * Loading 以及 Toast都是封装好的，如果需要修改建议修改全局的
 * okhttp3 请求以及响应封装过滤请看 com.gdxydgyhlw.heyuan.http RequestInterceptor 以及 ResponseInterceptor
 */
package com.gdxydgyhlw.heyuan;
import static com.gdxydgyhlw.heyuan.http.Service.COOKIE;
import static com.gdxydgyhlw.heyuan.http.Service.LOGIN_NAME;
import static com.gdxydgyhlw.heyuan.http.Service.USER_ID;
import static com.gdxydgyhlw.heyuan.http.Service.USER_NAME;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.gdxydgyhlw.heyuan.eventbus.ResponseErrorEvent;
import com.gdxydgyhlw.heyuan.libs.Permission;
import com.gdxydgyhlw.heyuan.tools.Utils;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.hjq.toast.ToastParams;
import com.hjq.toast.Toaster;
import com.gdxydgyhlw.heyuan.eventbus.LoginSuccessEvent;
import com.gdxydgyhlw.heyuan.http.ClientBuilder;
import com.gdxydgyhlw.heyuan.http.LoginFetch;
import com.hjq.toast.style.CustomToastStyle;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;

public class Login extends Activity {
    LoginFetch loginFetch = new LoginFetch();

    public OkHttpClient client;
    SharedPreferences sp;
    ProgressBar progressBar;
    Button loginButton;
    EditText loginName;
    EditText password;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginSuccess(LoginSuccessEvent event) {
        //保存一部分登录信息
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(USER_ID, event.getData().id);
        editor.putString(USER_NAME, event.getData().name);
        editor.putString(LOGIN_NAME, loginName.getText().toString());
        editor.apply();
        ToastParams params = new ToastParams();
        params.text = "登录成功";
        params.style = new CustomToastStyle(R.layout.toast_success);
        Toaster.show(params);
        loginButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        finish();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginFail(ResponseErrorEvent event) {
        Log.v("fail", event.getResponseError().message);
        ToastParams params = new ToastParams();
        params.text = event.getResponseError().message;
        params.style = new CustomToastStyle(R.layout.toast_error);
        Toaster.show(params);
        loginButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    @Subscribe
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        hideSystemUI();

        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        Toaster.init(Login.this.getApplication());
        //注册订阅者
        EventBus.getDefault().register(this);
        Context directBootContext = createDeviceProtectedStorageContext();
        sp = directBootContext.getSharedPreferences(COOKIE, MODE_PRIVATE);
        client = ClientBuilder.getClient(sp, getApplicationContext());
        progressBar = (ProgressBar)findViewById(R.id.login_progress);
        Sprite doubleBounce = new DoubleBounce();
        progressBar.setIndeterminateDrawable(doubleBounce);
        loginButton = (Button) findViewById(R.id.login_button);
        //自动填充上一次登录的账号
        if(!TextUtils.isEmpty(sp.getString(LOGIN_NAME, ""))){
            ((EditText) findViewById(R.id.edit_text_login_name)).setText(sp.getString(LOGIN_NAME, ""));
        };
        ((EditText) findViewById(R.id.edit_text_login_name)).setFilters(new InputFilter[]{Utils.filter});
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmitHandle();
            }
        });
       // 密码框回车
        ((EditText) findViewById(R.id.edit_text_login_password)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== EditorInfo.IME_ACTION_GO) {
                    onSubmitHandle();
                }
                return false;
            }
        });
    }

    /**
     * 表单提交，简单验证是否为空
     */
    private void onSubmitHandle(){
        loginName = (EditText) findViewById(R.id.edit_text_login_name);
        password = (EditText) findViewById(R.id.edit_text_login_password);
        @Nullable String loginNameValue = loginName.getText().toString();
        @Nullable String loginPasswordValue = password.getText().toString();

        if (!TextUtils.isEmpty(loginNameValue) && !TextUtils.isEmpty(loginPasswordValue)) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            loginFetch.login(loginNameValue, loginPasswordValue, Login.this);
            Handler handler = new Handler();
            Runnable runnable = () -> {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
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
        } else {
            ToastParams params = new ToastParams();
            params.text = "请填写账号和密码";
            params.style = new CustomToastStyle(R.layout.toast_warn);
            Toaster.show(params);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if(!new Permission().hasInternetAccess(getApplicationContext())){
            startActivity(new Intent(getApplicationContext(), NetworkPermissions.class));
        }
        super.onResume();
    }
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                // remove the following flag for version < API 19
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}