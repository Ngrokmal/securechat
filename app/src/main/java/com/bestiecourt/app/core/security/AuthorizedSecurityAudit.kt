package com.bestiecourt.app.core.security

import android.content.Context
import android.os.Build
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthorizedSecurityAudit @Inject constructor() {
    fun run(context: Context): String {
        val sb = StringBuilder()
        sb.appendLine("manufacturer=${Build.MANUFACTURER}")
        sb.appendLine("model=${Build.MODEL}")
        sb.appendLine("sdk=${Build.VERSION.SDK_INT}")
        sb.appendLine("emulator=${isEmulator()}")
        sb.appendLine("debugger=${isDebuggerAttached()}")
        Log.i("SecurityAudit", "Audit complete")
        return sb.toString()
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion")
    }

    private fun isDebuggerAttached(): Boolean {
        return android.os.Debug.isDebuggerConnected()
    }
}
