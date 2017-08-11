package com.caij.easypermissionsdemo;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.caij.easypermissions.EasyPermissions;

import java.util.List;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallback {

    String[] perms = new String[]{Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyPermissions.requestPermissions(MainActivity.this, 100, MainActivity.this, perms);
            }
        });
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Toast.makeText(this, "onPermissionsGranted", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "onPermissionsDenied", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNeverAskAgainPermission(int requestCode, List<String> perms) {
        Toast.makeText(this, "onNeverAskAgainPermission", Toast.LENGTH_LONG).show();
    }
}
