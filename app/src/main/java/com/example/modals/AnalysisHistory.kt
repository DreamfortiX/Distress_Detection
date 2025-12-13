package com.example.modals

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class AnalysisHistory(
    val id: Int,
    val emotion: String,
    val confidence: Double,
    val date: Long,
    val thumbnail: String?,
    val videoPath: String?,
    val audioFeatures: List<Float> = emptyList(),
    val headPose: Triple<Float, Float, Float>? = null,
    val faceEmbedding: List<Float> = emptyList()
) : Parcelable {

    fun getFormattedDate(): String {
        val date = Date(date)
        val formatter = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }

    fun getConfidenceFormatted(): String = String.format("%.1f%%", confidence)
}