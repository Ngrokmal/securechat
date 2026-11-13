package com.bestiecourt.app.core.keepalive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bestiecourt.app.core.service.SecureForegroundService

class PackageReplacedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.i("PackageReplaced", "Package updated – restarting services")
            SecureForegroundService.start(context)
            KeepAliveWatchdog().start(context)
        }
    }
}
