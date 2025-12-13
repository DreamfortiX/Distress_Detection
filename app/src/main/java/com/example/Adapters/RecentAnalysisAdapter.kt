package com.example.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.modals.AnalysisHistory
import com.google.android.material.card.MaterialCardView
import android.widget.ImageView
import com.example.myapplication.R

class RecentAnalysisAdapter(
    private val onItemClick: (AnalysisHistory) -> Unit
) : ListAdapter<AnalysisHistory, RecentAnalysisAdapter.ViewHolder>(AnalysisDiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.analysisCard)
        val emotionIcon: ImageView = itemView.findViewById(R.id.emotionIcon)
        val emotionText: TextView = itemView.findViewById(R.id.emotionText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val confidenceText: TextView = itemView.findViewById(R.id.confidenceText)
        val filenameText: TextView = itemView.findViewById(R.id.filenameText)
        val mediaTypeText: TextView = itemView.findViewById(R.id.mediaTypeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val analysis = getItem(position)

        holder.emotionText.text = analysis.emotion
        holder.dateText.text = analysis.getFormattedDate()
        holder.confidenceText.text = analysis.getConfidenceFormatted()

        // Set filename from videoPath or use default
        val filename = analysis.videoPath?.let { path ->
            path.substringAfterLast("/")
        } ?: "Unknown"
        holder.filenameText.text = filename

        // Set media type (assume video for now)
        holder.mediaTypeText.text = "VIDEO"

        // Set emotion icon based on emotion type
        val iconRes = when (analysis.emotion.toLowerCase()) {
            "happy" -> R.drawable.ic_happy
            "sad" -> R.drawable.ic_sad
            "angry" -> R.drawable.ic_angry
            "neutral" -> R.drawable.ic_neutral
            "surprised" -> R.drawable.ic_surprised
            else -> R.drawable.ic_face
        }
        holder.emotionIcon.setImageResource(iconRes)

        holder.cardView.setOnClickListener {
            onItemClick(analysis)
        }
    }
}

class AnalysisDiffCallback : DiffUtil.ItemCallback<AnalysisHistory>() {
    override fun areItemsTheSame(oldItem: AnalysisHistory, newItem: AnalysisHistory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AnalysisHistory, newItem: AnalysisHistory): Boolean {
        return oldItem == newItem
    }
}