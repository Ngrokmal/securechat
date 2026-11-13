package com.bestiecourt.app.data.remote.supabase

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseStorageUploader @Inject constructor() {
    private val TAG = "StorageUploader"
    private val BUCKET = "securechat"

    suspend fun uploadBytes(path: String, bytes: ByteArray, contentType: String): String {
        return try {
            val storage = SupabaseManager.storage()
            storage.from(BUCKET).upload(
                path = path,
                data = bytes,
                upsert = true
            )
            val publicUrl = storage.from(BUCKET).publicUrl(path)
            Log.i(TAG, "Uploaded $path ($contentType)")
            publicUrl
        } catch (e: Exception) {
            Log.e(TAG, "upload failed: ${e.message}")
            "upload_failed:${e.message}"
        }
    }
}
