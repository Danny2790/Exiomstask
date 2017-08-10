package com.akash.exiomstask.Utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.akash.exiomstask.Constants.Constant;

/**
 * Created by akash on 8/10/2017.
 */

public class Utils {

    public static Boolean LocationPermissionAvailable(Context context) {
        Boolean permissionGranted = false;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true;
        }
        return permissionGranted;
    }

    public static void showLocationAccessDialog(Activity context) {
        ActivityCompat.requestPermissions(context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                Constant.REQUEST_LOCATION);
    }

}
