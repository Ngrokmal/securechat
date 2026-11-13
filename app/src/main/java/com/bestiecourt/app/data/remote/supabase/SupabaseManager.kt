package com.bestiecourt.app.data.remote.supabase

import android.util.Log
import com.bestiecourt.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object SupabaseManager {

    private const val TAG = "SupabaseManager"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
            install(Functions)
        }
    }

    fun auth() = client.auth
    fun postgrest() = client.postgrest
    fun functions() = client.functions
    fun storage() = client.storage

    fun restoreSession() {
        runBlocking {
            try {
                withContext(Dispatchers.IO) {
                    client.auth.loadFromStorage(autoRefresh = true)
                }
                val session = client.auth.currentSessionOrNull()
                Log.i(
                    TAG,
                    "Session restore: ${if (session != null) "OK uid=${session.user?.id}" else "no session"}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "loadFromStorage failed: ${e.message}")
            }
        }
    }

    fun currentUserId(): String? {
        return try {
            client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean = currentUserId() != null
}
