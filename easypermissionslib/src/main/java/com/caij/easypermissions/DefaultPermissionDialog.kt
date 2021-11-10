package com.caij.easypermissions

import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.os.Build
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity

class DefaultPermissionDialog : PermissionDialog {

    override fun showReason(
        fragmentActivity: FragmentActivity,
        permissions: Array<String>,
        okClickListener: DialogInterface.OnClickListener,
        cancelClickListener: DialogInterface.OnClickListener
    ) {
        val pm: PackageManager = fragmentActivity.packageManager
        val contentView = LayoutInflater.from(fragmentActivity)
            .inflate(R.layout.permission_default_dialog_layout, null)
        val linearLayout =
            contentView.findViewById<LinearLayout>(R.id.permissionsLayout)
        val tvMessage = contentView.findViewById<TextView>(R.id.messageText)
        val appLabel = pm.getApplicationLabel(fragmentActivity.applicationInfo)
        tvMessage.text = "$appLabel 需要下述权限才能继续下面的功能."
        val accentColor = getAccentColor(fragmentActivity)
        for (permission in permissions) {
            val view = LayoutInflater.from(fragmentActivity)
                .inflate(R.layout.permission_item, linearLayout, false)
            val imageView = view.findViewById<ImageView>(R.id.permissionIcon)
            val textView = view.findViewById<TextView>(R.id.permissionText)
            try {
                var permissionGroup: String?
                when {
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                        permissionGroup = permissionMapOnQ[permission]
                    }
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.R -> {
                        permissionGroup = permissionMapOnR[permission]
                    }
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.S -> {
                        permissionGroup = permissionMapOnS[permission]
                    }
                    Build.VERSION.SDK_INT > Build.VERSION_CODES.Q -> {
                        permissionGroup = permissionMapOnS[permission]
                    }
                    else -> {
                        val permissionInfo: PermissionInfo =
                            fragmentActivity.packageManager.getPermissionInfo(permission, 0)
                        permissionGroup = permissionInfo.group
                    }
                }
                val groupInfo = pm.getPermissionGroupInfo(permissionGroup!!, 0)
                textView.text = fragmentActivity.getText(groupInfo.labelRes)
                imageView.setImageResource(groupInfo.icon)
                if (accentColor != -1) {
                    imageView.imageTintList = ColorStateList.valueOf(accentColor)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            linearLayout.addView(view)
        }
        val dialog = AlertDialog.Builder(fragmentActivity)
            .setView(contentView)
            .setPositiveButton("允许", okClickListener)
            .setNegativeButton("拒绝", cancelClickListener)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun showSetting(
        fragmentActivity: FragmentActivity,
        permissions: Array<String>,
        okClickListener: DialogInterface.OnClickListener,
        cancelClickListener: DialogInterface.OnClickListener
    ) {
        val dialog = AlertDialog.Builder(fragmentActivity)
            .setMessage("你请求的权限已被拒绝并不再提示，请到设置中手动开启")
            .setPositiveButton("去设置", okClickListener)
            .setNegativeButton("拒绝", cancelClickListener)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun getAccentColor(activity: Activity): Int {
        val tmpArray = intArrayOf(R.attr.colorAccent)
        val a: TypedArray = activity.obtainStyledAttributes(null, tmpArray)
        try {
            return a.getColor(0, -1)
        } finally {
            a.recycle()
        }
        return -1
    }
}