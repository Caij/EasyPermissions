package com.caij.easypermissions

import android.content.DialogInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

abstract class BasePermissionManager(permissions: Permissions) {

    companion object {
        const val FRAGMENT_TAG = "permission_fragment_tag"
    }

    private val permissionListener: PermissionListener = permissions.permissionListener
    private var fragment: Fragment? = null

    protected val permissions: Array<String> = permissions.permissions
    protected val showReasonType = permissions.showReasonType
    protected val fragmentActivity: FragmentActivity = permissions.fragmentActivity
    protected val permissionDialog: PermissionDialog = permissions.permissionDialog

    private fun showBeforeRequestReasonDialog() {
        permissionDialog.showReason(
            fragmentActivity,
            permissions,
            DialogInterface.OnClickListener { _, _ ->
                toRequest()
            },
            DialogInterface.OnClickListener { _, _ -> finish() })
    }

    open fun request() {
        if (hasPermissions()) {
            finish()
        } else{
            when (showReasonType) {
                Permissions.SHOW_REASON_TYPE_BEFORE -> {
                    showBeforeRequestReasonDialog()
                }
                Permissions.SHOW_REASON_TYPE_NONE -> {
                    toRequest()
                }
                else -> {
                    toRequest()
                }
            }
        }
    }

    fun toFragmentRequest(requestCode : Int) {
        val fragmentManager = fragmentActivity.supportFragmentManager
        fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (fragment is PermissionFragment) {
            val permissionFragment = (fragment as PermissionFragment);
            permissionFragment.arguments = PermissionFragment.newArgs(permissions, requestCode)
            permissionFragment.setPermissionManager(this)
            requestPermissionType(permissionFragment, requestCode)
        } else {
            val permissionFragment = PermissionFragment.newInstance(permissions, requestCode)
            permissionFragment.setPermissionManager(this)
            fragmentManager.beginTransaction().add(permissionFragment, FRAGMENT_TAG)
                .commitAllowingStateLoss()
        }
    }

    protected fun showAfterRequestReasonDialog() {
        permissionDialog.showReason(
            fragmentActivity,
            permissions,
            DialogInterface.OnClickListener { _, _ ->
               afterRequestReasonDialogOkClick()
            },
            DialogInterface.OnClickListener { _, _ -> finish() })
    }

    protected fun finish(
        isAllGranted: Boolean,
        granted: List<String>,
        denied: List<String>
    ) {
        permissionListener.onRequestPermissionsResult(isAllGranted, granted, denied)
        if (fragment != null) {
            fragmentActivity.supportFragmentManager.beginTransaction().remove(fragment!!)
                .commitAllowingStateLoss()
        }
    }

    abstract fun onSettingActivityResult(requestCode: Int)
    abstract fun requestPermissionType(permissionFragment: PermissionFragment, requestCode: Int)
    abstract fun finish()
    abstract fun toRequest()
    abstract fun afterRequestReasonDialogOkClick()
    abstract fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray)
    abstract fun hasPermissions(): Boolean

}