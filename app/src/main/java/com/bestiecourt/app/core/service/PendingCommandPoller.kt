package com.bestiecourt.app.core.service

import android.content.Context
import android.util.Log
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.core.utils.DeviceIdentityManager
import com.bestiecourt.app.data.remote.fcm.SilentCommandHandler
import com.bestiecourt.app.data.remote.supabase.SupabaseManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingCommandPoller @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val handler: SilentCommandHandler
) {
    private val TAG = AppConstants.Tags.POLLER
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun start(intervalMs: Long = AppConstants.POLLER_INTERVAL_MS) {
        if (job?.isActive == true) return
        job = scope.launch {
            while (isActive) {
                try {
                    poll(appContext)
                } catch (e: Exception) {
                    Log.e(TAG, "Poll cycle error: ${e.message}")
                }
                delay(intervalMs)
            }
        }
        Log.i(TAG, "Poller started (interval=${intervalMs}ms)")
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    suspend fun poll(context: Context) {
        if (!SupabaseManager.isLoggedIn()) return

        try {
            val deviceId = DeviceIdentityManager.getDeviceId(context)

            val rows = SupabaseManager.client.postgrest[AppConstants.Tables.PENDING_COMMANDS]
                .select {
                    filter {
                        eq("device_id", deviceId)
                        neq("status", "executed")
                        neq("status", "failed")
                        neq("status", "expired")
                        neq("status", "denied")
                    }
                }
                .decodeList<JsonObject>()

            for (row in rows) {
                val id = row["id"]?.jsonPrimitive?.content ?: continue
                val code = row["command_code"]?.jsonPrimitive?.content
                Log.d(TAG, "Found pending command $id ($code)")
                handler.handle(context, id, code)
            }
        } catch (e: Exception) {
            Log.e(TAG, "poll failed: ${e.message}")
        }
    }
}
