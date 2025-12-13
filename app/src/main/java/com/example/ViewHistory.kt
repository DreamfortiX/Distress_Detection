package com.example

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.database.AnalysisDatabase
import com.example.database.AnalysisEntity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityViewHistoryBinding
import kotlinx.coroutines.launch

class ViewHistory : AppCompatActivity() {

    private lateinit var binding: ActivityViewHistoryBinding
    private lateinit var database: AnalysisDatabase
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database
        database = AnalysisDatabase.getDatabase(this)

        // Setup RecyclerView
        setupRecyclerView()

        // Load analyses
        loadAnalyses()

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter { analysis ->
            // Handle item click - navigate to results with saved analysis
            navigateToResults(analysis)
        }

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ViewHistory)
            adapter = this@ViewHistory.adapter
        }
    }

    private fun loadAnalyses() {
        database.analysisDao().getAllAnalyses().observe(this, Observer { analyses ->
            if (analyses.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.historyRecyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.historyRecyclerView.visibility = View.VISIBLE
                adapter.submitList(analyses)
            }
        })
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.clearAllButton.setOnClickListener {
            clearAllAnalyses()
        }
    }

    private fun navigateToResults(analysis: AnalysisEntity) {
        // Create a mock PredictionResponse from the saved analysis
        val gson = com.google.gson.Gson()
        val probabilities = gson.fromJson(analysis.probabilities, Map::class.java) as Map<String, Float>
        
        val mockResponse = """
        {
            "status": "success",
            "filename": "${analysis.filename}",
            "prediction": {
                "emotion": "${analysis.dominantEmotion}",
                "emotion_id": ${analysis.emotionId},
                "confidence": ${analysis.confidence}
            },
            "probabilities": ${gson.toJson(probabilities)},
            "features": {
                "audio_shape": ${analysis.audioFeatures},
                "video_shape": ${analysis.videoFeatures},
                "image_shape": ${analysis.imageFeatures}
            }
        }
        """.trimIndent()

        val intent = Intent(this, Results::class.java).apply {
            putExtra("prediction_response", mockResponse)
            putExtra("video_path", analysis.filePath)
            putExtra("is_new_recording", false)
            putExtra("media_type", analysis.mediaType)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun clearAllAnalyses() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear All History")
            .setMessage("Are you sure you want to delete all analysis history?")
            .setPositiveButton("Clear") { _, _ ->
                lifecycleScope.launch {
                    try {
                        database.analysisDao().deleteAllAnalyses()
                        Toast.makeText(this@ViewHistory, "History cleared", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@ViewHistory, "Error clearing history: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
