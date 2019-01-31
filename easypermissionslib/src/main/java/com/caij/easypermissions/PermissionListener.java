package com.caij.easypermissions;

import androidx.annotation.NonNull;

public interface PermissionListener {

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
}
