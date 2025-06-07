package com.ocean.bluectrl.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {

    // 动态生成所需权限列表（根据Android版本）
    private static String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();

        // 所有需要蓝牙扫描的设备都需要位置权限（Android 6.0+）
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        // Android 12及以上使用新的蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        } else {
            // Android 9-11需要旧的蓝牙权限（BLUETOOTH和BLUETOOTH_ADMIN）
            permissions.add(Manifest.permission.BLUETOOTH);
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        return permissions.toArray(new String[0]);
    }

    public static boolean checkPermission (Activity act, String[] permissions, int requestCode) {
        int check = PackageManager.PERMISSION_GRANTED;
        for (String permission : permissions) {
            check = ContextCompat.checkSelfPermission(act, permission);
            if (check != PackageManager.PERMISSION_GRANTED) {
                break;
            }
        }
        if (check != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(act, permissions, requestCode);
            return false;
        }
        return true;
    }

    // 检查并请求权限（兼容多版本）
    public static boolean checkAndRequestPermissions(Activity activity, int requestCode) {
        String[] permissions = getRequiredPermissions();
        int checkResult = PackageManager.PERMISSION_GRANTED;

        // 检查所有权限是否已授予
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(activity, permission);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                checkResult = permissionCheck;
                break;
            }
        }

        // 有未授予的权限则请求
        if (checkResult != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            return false;
        }
        return true;
    }

    public static boolean checkGrant(int[] grantResults) {
        if (grantResults != null) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
