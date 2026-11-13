package com.bestiecourt.app.core.keepalive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bestiecourt.app.core.service.SecureForegroundService

class WatchdogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.i("WatchdogReceiver", "triggered – restarting FG service")
        SecureForegroundService.start(context)
        KeepAliveWatchdog().start(context) // reschedule
    }
}
