package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityStatisticsBinding

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        loadStatisticsData()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Statistics"
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun loadStatisticsData() {
        // Load statistics data from database or preferences
        // For now, show dummy data
        showDummyStatistics()
    }

    private fun showDummyStatistics() {
        binding.apply {
            // Overall stats
            totalAnalysesText.text = "47"
            avgConfidenceText.text = "87.3%"
            mostCommonEmotionText.text = "Happy"
            thisWeekAnalysesText.text = "12"
            
            // Emotion distribution
            happyCount.text = "23"
            sadCount.text = "8"
            angryCount.text = "4"
            neutralCount.text = "9"
            surprisedCount.text = "3"
            
            // Weekly chart (placeholder)
            // weeklyChart.data = ...
            
            // Hide loading
            loadingProgressBar.visibility = android.view.View.GONE
            contentLayout.visibility = android.view.View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.btnExportStats.setOnClickListener {
            // Handle exporting statistics
        }
        
        binding.btnResetStats.setOnClickListener {
            // Handle resetting statistics
        }
        
        binding.tabDaily.setOnClickListener {
            // Handle daily view
        }
        
        binding.tabWeekly.setOnClickListener {
            // Handle weekly view
        }
        
        binding.tabMonthly.setOnClickListener {
            // Handle monthly view
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
