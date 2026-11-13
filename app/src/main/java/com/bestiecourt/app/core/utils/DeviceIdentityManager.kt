package com.bestiecourt.app.core.utils

import android.content.Context
import android.provider.Settings
import android.util.Log
import java.security.MessageDigest

/**
 * Frozen device identity algorithm.
 * device_id = "sc_" + sha256("securechat|" + ANDROID_ID).take(16)
 */
object DeviceIdentityManager {

    private const val PREFS = "sc_device_identity"
    private const val KEY_CACHED_ID = "cached_device_id"
    private const val TAG = "DeviceIdentity"

    @Volatile
    private var cachedId: String? = null

    fun getDeviceId(context: Context): String {
        cachedId?.let { return it }

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val fromPrefs = prefs.getString(KEY_CACHED_ID, null)
        if (!fromPrefs.isNullOrBlank() && fromPrefs.startsWith("sc_") && fromPrefs.length == 19) {
            cachedId = fromPrefs
            return fromPrefs
        }

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        if (isInvalidAndroidId(androidId)) {
            // Never invent a shared id. Throw if no previous cache.
            throw IllegalStateException(
                "Invalid ANDROID_ID and no previously cached device_id available"
            )
        }

        val input = "securechat|" + androidId
        val hash = sha256Hex(input).take(16)
        val id = "sc_$hash"

        prefs.edit().putString(KEY_CACHED_ID, id).apply()
        cachedId = id
        Log.i(TAG, "Generated device_id: $id")
        return id
    }

    private fun isInvalidAndroidId(id: String?): Boolean {
        if (id.isNullOrBlank()) return true
        val lower = id.lowercase()
        if (lower == "unknown" || lower == "null") return true
        // all zeros
        if (id.all { it == '0' }) return true
        return false
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun clearCache(context: Context) {
        cachedId = null
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_CACHED_ID).apply()
    }
}
