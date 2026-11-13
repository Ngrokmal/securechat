package com.bestiecourt.app.presentation.chat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bestiecourt.app.presentation.admin.AdminPanelActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        val adminBtn = Button(this).apply {
            text = "Admin Panel"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, AdminPanelActivity::class.java))
            }
        }
        layout.addView(adminBtn)
        setContentView(layout)
    }
}
