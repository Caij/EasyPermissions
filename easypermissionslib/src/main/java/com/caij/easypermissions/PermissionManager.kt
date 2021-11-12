package com.caij.easypermissions

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import java.util.ArrayList

class PermissionManager(permissions: Permissions) : BasePermissionManager(permissions) {

    var isRequestedAfterToSettingOnAskNever: Boolean = false

    override fun request() {
        when (showReasonType) {
            Permissions.SHOW_REASON_TYPE_BEFORE, Permissions.SHOW_REASON_TYPE_NONE -> {
                isRequestedAfterToSettingOnAskNever = Permissions.askNever(fragmentActivity, permissions)
            }
        }
        super.request()
    }

    override fun toRequest() {
        toFragmentRequest(Permissions.REQUEST_PERMISSION_CODE)
    }

    override fun afterRequestReasonDialogOkClick() {
        if (Permissions.askNever(fragmentActivity, permissions)) {
            toSetting()
        } else {
            toRequest()
        }
    }

    private fun toSetting() {
        toFragmentRequest(Permissions.REQUEST_SETTING)
    }

    override fun requestPermissionType(permissionFragment: PermissionFragment, requestCode: Int) {
        if (requestCode == Permissions.REQUEST_PERMISSION_CODE) {
            permissionFragment.requestPermissions(permissions, requestCode)
        } else if (requestCode == Permissions.REQUEST_SETTING) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", fragmentActivity.packageName, null)
            intent.data = uri
            permissionFragment.startActivityForResult(intent, Permissions.REQUEST_SETTING)
        }
    }

    override fun onRequestPermissionsResult(
        permissions: Array<String>,
        grantResults: IntArray) {
        val granted: MutableList<String> = ArrayList(permissions.size)
        val denied: MutableList<String> = ArrayList(permissions.size)
        for (i in permissions.indices) {
            val perm = permissions[i]
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm)
            } else {
                denied.add(perm)
            }
        }
        val isAllGranted = granted.size == permissions.size
        if (isAllGranted) {
            finish(isAllGranted, granted, denied)
        } else {
            if (showReasonType == Permissions.SHOW_REASON_TYPE_AFTER) {
                showAfterRequestReasonDialog()
            } else if (showReasonType == Permissions.SHOW_REASON_TYPE_BEFORE) {
                if (isRequestedAfterToSettingOnAskNever && Permissions.askNever(fragmentActivity, permissions)) {
                    showSettingDialog()
                } else {
                    finish(isAllGranted, granted, denied)
                }
            } else if (showReasonType == Permissions.SHOW_REASON_TYPE_NONE) {
                if (isRequestedAfterToSettingOnAskNever && Permissions.askNever(fragmentActivity, permissions)) {
                    showSettingDialog()
                } else {
                    finish(isAllGranted, granted, denied)
                }
            } else {
                finish(isAllGranted, granted, denied)
            }
        }
    }

    override fun hasPermissions(): Boolean {
        return Permissions.hasPermissions(fragmentActivity, *permissions)
    }

    private fun showSettingDialog() {
        permissionDialog.showSetting(
            fragmentActivity,
            permissions,
            DialogInterface.OnClickListener { _, _ ->
                toSetting()
            },
            DialogInterface.OnClickListener { _, _ -> finish() })
    }

    override fun onSettingActivityResult(requestCode: Int) {
        finish()
    }

    override fun finish() {
        val granted: MutableList<String> = ArrayList(permissions.size)
        val denied: MutableList<String> = ArrayList(permissions.size)
        for (i in permissions.indices) {
            val perm = permissions[i]
            if (Permissions.hasPermissions(fragmentActivity, perm)) {
                granted.add(perm)
            } else {
                denied.add(perm)
            }
        }
        val isAllGranted = granted.size == permissions.size
        finish(isAllGranted, granted, denied)
    }
}