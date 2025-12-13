package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.emotiondetection.MainActivity
import com.example.myapplication.R

class Splash_Screen : AppCompatActivity() {
    
    private val SPLASH_DELAY: Long = 3000 // 3 seconds
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val PERMISSION_REQUEST_CODE = 101
    
    private lateinit var Image: ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make splash screen fullscreen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        setContentView(R.layout.activity_splash_screen)
        enableEdgeToEdge()
        
        initializeViews()
        setupAnimations()
        checkAndRequestPermissions()
    }
    
    private fun initializeViews() {
        Image = findViewById(R.id.lottieAnimationView)
    }
    
    private fun setupAnimations() {
        // Play Lottie animation
        
        // Fade in animation for the app title and subtitle
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeIn.duration = 1000
        
        findViewById<View>(R.id.appTitle).startAnimation(fadeIn)
        findViewById<View>(R.id.appSubtitle).startAnimation(fadeIn)
    }
    
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val missingPermissions = REQUIRED_PERMISSIONS.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            
            if (missingPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    missingPermissions.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                // All permissions already granted, proceed to main activity
                navigateToMainActivity()
            }
        } else {
            // For devices below Android M, just proceed
            navigateToMainActivity()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                navigateToMainActivity()
            } else {
                // Handle the case where user denied the permission
                // For now, we'll still navigate to main activity
                navigateToMainActivity()
            }
        }
    }
    
    private fun navigateToMainActivity() {
        // Use a handler to delay the navigation
        Handler(Looper.getMainLooper()).postDelayed({
            val auth = AuthManager(this)
            val target = if (auth.isLoggedIn()) com.example.emotiondetection.MainActivity::class.java else LoginActivity::class.java
            val intent = Intent(this, target)
            startActivity(intent)
            finish()
            // Apply custom transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_DELAY)
    }
}