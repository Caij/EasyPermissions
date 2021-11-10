/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.caij.easypermissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

/**
 * Utility to request and check System permissions for apps targeting Android M (API >= 23).
 */
public class Permissions {

    private static final String TAG = "EasyPermissions";


    static final int SHOW_REASON_TYPE_NONE = 1;
    static final int SHOW_REASON_TYPE_BEFORE = 2;
    static final int SHOW_REASON_TYPE_AFTER = 3;

    static final int REQUEST_PERMISSION_CODE = 1013;
    static final int REQUEST_SETTING = 1011;

    String[] permissions;
    int showReasonType = SHOW_REASON_TYPE_NONE;
    PermissionListener permissionListener;
    final FragmentActivity fragmentActivity;
    PermissionDialog permissionDialog;

    public Permissions(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context the calling context.
     * @param permissions   one ore more permissions, such as {@link Manifest.permission#CAMERA}.
     * @return true if all permissions are already granted, false if at least one permission is not
     * yet granted.
     * @see Manifest.permission
     */
    public static boolean hasPermissions(@NonNull Context context, @NonNull String... permissions) {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default");
            return true;
        }

        for (String permission : permissions) {
            boolean hasPermission = (ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                return false;
            }
        }

        return true;
    }

    public static Permissions with(Fragment fragment) {
        return with(fragment.getActivity());
    }

    public static Permissions with(FragmentActivity fragmentActivity) {
        return new Permissions(fragmentActivity);
    }

    public Permissions permissions(String... permissions) {
        this.permissions = permissions;
        return this;
    }

    public Permissions showReasonBeforeRequest() {
        return showReasonBeforeRequest(new DefaultPermissionDialog());
    }

    public Permissions showReasonAfterRequest() {
        return showReasonAfterRequest(new DefaultPermissionDialog());
    }

    public Permissions showReasonBeforeRequest(PermissionDialog permissionDialog) {
        showReasonType = SHOW_REASON_TYPE_BEFORE;
        this.permissionDialog = permissionDialog;
        return this;
    }

    public Permissions showReasonAfterRequest(PermissionDialog permissionDialog) {
        showReasonType = SHOW_REASON_TYPE_AFTER;
        this.permissionDialog = permissionDialog;
        return this;
    }

    public void request(PermissionListener permissionListener) {
        this.permissionListener = permissionListener;

        if (permissions == null || permissions.length == 0) {
            throw new NullPointerException("permission null");
        }

        if (permissionDialog == null) {
            permissionDialog = new DefaultPermissionDialog();
        }

        PermissionManager permissionManager = new PermissionManager(this);
        permissionManager.request();
    }

    public static boolean askNever(@NonNull Activity activity,
                                                                @NonNull String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    public static boolean askNever(@NotNull FragmentActivity fragmentActivity, @NotNull String[] permissions) {
        for (String permission : permissions) {
            if (askNever(fragmentActivity, permission)) return true;
        }
        return false;
    }
}
