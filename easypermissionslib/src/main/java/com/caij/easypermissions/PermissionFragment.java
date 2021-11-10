package com.caij.easypermissions;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PermissionFragment extends Fragment {

    private PermissionManager permissionManager;

    private static final String PERMISSION_KEYS = "KEY_INPUT_PERMISSIONS";
    private static final String REQUEST_CODE = "request_code";
    private int requestCode;

    static PermissionFragment newInstance(String[] permissions, int requestCode) {
        PermissionFragment fragment = new PermissionFragment();
        fragment.setArguments(newArgs(permissions, requestCode));
        return fragment;
    }

    static Bundle newArgs(String[] permissions, int requestCode) {
        Bundle args = new Bundle();
        args.putStringArray(PERMISSION_KEYS, permissions);
        args.putInt(REQUEST_CODE, requestCode);
        return args;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (permissionManager != null) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                int requestCode = bundle.getInt(REQUEST_CODE, -1);
                if (requestCode == Permissions.REQUEST_PERMISSION_CODE) {
                    requestPermissions();
                }
                if (requestCode == Permissions.REQUEST_SETTING) {
                    forwardToSettings();
                }
            }
        } else {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
            }
        }
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    void requestPermissions() {
        if (isAdded() && getActivity() != null && !isDetached()) {
            String[] permissions = getArguments().getStringArray(PERMISSION_KEYS);
            requestCode = getArguments().getInt(REQUEST_CODE, 1);
            if (permissions != null) {
                requestPermissions(permissions, requestCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager != null) {
            if (requestCode == this.requestCode) {
                permissionManager.onRequestPermissionsResult(permissions, grantResults);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Permissions.REQUEST_SETTING) {
            permissionManager.onSettingUpdate();
        }
    }

    void forwardToSettings() {
        if (isAdded() && getActivity() != null && !isDetached()) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, Permissions.REQUEST_SETTING);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        permissionManager = null;
    }

}
