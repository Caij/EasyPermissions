package com.caij.easypermissions

import android.content.DialogInterface
import androidx.fragment.app.FragmentActivity

interface PermissionDialog {
    fun showReason(
        fragmentActivity: FragmentActivity,
        permissions: Array<String>,
        okClickListener: DialogInterface.OnClickListener,
        cancelClickListener: DialogInterface.OnClickListener)

    fun showSetting(
        fragmentActivity: FragmentActivity,
        permissions: Array<String>,
        okClickListener: DialogInterface.OnClickListener,
        cancelClickListener: DialogInterface.OnClickListener)
}