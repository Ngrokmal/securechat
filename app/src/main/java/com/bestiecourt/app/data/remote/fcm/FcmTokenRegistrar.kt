package com.bestiecourt.app.data.remote.fcm

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.core.utils.DeviceIdentityManager
import com.bestiecourt.app.data.remote.supabase.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenRegistrar @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = AppConstants.Tags.FCM_REG

    fun register(context: Context) {
        scope.launch {
            registerWithRetries(context)
        }
    }

    /**
     * Minimum 5 attempts with exponential backoff: 2s, 4s, 8s, 16s, 32s
     * Never writes blank/null token to device_info.
     */
    private suspend fun registerWithRetries(context: Context) {
        val delays = listOf(2000L, 4000L, 8000L, 16000L, 32000L)
        var token: String? = null

        for ((index, delayMs) in delays.withIndex()) {
            try {
                token = FirebaseMessaging.getInstance().token.await()
                if (!token.isNullOrBlank()) {
                    Log.i(TAG, "FCM token obtained on attempt ${index + 1}")
                    break
                }
            } catch (e: Exception) {
                Log.w(TAG, "Attempt ${index + 1} failed: ${e.message}")
            }
            if (index < delays.lastIndex) {
                delay(delayMs)
            }
        }

        if (token.isNullOrBlank()) {
            Log.e(TAG, "FCM_REG_FAILED after all retries – not writing to device_info")
            return
        }

        writeToken(context, token)
    }

    fun onNewToken(context: Context, newToken: String?) {
        if (newToken.isNullOrBlank()) {
            Log.w(TAG, "onNewToken received blank – ignored")
            return
        }
        scope.launch {
            writeToken(context, newToken)
        }
    }

    private suspend fun writeToken(context: Context, token: String) {
        try {
            val deviceId = DeviceIdentityManager.getDeviceId(context)
            val userId = SupabaseManager.currentUserId()

            val row = buildJsonObject {
                put("device_id", deviceId)
                put("fcm_token", token)
                if (userId != null) put("user_id", userId)
                put("updated_at", java.time.Instant.now().toString())
            }

            SupabaseManager.client.postgrest[AppConstants.Tables.DEVICE_INFO]
                .upsert(row)

            Log.i(TAG, "device_info upserted for $deviceId (token length=${token.length})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write FCM token: ${e.message}")
            // Never crash the app
        }
    }
}
