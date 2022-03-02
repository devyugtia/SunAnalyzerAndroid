package com.sunanalyzer.app.utility

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.material.R
import com.google.android.material.snackbar.Snackbar
import java.util.*

object RuntimeAppPermission {

    fun getPermissions(ctx: Activity?, permissions: ArrayList<String>) {
        if (isRuntimePermissionRequired()) {
            val permissionsList: ArrayList<String?> = ArrayList()
            for (i in permissions.indices) {
                if (!checkGrantedPermission(ctx!!, permissions[i])) {
                    if (addPermission(permissionsList, permissions[i], ctx)) {
                    }
                }
            }
            if (permissionsList.size > 0) {
                ActivityCompat.requestPermissions(ctx!!, permissionsList.toTypedArray(), 999)
            }
        }
    }

    private fun isRuntimePermissionRequired(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    fun checkGrantedPermission(mActivity: Activity, permissionName: String): Boolean {
        return ActivityCompat.checkSelfPermission(mActivity, permissionName) === PackageManager.PERMISSION_GRANTED
    }

    private fun addPermission(permissionsList: ArrayList<String?>, permission: String?, ctx: Activity?): Boolean {
        if (ActivityCompat.checkSelfPermission(ctx!!, permission!!) !== PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission)
            return ActivityCompat.shouldShowRequestPermissionRationale(ctx!!, permission!!)
        }
        return true
    }

    private fun isCheckPermissionAllowOrNot(activity: Activity?, permissionsList: ArrayList<String>): Boolean {
        for (i in permissionsList.indices) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permissionsList[i]!!)) {
                return true
            }
        }
        return false
    }

    fun getRuntimePermissionSnackBar(view: View, permissions: Int, permissionsList: ArrayList<String>?, activity: Activity) {
        var message = ""
        message = if (permissions > 1) {
            "$permissions permissions were rejected"
        } else {
            "$permissions permission was rejected"
        }
        val snackbar: Snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("Allow to Ask Again") { view ->
                    if (isCheckPermissionAllowOrNot(activity, permissionsList!!)) {
                        getPermissions(activity, permissionsList!!)
                    } else {
                        val snackbar: Snackbar = Snackbar
                            .make(
                                view,
                                "Go to settings and enable permissions",
                                Snackbar.LENGTH_INDEFINITE
                            )
                            .setAction("OKAY", View.OnClickListener {
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts("package", activity.packageName, null)
                                intent.data = uri
                                activity.startActivity(intent)
                            })
                        snackbar.setActionTextColor(Color.BLACK)
                        val snackView: View = snackbar.view
                        snackView.setBackgroundColor(Color.WHITE)
                        val textView = snackView.findViewById<TextView>(R.id.snackbar_text)
                        textView.setTextColor(Color.BLACK)
                        snackbar.show()
                    }
                }
        snackbar.setActionTextColor(Color.BLACK)
        val snackView: View = snackbar.view
        snackView.setBackgroundColor(Color.WHITE)
        val textView = snackView.findViewById<TextView>(R.id.snackbar_text)
        textView.setTextColor(Color.BLACK)
        snackbar.show()
    }
}