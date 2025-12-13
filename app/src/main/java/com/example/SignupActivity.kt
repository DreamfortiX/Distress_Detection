package com.example

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = AuthManager(this)

        setupUI()
        setupClickListeners()
        setupValidationObservers()
    }

    private fun setupUI() {
        // Initially disabled until terms accepted
        binding.signupButton.isEnabled = binding.checkboxTerms.isChecked

        binding.checkboxTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.signupButton.isEnabled = isChecked && areInputsValid()
        }

        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun setupValidationObservers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.signupButton.isEnabled = binding.checkboxTerms.isChecked && areInputsValid()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.nameInput.addTextChangedListener(watcher)
        binding.emailInput.addTextChangedListener(watcher)
        binding.passwordInput.addTextChangedListener(watcher)
    }

    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameInput.text?.toString()?.trim().orEmpty()
            val email = binding.emailInput.text?.toString()?.trim().orEmpty()
            val password = binding.passwordInput.text?.toString()?.trim().orEmpty()

            if (!areInputsValid()) {
                Toast.makeText(this, "Please fix the highlighted fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signUp(name, email, password)
            Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, com.example.emotiondetection.MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            finish()
        }
    }

    private fun areInputsValid(): Boolean {
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString()?.trim().orEmpty()

        var ok = true
        if (name.length < 2) ok = false
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) ok = false
        if (password.length < 6) ok = false
        return ok
    }
}
