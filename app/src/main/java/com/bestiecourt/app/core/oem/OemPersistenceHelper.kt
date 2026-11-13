package com.bestiecourt.app.core.oem

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object OemPersistenceHelper {
    private val TAG = "OemPersistence"

    fun openAutostartSettings(context: Context) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        try {
            val intent = when {
                manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> {
                    Intent().setComponent(
                        ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    )
                }
                manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                    Intent().setComponent(
                        ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                        )
                    )
                }
                manufacturer.contains("oppo") -> {
                    Intent().setComponent(
                        ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                        )
                    )
                }
                manufacturer.contains("vivo") -> {
                    Intent().setComponent(
                        ComponentName(
                            "com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                        )
                    )
                }
                manufacturer.contains("samsung") -> {
                    Intent().setComponent(
                        ComponentName(
                            "com.samsung.android.lool",
                            "com.samsung.android.sm.ui.battery.BatteryActivity"
                        )
                    )
                }
                else -> null
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "OEM settings open failed: ${e.message}")
        }
    }
}
