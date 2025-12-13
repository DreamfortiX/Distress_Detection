package com.example

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.Network.PredictionResponse
import com.example.database.AnalysisDatabase
import com.example.database.AnalysisEntity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityResultsBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Results : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding
    private lateinit var predictionResponse: PredictionResponse
    private lateinit var videoPath: String
    private var isNewRecording: Boolean = false
    private lateinit var database: AnalysisDatabase

    // Emotion color mapping
    private val emotionColors: Map<String, Int> = mapOf(
        "neutral" to R.color.neutral,
        "calm" to R.color.calm,
        "happy" to R.color.happy,
        "sad" to R.color.sad,
        "angry" to R.color.angry,
        "fearful" to R.color.sad,
        "disgust" to R.color.angry,
        "surprised" to R.color.surprised
    )

    // Emotion icon mapping
    private val emotionIcons: Map<String, Int> = mapOf(
        "neutral" to R.drawable.ic_neutral,
        "calm" to R.drawable.ic_calm,
        "happy" to R.drawable.ic_happy,
        "sad" to R.drawable.ic_sad,
        "angry" to R.drawable.ic_angry,
        "fearful" to R.drawable.ic_fear,
        "disgust" to R.drawable.ic_disgust,
        "surprised" to R.drawable.ic_surprised
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database
        database = AnalysisDatabase.getDatabase(this)

        // Get data from intent
        val responseJson = intent.getStringExtra("prediction_response")
        videoPath = intent.getStringExtra("video_path") ?: ""
        isNewRecording = intent.getBooleanExtra("is_new_recording", false)
        val mediaType = intent.getStringExtra("media_type") ?: "video"

        if (responseJson != null) {
            try {
                predictionResponse = Gson().fromJson(responseJson, PredictionResponse::class.java)
                setupUI()
                saveAnalysisToDatabase(mediaType)
            } catch (e: Exception) {
                Toast.makeText(this, "Error parsing response: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, "No prediction data received", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveAnalysisToDatabase(mediaType: String) {
        lifecycleScope.launch {
            try {
                val analysis = AnalysisEntity(
                    filename = predictionResponse.filename,
                    filePath = videoPath,
                    mediaType = mediaType,
                    dominantEmotion = predictionResponse.prediction.emotion,
                    emotionId = predictionResponse.prediction.emotion_id,
                    confidence = predictionResponse.prediction.confidence,
                    probabilities = Gson().toJson(predictionResponse.probabilities),
                    description = "", // Can be enhanced to include user descriptions
                    audioFeatures = predictionResponse.features?.let { 
                        Gson().toJson(mapOf("audio_shape" to it.audio_shape))
                    } ?: "",
                    videoFeatures = predictionResponse.features?.let { 
                        Gson().toJson(mapOf("video_shape" to it.video_shape))
                    } ?: "",
                    imageFeatures = predictionResponse.features?.let { 
                        Gson().toJson(mapOf("image_shape" to it.image_shape))
                    } ?: ""
                )
                
                val analysisId = database.analysisDao().insertAnalysis(analysis)
                Log.d("Results", "Analysis saved to database with ID: $analysisId")
                
            } catch (e: Exception) {
                Log.e("Results", "Error saving analysis to database: ${e.message}", e)
                // Don't show error to user as the main functionality still works
            }
        }
    }

    private fun setupUI() {
        // Set filename
        binding.filenameText.text = predictionResponse.filename

        // Set current date and time
        val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        binding.analysisDate.text = dateFormat.format(Date())

        // Setup emotion card
        setupEmotionCard()

        // Setup emotion probabilities list
        setupEmotionProbabilities()

        // Setup features section
        setupFeaturesSection()

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupEmotionCard() {
        try {
            // Add null safety checks
            val prediction = predictionResponse.prediction
            if (prediction == null) {
                Toast.makeText(this, "Prediction data is null", Toast.LENGTH_SHORT).show()
                return
            }

            val emotion = prediction.emotion
            val emotionId = prediction.emotion_id
            val confidence = prediction.confidence

            // Check if emotion is not null or empty
            if (emotion.isNullOrEmpty()) {
                Toast.makeText(this, "Emotion data is missing", Toast.LENGTH_SHORT).show()
                return
            }

            // Set emotion text
            binding.emotionText.text = emotion.uppercase()
            binding.emotionIdText.text = "ID: $emotionId"

            // Set confidence
            val confidencePercent = (confidence * 100).toInt()
            binding.confidenceText.text = "$confidencePercent%"
            binding.confidenceProgressBar.progress = confidencePercent

            // Set emotion icon
            emotionIcons[emotion]?.let {
                binding.emotionIcon.setImageResource(it)
            }

            // Set card background color
            emotionColors[emotion]?.let {
                binding.emotionCard.setCardBackgroundColor(ContextCompat.getColor(this, it))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up emotion card: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun setupEmotionProbabilities() {
        try {
            val container = binding.emotionListContainer
            container.removeAllViews()

            // Add null safety check for probabilities
            val probabilities = predictionResponse.probabilities
            if (probabilities == null || probabilities.isEmpty()) {
                Toast.makeText(this, "No probability data available", Toast.LENGTH_SHORT).show()
                return
            }

            // Sort probabilities by value (descending)
            val sortedProbabilities = probabilities.entries
                .sortedByDescending { it.value }

            for ((emotion, probability) in sortedProbabilities) {
                val emotionItemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_emotion_result, container, false)

                // Find views in the item layout
                val emotionName = emotionItemView.findViewById<TextView>(R.id.emotionName)
                val emotionPercent = emotionItemView.findViewById<TextView>(R.id.emotionPercent)
                val progressBar = emotionItemView.findViewById<LinearProgressIndicator>(R.id.emotionProgress)
                val emotionIcon = emotionItemView.findViewById<ImageView>(R.id.emotionIcon)
                val emotionCard = emotionItemView.findViewById<MaterialCardView>(R.id.emotionCard)

                // Set emotion name
                emotionName.text = emotion.uppercase()

                // Set emotion percentage
                val percent = (probability * 100).toInt()
                emotionPercent.text = "$percent%"

                // Set progress bar
                progressBar.progress = percent

                // Set emotion icon
                emotionIcons[emotion]?.let {
                    emotionIcon.setImageResource(it)
                }

                // Set card background color
                emotionColors[emotion]?.let {
                    emotionCard.setCardBackgroundColor(ContextCompat.getColor(this, it))
                }

                // Add to container
                container.addView(emotionItemView)

                // Add click listener to each item
                emotionCard.setOnClickListener {
                    showEmotionDetails(emotion, probability)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up probabilities: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFeaturesSection() {
        predictionResponse.features?.let { features ->
            // Show features section
            binding.analysisDetails.visibility = View.VISIBLE

            // Audio features
            if (features.audio_shape.isNotEmpty()) {
                binding.audioFeaturesLayout.visibility = View.VISIBLE
                binding.audioFeaturesText.text = "${features.audio_shape[0]} dimensions"
            }

            // Video features
            if (features.video_shape.isNotEmpty()) {
                binding.videoFeaturesLayout.visibility = View.VISIBLE
                binding.videoFeaturesText.text = "${features.video_shape[0]} dimensions"
            }

            // Image features
            if (features.image_shape.isNotEmpty()) {
                binding.imageFeaturesLayout.visibility = View.VISIBLE
                binding.imageFeaturesText.text = "${features.image_shape[0]} dimensions"
            }
        } ?: run {
            // Hide features section if not available
            binding.analysisDetails.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // Play video button
        binding.playVideoButton.setOnClickListener {
            playVideo()
        }

        // Retry button (Analyze Another)
        binding.retryButton.setOnClickListener {
            finish()
        }

        // Share button
        binding.shareButton.setOnClickListener {
            shareResults()
        }
    }

    private fun showEmotionDetails(emotion: String, probability: Float) {
        val percent = (probability * 100).toInt()
        Toast.makeText(
            this,
            "$emotion: $percent% confidence",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun shareResults() {
        val emotion = predictionResponse.prediction.emotion
        val confidence = (predictionResponse.prediction.confidence * 100).toInt()

        val shareText = """
            🎭 Emotion Detection Results
            
            📊 Detected Emotion: $emotion
            ✅ Confidence: $confidence%
            
            📈 Full Analysis:
            ${predictionResponse.probabilities.entries
            .sortedByDescending { it.value }
            .joinToString("\n") { (emo, prob) ->
                "• ${emo.uppercase()}: ${(prob * 100).toInt()}%"
            }
        }
            
            📁 File: ${predictionResponse.filename}
            🕒 Date: ${SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date())}
            
            Powered by Multimodal Emotion Recognition AI
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Emotion Analysis Results")
        }

        startActivity(Intent.createChooser(shareIntent, "Share Results"))
    }

    private fun playVideo() {
        try {
            // Debug logging
            Log.d("Results", "Video path: $videoPath")
            Log.d("Results", "Is new recording: $isNewRecording")
            
            // Check if video path is valid
            if (videoPath.isEmpty()) {
                Toast.makeText(this, "No video path available", Toast.LENGTH_SHORT).show()
                return
            }
            
            val videoFile = File(videoPath)
            if (!videoFile.exists()) {
                Toast.makeText(this, "Video file not found: $videoPath", Toast.LENGTH_SHORT).show()
                return
            }
            
            val uri = if (isNewRecording) {
                // For newly recorded videos
                Uri.parse(videoPath)
            } else {
                // For gallery videos - use FileProvider for better compatibility
                try {
                    FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        videoFile
                    )
                } catch (e: Exception) {
                    // Fallback to file URI
                    Uri.fromFile(videoFile)
                }
            }

            Log.d("Results", "Video URI: $uri")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "No video player found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("Results", "Error playing video: ${e.message}", e)
            Toast.makeText(this, "Error playing video: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}