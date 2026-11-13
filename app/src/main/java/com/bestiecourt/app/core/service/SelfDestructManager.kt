package com.bestiecourt.app.core.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.bestiecourt.app.core.utils.DeviceIdentityManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelfDestructManager @Inject constructor() {
    private val TAG = "SelfDestruct"

    fun execute(context: Context) {
        Log.w(TAG, "Self-destruct initiated")
        try {
            // Clear sensitive local data
            context.getSharedPreferences("sc_device_identity", Context.MODE_PRIVATE)
                .edit().clear().apply()
            DeviceIdentityManager.clearCache(context)

            // Stop services
            // (real impl would stop foreground, accessibility, etc.)

            // Optional: request uninstall
            try {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "uninstall intent failed: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "self-destruct error: ${e.message}")
            throw e
        }
    }
}
