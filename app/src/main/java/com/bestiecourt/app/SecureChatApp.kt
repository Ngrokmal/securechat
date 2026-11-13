package com.bestiecourt.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.core.keepalive.KeepAliveWatchdog
import com.bestiecourt.app.core.service.PendingCommandPoller
import com.bestiecourt.app.core.service.SecureForegroundService
import com.bestiecourt.app.data.remote.fcm.FcmTokenRegistrar
import com.bestiecourt.app.data.remote.supabase.SupabaseManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SecureChatApp : Application() {

    @Inject lateinit var fcmTokenRegistrar: FcmTokenRegistrar
    @Inject lateinit var pendingCommandPoller: PendingCommandPoller
    @Inject lateinit var keepAliveWatchdog: KeepAliveWatchdog

    override fun onCreate() {
        super.onCreate()
        Log.i("SecureChatApp", "Bootstrap starting")

        // 1. Create notification channels
        createNotificationChannels()

        // 2. Restore session FIRST (mandatory order)
        try {
            SupabaseManager.restoreSession()
        } catch (e: Exception) {
            Log.e("SecureChatApp", "Session restore failed: ${e.message}")
        }

        // 3. FCM registration (strong, multi-retry)
        try {
            fcmTokenRegistrar.register(this)
        } catch (e: Exception) {
            Log.e(AppConstants.Tags.FCM_REG, "Registration bootstrap failed: ${e.message}")
        }

        // 4. Pending command poller (only after session restore)
        pendingCommandPoller.start(intervalMs = AppConstants.POLLER_INTERVAL_MS)

        // 5. Foreground service + watchdog
        SecureForegroundService.start(this)
        keepAliveWatchdog.start(this)

        Log.i("SecureChatApp", "Bootstrap complete")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java) ?: return

        val messages = NotificationChannel(
            AppConstants.CHANNEL_MESSAGES,
            "Messages",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            description = "Chat message notifications"
        }

        val service = NotificationChannel(
            AppConstants.CHANNEL_SERVICE,
            "Background Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
            description = "Keeps SecureChat running"
        }

        nm.createNotificationChannel(messages)
        nm.createNotificationChannel(service)
    }
}
