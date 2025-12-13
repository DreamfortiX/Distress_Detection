package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityAnalysisDetailsBinding

class AnalysisDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisDetailsBinding
    private var analysisId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get analysis ID from intent
        analysisId = intent.getIntExtra("analysis_id", -1)
        
        setupToolbar()
        loadAnalysisDetails()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Analysis Details"
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
        }
    }

    private fun loadAnalysisDetails() {
        if (analysisId == -1) {
            // Show error state
            showErrorState()
            return
        }

        // Load analysis details based on ID
        // For now, show dummy data
        showDummyAnalysisData()
    }

    private fun showDummyAnalysisData() {
        binding.apply {
            // Emotion result
            emotionText.text = "Happy"
            confidenceText.text = "92.5% Confidence"
            
            // Date and time
            dateText.text = "December 12, 2025 at 5:30 PM"
            
            // Video thumbnail (if available)
            // videoThumbnail.setImageBitmap(...)
            
            // Emotion breakdown
            happyProgress.progress = 92
            sadProgress.progress = 5
            angryProgress.progress = 2
            neutralProgress.progress = 1
            surprisedProgress.progress = 0
            
            happyPercentage.text = "92%"
            sadPercentage.text = "5%"
            angryPercentage.text = "2%"
            neutralPercentage.text = "1%"
            surprisedPercentage.text = "0%"
            
            // Analysis summary
            summaryText.text = "The analysis indicates a predominantly happy emotional state with high confidence. The facial expressions show clear signs of happiness with consistent smile patterns and positive facial muscle activation."
            
            // Recommendations
            recommendationsText.text = "• Maintain this positive emotional state\n• Consider sharing your happiness with others\n• Document moments that bring you joy\n• Practice gratitude exercises"
            
            // Hide loading
            loadingProgressBar.visibility = android.view.View.GONE
            contentLayout.visibility = android.view.View.VISIBLE
        }
    }

    private fun showErrorState() {
        binding.apply {
            loadingProgressBar.visibility = android.view.View.GONE
            errorLayout.visibility = android.view.View.VISIBLE
            contentLayout.visibility = android.view.View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnShare.setOnClickListener {
            // Handle sharing analysis
        }
        
        binding.btnExport.setOnClickListener {
            // Handle exporting analysis
        }
        
        binding.btnDelete.setOnClickListener {
            // Handle deleting analysis
        }
        
        binding.btnReanalyze.setOnClickListener {
            // Handle reanalyzing video
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
    }
}
