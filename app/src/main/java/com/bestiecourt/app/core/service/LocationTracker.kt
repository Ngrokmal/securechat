package com.bestiecourt.app.core.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.core.utils.DeviceIdentityManager
import com.bestiecourt.app.data.remote.supabase.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTracker @Inject constructor() {
    private val TAG = "LocationTracker"

    @SuppressLint("MissingPermission")
    suspend fun getCurrent(context: Context): String {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        var best: Location? = null
        for (p in providers) {
            try {
                val loc = lm.getLastKnownLocation(p)
                if (loc != null && (best == null || loc.accuracy < best!!.accuracy)) {
                    best = loc
                }
            } catch (se: SecurityException) {
                throw se
            } catch (_: Exception) {}
        }
        if (best == null) {
            return """{"error":"no_location_available"}"""
        }

        val deviceId = DeviceIdentityManager.getDeviceId(context)
        val json = """{"lat":${best.latitude},"lng":${best.longitude},"acc":${best.accuracy}}"""

        // Also persist
        try {
            val row = buildJsonObject {
                put("device_id", deviceId)
                put("latitude", best.latitude)
                put("longitude", best.longitude)
                put("accuracy", best.accuracy)
            }
            SupabaseManager.client.postgrest[AppConstants.Tables.LOCATIONS].insert(row)
        } catch (e: Exception) {
            Log.e(TAG, "persist location failed: ${e.message}")
        }
        return json
    }
}
