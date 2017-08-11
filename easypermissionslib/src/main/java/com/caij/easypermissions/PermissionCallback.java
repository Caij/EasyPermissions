package com.caij.easypermissions;

import java.util.List;

/**
 * Created by caij on 2017/8/11.
 */

public interface PermissionCallback {

    void onPermissionsGranted(int requestCode, List<String> permissions);

    void onPermissionsDenied(int requestCode, List<String> permissions);

    void onNeverAskAgainPermission(int requestCode, List<String> permissions);
}
