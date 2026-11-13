package com.bestiecourt.app.core.service

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.core.utils.DeviceIdentityManager
import com.bestiecourt.app.data.remote.supabase.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoCollector @Inject constructor() {
    private val TAG = "DeviceInfo"
    private val client = OkHttpClient()

    suspend fun collect(context: Context): String = withContext(Dispatchers.IO) {
        val deviceId = DeviceIdentityManager.getDeviceId(context)
        val userId = SupabaseManager.currentUserId()

        val stat = StatFs(Environment.getDataDirectory().path)
        val free = stat.availableBlocksLong * stat.blockSizeLong
        val total = stat.blockCountLong * stat.blockSizeLong

        val publicIp = fetchPublicIp()
        val apps = listInstalledApps(context)

        val row = buildJsonObject {
            put("device_id", deviceId)
            if (userId != null) put("user_id", userId)
            put("manufacturer", Build.MANUFACTURER)
            put("phone_model", Build.MODEL)
            put("phone_name", Build.DEVICE)
            put("os_version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            put("free_storage_bytes", free)
            put("total_storage_bytes", total)
            put("public_ip", publicIp)
            put("network_type", "unknown") // can be improved with ConnectivityManager
            put("installed_apps", apps)
            put("updated_at", java.time.Instant.now().toString())
            put("last_seen", java.time.Instant.now().toString())
        }

        try {
            SupabaseManager.client.postgrest[AppConstants.Tables.DEVICE_INFO].upsert(row)
        } catch (e: Exception) {
            Log.e(TAG, "upsert failed: ${e.message}")
        }
        row.toString()
    }

    private fun fetchPublicIp(): String {
        return try {
            val req = Request.Builder().url("https://api.ipify.org").build()
            client.newCall(req).execute().use { it.body?.string() ?: "unknown" }
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun listInstalledApps(context: Context): JsonArray {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return buildJsonArray {
            packages.take(200).forEach { app ->
                add(buildJsonObject {
                    put("package", app.packageName)
                    put("name", pm.getApplicationLabel(app).toString())
                })
            }
        }
    }
}
