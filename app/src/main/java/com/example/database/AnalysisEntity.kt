package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "analysis_history")
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filename: String,
    val filePath: String,
    val mediaType: String, // "image" or "video"
    val dominantEmotion: String,
    val emotionId: Int,
    val confidence: Float,
    val probabilities: String, // JSON string of emotion probabilities
    val analysisDate: Long = System.currentTimeMillis(),
    val description: String = "",
    val audioFeatures: String = "", // JSON string
    val videoFeatures: String = "", // JSON string
    val imageFeatures: String = ""  // JSON string
) {
    fun getFormattedDate(): String {
        val date = Date(analysisDate)
        val formatter = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }

    fun getConfidenceFormatted(): String = String.format("%.1f%%", confidence * 100)
}
