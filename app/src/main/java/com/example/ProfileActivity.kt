package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        loadProfileData()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Profile"
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun loadProfileData() {
        // Load profile data from preferences or database
        // For now, show dummy data
        showDummyProfileData()
    }

    private fun showDummyProfileData() {
        binding.apply {
            // Profile info
            userNameText.text = "John Doe"
            userEmailText.text = "john.doe@example.com"
            memberSinceText.text = "Member since December 2024"
            
            // Stats
            analysesCount.text = "47"
            accuracyRate.text = "87.3%"
            streakDays.text = "5"
            
            // Subscription info
            subscriptionPlanText.text = "Free Plan"
            subscriptionStatusText.text = "Active"
            
            // Hide loading
            loadingProgressBar.visibility = android.view.View.GONE
            contentLayout.visibility = android.view.View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.editProfileButton.setOnClickListener {
            // Handle editing profile
        }
        
        binding.changePasswordButton.setOnClickListener {
            // Handle changing password
        }
        
        binding.subscriptionCard.setOnClickListener {
            // Navigate to subscription
        }
        
        binding.settingsCard.setOnClickListener {
            // Navigate to settings
        }
        
        binding.helpCard.setOnClickListener {
            // Navigate to help
        }
        
        binding.logoutButton.setOnClickListener {
            // Handle logout
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
