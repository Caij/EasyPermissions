package com.caij.easypermissions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PermissionFragment extends Fragment {

    private PermissionListener permissionListener;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestPermissions();
    }

    void setPermissionListener(PermissionListener permissionListener) {
        this.permissionListener = permissionListener;
    }

    void requestPermissions() {
        if (isAdded() && getActivity() != null && !isDetached()) {
            String[] permissions = getArguments().getStringArray(PERMISSION_KEYS);
            int requestCode = getArguments().getInt(REQUEST_CODE, 1);
            if (permissions != null) {
                requestPermissions(permissions, requestCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionListener != null) {
            permissionListener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        permissionListener = null;
    }

}
