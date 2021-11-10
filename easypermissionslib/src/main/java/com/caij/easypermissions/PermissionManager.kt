package com.caij.easypermissions

import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.*

class PermissionManager(permissions: Permissions) {

    private val FRAGMENT_TAG = "permission_fragment_tag"

    private val permissions: Array<String> = permissions.permissions
    private val showReasonType = permissions.showReasonType
    private val permissionListener: PermissionListener = permissions.permissionListener
    private val fragmentActivity: FragmentActivity = permissions.fragmentActivity
    private val permissionDialog: PermissionDialog = permissions.permissionDialog

    var isRequestAfterToSetting: Boolean = false

    private var fragment: Fragment? = null

    private fun requestPermission() {
        when (showReasonType) {
            Permissions.SHOW_REASON_TYPE_BEFORE -> {
                isRequestAfterToSetting = Permissions.askNever(fragmentActivity, permissions)
                showReasonDialogPre()
            }
            Permissions.SHOW_REASON_TYPE_NONE -> {
                isRequestAfterToSetting = Permissions.askNever(fragmentActivity, permissions)
                toRequest()
            }
            else -> {
                toRequest()
            }
        }
    }

    private fun showReasonDialogPre() {
        permissionDialog.showReason(
            fragmentActivity,
            permissions,
            DialogInterface.OnClickListener { _, _ ->
                toRequest()
            },
            DialogInterface.OnClickListener { _, _ -> finish() })
    }

    private fun showReasonDialogPermissionRationale() {
        permissionDialog.showReason(
            fragmentActivity,
            permissions,
            DialogInterface.OnClickListener { _, _ -> toSetting() },
            DialogInterface.OnClickListener { _, _ -> finish() })
    }

    private fun toSetting() {
        val fragmentManager =
            fragmentActivity.supportFragmentManager
        fragment =
            fragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (fragment is PermissionFragment) {
            (fragment as PermissionFragment).arguments = PermissionFragment.newArgs(permissions, Permissions.REQUEST_SETTING)
            (fragment as PermissionFragment).setPermissionManager(this)
            (fragment as PermissionFragment).forwardToSettings()
        } else {
            val permissionFragment =
                PermissionFragment.newInstance(permissions, Permissions.REQUEST_SETTING)
            permissionFragment.setPermissionManager(this)
            fragmentManager.beginTransaction().add(permissionFragment, FRAGMENT_TAG)
                .commitAllowingStateLoss()
        }
    }

    fun request() {
        if (Permissions.hasPermissions(fragmentActivity, *permissions)) {
            permissionListener.onRequestPermissionsResult(
                true,
                emptyList(),
                listOf(*permissions)
            )
        } else {
            requestPermission()
        }
    }

    private fun toRequest() {
        val fragmentManager =
            fragmentActivity.supportFragmentManager

        fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (fragment is PermissionFragment) {
            (fragment as PermissionFragment).arguments = PermissionFragment.newArgs(permissions, Permissions.REQUEST_PERMISSION_CODE)
            (fragment as PermissionFragment).setPermissionManager(this)
            (fragment as PermissionFragment).requestPermissions()
        } else {
            val permissionFragment =
                PermissionFragment.newInstance(permissions, Permissions.REQUEST_PERMISSION_CODE)
            permissionFragment.setPermissionManager(this)
            fragmentManager.beginTransaction().add(permissionFragment, FRAGMENT_TAG)
                .commitAllowingStateLoss()
        }
    }

    fun onRequestPermissionsResult(
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
                showReasonDialogAfter()
            } else if (showReasonType == Permissions.SHOW_REASON_TYPE_BEFORE) {
                if (isRequestAfterToSetting && Permissions.askNever(fragmentActivity, permissions)) {
                    showSettingDialog()
                } else {
                    finish(isAllGranted, granted, denied)
                }
            } else if (showReasonType == Permissions.SHOW_REASON_TYPE_NONE) {
                if (isRequestAfterToSetting && Permissions.askNever(fragmentActivity, permissions)) {
                    showSettingDialog()
                } else {
                    finish(isAllGranted, granted, denied)
                }
            } else {
                finish(isAllGranted, granted, denied)
            }
        }
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

    private fun showReasonDialogAfter() {
        permissionDialog.showReason(
            fragmentActivity,
            permissions,
            DialogInterface.OnClickListener { _, _ ->
                if (Permissions.askNever(fragmentActivity, permissions)) {
                    toSetting()
                } else {
                    toRequest()
                }
            },
            DialogInterface.OnClickListener { _, _ -> finish() })
    }

    private fun finish(
        isAllGranted: Boolean,
        granted: MutableList<String>,
        denied: MutableList<String>
    ) {
        permissionListener.onRequestPermissionsResult(isAllGranted, granted, denied)
        if (fragment != null) {
            fragmentActivity.supportFragmentManager.beginTransaction().remove(fragment!!)
                .commitAllowingStateLoss()
        }
    }

    private fun finish() {
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

    fun onSettingUpdate() {
        finish()
    }

}