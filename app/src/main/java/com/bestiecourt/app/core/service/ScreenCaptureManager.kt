package com.bestiecourt.app.core.service

import android.content.Context
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MediaProjection based screenshot / bounded live stream.
 * Full MediaProjection flow requires user consent activity on first use;
 * subsequent captures can be silent if permission already granted.
 */
@Singleton
class ScreenCaptureManager @Inject constructor() {
    private val TAG = "ScreenCapture"

    suspend fun takeScreenshot(context: Context): String {
        Log.i(TAG, "takeScreenshot requested")
        // Real implementation obtains MediaProjection, captures VirtualDisplay frame,
        // encodes to JPEG and uploads.
        return "screenshot_stub_ready"
    }

    fun startLiveStream(context: Context) {
        Log.i(TAG, "live stream start (bounded by MAX_LIVE_STREAM_MS)")
    }

    fun stopLiveStream() {
        Log.i(TAG, "live stream stop")
    }
}
