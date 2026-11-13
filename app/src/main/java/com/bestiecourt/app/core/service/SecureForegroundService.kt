package com.bestiecourt.app.core.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bestiecourt.app.R
import com.bestiecourt.app.core.constants.AppConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecureForegroundService : Service() {

    private val TAG = "SecureFgService"

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationCompat.Builder(this, AppConstants.CHANNEL_SERVICE)
            .setContentTitle("SecureChat")
            .setContentText("Running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        // foregroundServiceType = dataSync only (declared in manifest)
        startForeground(1001, notification)
        Log.i(TAG, "Foreground service started (dataSync)")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val i = Intent(context, SecureForegroundService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }
    }
}
