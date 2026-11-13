package com.bestiecourt.app.presentation.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bestiecourt.app.data.remote.supabase.SupabaseManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@AndroidEntryPoint
class AdminPanelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val uid = SupabaseManager.currentUserId()
            if (uid == null) {
                Toast.makeText(this@AdminPanelActivity, "Access denied", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            // Check is_admin
            try {
                val profile = SupabaseManager.client.postgrest["profiles"]
                    .select { filter { eq("id", uid) } }
                    .decodeSingle<JsonObject>()
                val isAdmin = profile["is_admin"]?.jsonPrimitive?.content?.toBoolean() == true
                if (!isAdmin) {
                    Toast.makeText(this@AdminPanelActivity, "Access denied", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminPanelActivity, "Access denied", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        val openDevice = Button(this).apply {
            text = "Open Device Console (demo)"
            setOnClickListener {
                // In real UI: list devices from device_info, then open AdminDeviceActivity with device_id
                startActivity(Intent(this@AdminPanelActivity, AdminDeviceActivity::class.java)
                    .putExtra("device_id", "sc_demo"))
            }
        }
        layout.addView(openDevice)
        setContentView(layout)
    }
}
