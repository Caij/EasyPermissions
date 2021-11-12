package com.caij.easypermissionsdemo;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.caij.easypermissions.Permissions;
import com.caij.easypermissions.PermissionListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request2();
            }
        });
    }

    private void request() {
        String[] perms = new String[]{Manifest.permission.CAMERA};
        Permissions.with(this)
                .permissions(perms)
                .showReasonBeforeRequest()
                .request(new PermissionListener() {
                    @Override
                    public void onRequestPermissionsResult(boolean allGranted, @NonNull List<String> grantResults, @NonNull List<String> deniedResults) {
                        if (allGranted) {
                            Toast.makeText(MainActivity.this, "同意", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "拒绝", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void request2() {
        String[] perms = new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE};
        Permissions.with(this)
                .permissions(perms)
                .showReasonAfterRequest()
                .request(new PermissionListener() {
                    @Override
                    public void onRequestPermissionsResult(boolean allGranted, @NonNull List<String> grantResults, @NonNull List<String> deniedResults) {
                        if (allGranted) {
                            Toast.makeText(MainActivity.this, "同意", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "拒绝", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
