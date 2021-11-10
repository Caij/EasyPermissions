package com.caij.easypermissions;

import androidx.annotation.NonNull;

import java.util.List;

public interface PermissionListener {

    void onRequestPermissionsResult(boolean allGranted, @NonNull List<String> grantResults, @NonNull List<String> deniedResults);
}
