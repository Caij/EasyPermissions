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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility to request and check System permissions for apps targeting Android M (API >= 23).
 */
public class EasyPermissions {

    private static final String TAG = "EasyPermissions";
    private static final String SP_FILE_NAME = EasyPermissions.class.getName() + ".permissions";

    private static final String FRAGMENT_TAG = "permission_fragment_tag";

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

            // DANGER ZONE!!! Changing this will break the library.
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

    public static void requestPermissions(@NonNull Fragment fragment, int requestCode,
                                          final PermissionCallback permissionCallback, @NonNull String... permissions) {
        if (fragment.getActivity() != null) {
            requestPermissions(fragment.getActivity(), requestCode, permissionCallback, permissions);
        }
    }

    /**
     *  Request permissions
     *
     * @param activity
     * @param requestCode request code to track this request, must be < 256.
     * @param permissionCallback request permission callback
     * @param permissions   a set of permissions to be requested.
     */
    @SuppressLint("NewApi")
    public static void requestPermissions(@NonNull FragmentActivity activity, int requestCode,
                                          final PermissionCallback permissionCallback, @NonNull String... permissions) {
        if (hasPermissions(activity, permissions)) {
            notifyAlreadyHasPermissions(requestCode, permissions, permissionCallback);
            return;
        }

        if (hasNeverAskAgainPermission(activity, permissions)) {
            notifyNeverAskAgainPermission(permissionCallback, requestCode, permissions);
        } else {
            requestPermissions(activity, activity.getSupportFragmentManager(), requestCode, permissionCallback, permissions);
        }
    }

    private static void requestPermissions(final Context context, @NonNull FragmentManager fragmentManager, int requestCode,
                                           final PermissionCallback permissionCallback, @NonNull String... permissions) {
        Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                onRequestPermissionsResultNotify(context, requestCode, permissions, grantResults, permissionCallback);
            }
        };
        if (fragment instanceof PermissionFragment) {
            PermissionFragment permissionFragment = (PermissionFragment) fragment;
            permissionFragment.setArguments(PermissionFragment.newArgs(permissions, requestCode));
            permissionFragment.setPermissionListener(permissionListener);
            permissionFragment.requestPermissions();
        } else {
            PermissionFragment permissionFragment = PermissionFragment.newInstance(permissions, requestCode);
            fragmentManager.beginTransaction().add(permissionFragment, FRAGMENT_TAG).commit();
        }
    }


    private static void onRequestPermissionsResultNotify(Context context, int requestCode,
                                                         @NonNull String[] permissions,
                                                         @NonNull int[] grantResults,
                                                         @NonNull PermissionCallback... receivers) {
        // Make a collection of granted and denied permissions from the request.
        List<String> granted = new ArrayList<>(permissions.length);
        List<String> denied = new ArrayList<>(permissions.length);
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];

            tagFirstRequest(context, perm);

            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        // iterate through all receivers
        for (PermissionCallback permissionCallback : receivers) {
            // Report granted permissions, if any.
            if (!granted.isEmpty()) {
                   permissionCallback.onPermissionsGranted(requestCode, granted);
            }

            // Report denied permissions, if any.
            if (!denied.isEmpty()) {
                permissionCallback.onPermissionsDenied(requestCode, denied);
            }
        }
    }

    private static void onNeverAskAgainPermission(int requestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull PermissionCallback... receivers) {
        for (PermissionCallback permissionCallback : receivers) {
            permissionCallback.onNeverAskAgainPermission(requestCode, Arrays.asList(permissions));
        }
    }

    private static void notifyAlreadyHasPermissions(int requestCode,
                                                    @NonNull String[] permissions, @NonNull PermissionCallback... receivers) {
        for (PermissionCallback permissionCallback : receivers) {
            permissionCallback.onPermissionsGranted(requestCode, Arrays.asList(permissions));
        }
    }

    private static void notifyNeverAskAgainPermission(PermissionCallback permissionCallback,
                                                      int requestCode,
                                                      @NonNull String[] permissions) {
        onNeverAskAgainPermission(requestCode, permissions, permissionCallback);
    }

    private static boolean hasNeverAskAgainPermission(@NonNull Activity activity, @NonNull String[] permissions) {
        boolean isPermissionNotPromptedAgain = false;
        for (String permission : permissions) {
            boolean isPermissionNotPromptedPerm = !shouldShowRequestPermissionRationale(activity, permission)
                    && !isFirstRequest(activity, permission)
                    && !hasPermissions(activity, permission);
            isPermissionNotPromptedAgain = isPermissionNotPromptedAgain
                    || isPermissionNotPromptedPerm;
        }
        return isPermissionNotPromptedAgain;
    }

    private static boolean shouldShowRequestPermissionRationale(@NonNull Activity activity,
                                                                @NonNull String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    private static boolean isFirstRequest(Context context, String permission) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(permission, true);
    }

    private static void tagFirstRequest(Context context, String perm) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putBoolean(perm, false)
                .apply();
    }
}
