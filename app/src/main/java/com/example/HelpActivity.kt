package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Help & Support"
        }
        
        toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun setupClickListeners() {
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.faqSection).setOnClickListener {
            // Handle FAQ section click
        }
        
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.contactSupport).setOnClickListener {
            // Handle contact support click
        }
        
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.userGuide).setOnClickListener {
            // Handle user guide click
        }
        
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.privacyPolicy).setOnClickListener {
            // Handle privacy policy click
        }
        
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.termsOfService).setOnClickListener {
            // Handle terms of service click
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
