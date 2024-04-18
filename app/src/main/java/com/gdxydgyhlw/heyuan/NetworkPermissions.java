package com.gdxydgyhlw.heyuan;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;

import com.gdxydgyhlw.heyuan.libs.Permission;
import com.hjq.toast.Toaster;

import java.util.Objects;

public class NetworkPermissions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_permissions);
        hideSystemUI();
        Objects.requireNonNull(getSupportActionBar()).hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        Toaster.init(getApplication());
    }
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                // remove the following flag for version < API 19
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Button button = (Button) findViewById(R.id.network_permission_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(NetworkPermissions.this, new String[]{Manifest.permission.INTERNET}, Permission.INTERNET_PERMISSION_CODE);
            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(new Permission().hasInternetAccess(getApplicationContext())){
            finish();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Permission.INTERNET_PERMISSION_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // network-related task you need to do.
                // Do your network operations here

                if(!new Permission().hasInternetAccess(getApplicationContext())){
                    Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
                    startActivity(intent);
                } else {
                    finish();
                }

            } else {
                Toaster.show("未授权");
            }
        }
    }
}