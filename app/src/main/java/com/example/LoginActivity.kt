package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = AuthManager(this)

        // If already logged in, skip login
        if (auth.isLoggedIn()) {
            startActivity(Intent(this, com.example.emotiondetection.MainActivity::class.java))
            finish()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text?.toString()?.trim().orEmpty()
            val password = binding.passwordInput.text?.toString()?.trim().orEmpty()

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ok = auth.login(email, password)
            if (ok) {
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, com.example.emotiondetection.MainActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun isValidEmail(email: String): Boolean =
        email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
