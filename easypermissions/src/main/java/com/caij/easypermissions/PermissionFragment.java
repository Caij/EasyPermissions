package com.caij.easypermissions;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PermissionFragment extends Fragment {

    private BasePermissionManager permissionManager;

    private static final String PERMISSION_KEYS = "KEY_INPUT_PERMISSIONS";
    private static final String REQUEST_CODE = "request_code";

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
                permissionManager.requestPermissionType(this, requestCode);
            }
        } else {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
            }
        }
    }

    public void setPermissionManager(BasePermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager != null) {
            permissionManager.onRequestPermissionsResult(permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (permissionManager != null) {
            permissionManager.onSettingActivityResult(requestCode);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        permissionManager = null;
    }

}
