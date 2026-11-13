package com.bestiecourt.app.core.keepalive

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeepAliveWatchdog @Inject constructor() {
    private val TAG = "KeepAliveWatchdog"
    private val INTERVAL = 15 * 60 * 1000L // 15 min

    fun start(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WatchdogReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + INTERVAL,
            pi
        )
        Log.i(TAG, "Watchdog scheduled")
    }
}
