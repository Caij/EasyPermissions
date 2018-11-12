package com.caij.easypermissions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Created by Ca1j on 2017/8/10.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class RequestPermissionActivity extends Activity {

    private static final String PERMISSION_KEYS = "KEY_INPUT_PERMISSIONS";
    private static final String REQUEST_CODE = "request_code";

    public static PermissionListener sPermissionListener;

    public static Intent newIntent(Context context, String[] permissions, int requestCode) {
        return new Intent(context, RequestPermissionActivity.class)
                .putExtra(PERMISSION_KEYS, permissions)
                .putExtra(REQUEST_CODE, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        String[] permissions = intent.getStringArrayExtra(PERMISSION_KEYS);
        int requestCode = intent.getIntExtra(REQUEST_CODE, 1);
        if(permissions == null) {
            finish();
            overridePendingTransition(0, 0);
        } else if(sPermissionListener != null) {
            requestPermissions(permissions, requestCode);
        }
    }

    public static void setPermissionListener(RequestPermissionActivity.PermissionListener permissionListener) {
        sPermissionListener = permissionListener;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(sPermissionListener != null) {
            sPermissionListener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        sPermissionListener = null;
        this.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sPermissionListener = null;
    }

    interface PermissionListener {
        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }
}
