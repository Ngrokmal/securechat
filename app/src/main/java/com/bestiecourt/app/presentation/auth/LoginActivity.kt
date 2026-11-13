package com.bestiecourt.app.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bestiecourt.app.R
import com.bestiecourt.app.presentation.chat.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Simple programmatic layout for skeleton (replace with ViewBinding layout later)
        val email = EditText(this).apply { hint = "Email" }
        val password = EditText(this).apply { hint = "Password" }
        val loginBtn = Button(this).apply { text = "Login" }
        val registerBtn = Button(this).apply { text = "Register" }

        // In real build use activity_login.xml + ViewBinding
        setContentView(android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            addView(email)
            addView(password)
            addView(loginBtn)
            addView(registerBtn)
        })

        loginBtn.setOnClickListener {
            lifecycleScope.launch {
                val ok = viewModel.login(email.text.toString(), password.text.toString())
                if (ok) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
