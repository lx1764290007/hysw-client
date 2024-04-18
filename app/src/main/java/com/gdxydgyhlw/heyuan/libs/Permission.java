package com.gdxydgyhlw.heyuan.libs;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;



public class Permission {
    public static int INTERNET_PERMISSION_CODE =  10086;
    // 检查网络权限
    private boolean hasInternetPermission(Context context) {
        try {
            // 通过PackageManager获取应用的信息
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            // 获取应用的权限列表
            String[] permissions = info.requestedPermissions;
            // 判断INTERNET权限是否在权限列表中
            for (String permission : permissions) {
                if (Manifest.permission.INTERNET.equals(permission)) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isAvailable();
    }
    public Boolean hasInternetAccess(Context context){
        return hasInternetPermission(context) && isNetworkAvailable(context);
    }
}
