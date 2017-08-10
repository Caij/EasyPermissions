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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility to request and check System permissions for apps targeting Android M (API >= 23).
 */
public class EasyPermissions {

    public interface PermissionCallbacks {

        void onPermissionsGranted(int requestCode, List<String> perms);

        void onPermissionsDenied(int requestCode, List<String> perms);

        void onNeverAskAgainPermission(int requestCode, List<String> perms);
    }

    private static final String TAG = "EasyPermissions";
    private static final String DIALOG_TAG = "RationaleDialogFragmentCompat";
    private static final String SP_FILE_NAME = EasyPermissions.class.getName() + ".permissions";

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context the calling context.
     * @param perms   one ore more permissions, such as {@link Manifest.permission#CAMERA}.
     * @return true if all permissions are already granted, false if at least one permission is not
     * yet granted.
     * @see Manifest.permission
     */
    public static boolean hasPermissions(@NonNull Context context, @NonNull String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default");

            // DANGER ZONE!!! Changing this will break the library.
            return true;
        }

        for (String perm : perms) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(context, perm) ==
                    PackageManager.PERMISSION_GRANTED);
            if (!hasPerm) {
                return false;
            }
        }

        return true;
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     *                       ActivityCompat.OnRequestPermissionsResultCallback} or override {@link
     *                       FragmentActivity#onRequestPermissionsResult(int, String[], int[])} if
     *                       it extends from {@link FragmentActivity}.
     *                       will be displayed if the user rejects the request the first time.
     * @param requestCode    request code to track this request, must be < 256.
     * @param perms          a set of permissions to be requested.
     * @see Manifest.permission
     */
    @SuppressLint("NewApi")
    public static void requestPermissions(@NonNull Object object,
                                          int requestCode, final PermissionCallbacks permissionCallbacks, @NonNull String... perms) {
        if (hasPermissions(getActivity(object), perms)) {
            notifyAlreadyHasPermissions(requestCode, perms, permissionCallbacks);
            return;
        }

        if (hasNeverAskAgainPermission(object, perms)) {
            notifyNeverAskAgainPermission(permissionCallbacks, requestCode, perms);
        } else {
            Activity activity = getActivity(object);
            Intent intent = RequestPermissionActivity.newIntent(getActivity(object), perms, requestCode);
            RequestPermissionActivity.setPermissionListener(new RequestPermissionActivity.PermissionListener() {
                @Override
                public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                    onRequestPermissionsResultNotify(requestCode, permissions, grantResults, permissionCallbacks);
                }
            });
            activity.startActivity(intent);
        }
    }


    /**
     * Handle the result of a permission request, should be called from the calling {@link
     * Activity}'s {@link ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int,
     * String[], int[])} method.
     * <p>
     * If any permissions were granted or denied, the {@code object} will receive the appropriate
     * callbacks through {@link PermissionCallbacks} and methods annotated with {@link
     *
     * @param requestCode  requestCode argument to permission result callback.
     * @param permissions  permissions argument to permission result callback.
     * @param grantResults grantResults argument to permission result callback.
     * @param receivers    an array of objects that have a method annotated with {@link
     */
    private static void onRequestPermissionsResultNotify(int requestCode,
                                                         @NonNull String[] permissions,
                                                         @NonNull int[] grantResults,
                                                         @NonNull Object... receivers) {
        // Make a collection of granted and denied permissions from the request.
        List<String> granted = new ArrayList<>();
        List<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];

            tagFirstRequest(getActivity(receivers[0]), perm);

            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        // iterate through all receivers
        for (Object object : receivers) {
            // Report granted permissions, if any.
            if (!granted.isEmpty()) {
                if (object instanceof PermissionCallbacks) {
                    ((PermissionCallbacks) object).onPermissionsGranted(requestCode, granted);
                }
            }

            // Report denied permissions, if any.
            if (!denied.isEmpty()) {
                if (object instanceof PermissionCallbacks) {
                    ((PermissionCallbacks) object).onPermissionsDenied(requestCode, denied);
                }
            }
        }
    }

    private static void onNeverAskAgainPermission(int requestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull PermissionCallbacks... receivers) {
        for (PermissionCallbacks object : receivers) {
            object.onNeverAskAgainPermission(requestCode, Arrays.asList(permissions));
        }
    }

    private static void notifyAlreadyHasPermissions(int requestCode,
                                                    @NonNull String[] permissions, @NonNull PermissionCallbacks... receivers) {
        for (PermissionCallbacks object : receivers) {
            object.onPermissionsGranted(requestCode, Arrays.asList(permissions));
        }
    }

    private static void notifyNeverAskAgainPermission(PermissionCallbacks permissionCallbacks,
                                                      int requestCode,
                                                      @NonNull String[] perms) {
        onNeverAskAgainPermission(requestCode, perms, permissionCallbacks);
    }

    /**
     * @param
     * @return true if the user has previously denied any of the {@code perms} and we should show a
     * rationale, false otherwise.
     */
    private static boolean hasNeverAskAgainPermission(@NonNull Object object, @NonNull String[] permission) {
        boolean isPermissionNotPromptedAgain = false;
        for (String perm : permission) {
            boolean isPermissionNotPromptedPerm = !shouldShowRequestPermissionRationale(object, perm)
                    && !isFirstRequest(getActivity(object), perm)
                    && !hasPermissions(getActivity(object), perm);
            isPermissionNotPromptedAgain = isPermissionNotPromptedAgain
                    || isPermissionNotPromptedPerm;
        }
        return isPermissionNotPromptedAgain;
    }

    private static boolean shouldShowRequestPermissionRationale(@NonNull Object object,
                                                                @NonNull String perm) {
        if (object instanceof Activity) {
            return ActivityCompat.shouldShowRequestPermissionRationale((Activity) object, perm);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else if (object instanceof android.app.Fragment) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return ((android.app.Fragment) object).shouldShowRequestPermissionRationale(perm);
            } else {
                throw new IllegalArgumentException(
                        "Target SDK needs to be greater than 23 if caller is android.app.Fragment");
            }
        } else {
            throw new IllegalArgumentException("Object was neither an Activity nor a Fragment.");
        }
    }


    private static Activity getActivity(Object object) {
        Activity activity;
        if (object instanceof Activity) {
            activity = (Activity) object;
        } else if (object instanceof Fragment) {
            activity = ((Fragment) object).getActivity();
        } else if (object instanceof android.app.Fragment) {
            activity = ((android.app.Fragment) object).getActivity();
        } else {
            throw new IllegalArgumentException("Object was neither Activity or Fragment.");
        }
        return activity;
    }

    private static boolean isFirstRequest(Context context, String perm) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(perm, true);
    }

    private static void tagFirstRequest(Context context, String perm) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putBoolean(perm, false)
                .apply();
    }
}
