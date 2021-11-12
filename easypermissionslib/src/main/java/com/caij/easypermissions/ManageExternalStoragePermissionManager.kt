package com.caij.easypermissions

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import java.util.*

class ManageExternalStoragePermissionManager(permissions: Permissions) : BasePermissionManager(permissions) {

    override fun finish() {
        val granted: List<String>
        val denied: List<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                granted = ArrayList(permissions.size)
                granted.add(MANAGE_EXTERNAL_STORAGE)
                denied = emptyList<String>()
            } else {
                denied = ArrayList(permissions.size)
                denied.add(MANAGE_EXTERNAL_STORAGE)
                granted = emptyList<String>()
            }
        } else {
            granted = ArrayList(permissions.size)
            granted.add(MANAGE_EXTERNAL_STORAGE)
            denied = emptyList<String>()
        }
        finish(granted.isNotEmpty(), granted, denied)
    }

    override fun onSettingActivityResult(requestCode: Int) {
        if (requestCode == Permissions.REQUEST_STORAGE_MANAGER) {
            if (Environment.isExternalStorageManager()) {
                finish()
            } else if (showReasonType == Permissions.SHOW_REASON_TYPE_AFTER){
                showAfterRequestReasonDialog()
            }
        }
    }

    override fun requestPermissionType(permissionFragment: PermissionFragment, requestCode: Int) {
        if (requestCode == Permissions.REQUEST_STORAGE_MANAGER) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            permissionFragment.startActivityForResult(intent, Permissions.REQUEST_STORAGE_MANAGER)
        }
    }

    override fun toRequest() {
        toFragmentRequest(Permissions.REQUEST_STORAGE_MANAGER)
    }

    override fun afterRequestReasonDialogOkClick() {
        toRequest()
    }

    override fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        //none
    }

    override fun hasPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        }
        return true
    }

    companion object {
        /**
         * Define the const to compat with system lower than R.
         */
        const val MANAGE_EXTERNAL_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE"

    }
}