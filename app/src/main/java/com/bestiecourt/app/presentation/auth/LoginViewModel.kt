package com.bestiecourt.app.presentation.auth

import androidx.lifecycle.ViewModel
import com.bestiecourt.app.data.remote.supabase.SupabaseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    suspend fun login(email: String, password: String): Boolean {
        return try {
            SupabaseManager.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun register(email: String, password: String, username: String): Boolean {
        return try {
            SupabaseManager.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
