package com.ocean.bluectrl.util;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtil {

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
