package com.example

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.database.AnalysisEntity
import com.example.myapplication.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onItemClick: (AnalysisEntity) -> Unit
) : ListAdapter<AnalysisEntity, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding,
        private val onItemClick: (AnalysisEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(analysis: AnalysisEntity) {
            binding.apply {
                // Set filename
                filenameText.text = analysis.filename
                
                // Set media type
                mediaTypeText.text = analysis.mediaType.uppercase()
                
                // Set emotion
                emotionText.text = analysis.dominantEmotion.uppercase()
                
                // Set confidence
                confidenceText.text = analysis.getConfidenceFormatted()
                
                // Set date
                dateText.text = analysis.getFormattedDate()
                
                // Set emotion icon and color based on emotion
                val emotionColors = mapOf(
                    "neutral" to com.example.myapplication.R.color.neutral,
                    "calm" to com.example.myapplication.R.color.calm,
                    "happy" to com.example.myapplication.R.color.happy,
                    "sad" to com.example.myapplication.R.color.sad,
                    "angry" to com.example.myapplication.R.color.angry,
                    "fearful" to com.example.myapplication.R.color.sad,
                    "disgust" to com.example.myapplication.R.color.angry,
                    "surprised" to com.example.myapplication.R.color.surprised
                )
                
                val emotionIcons = mapOf(
                    "neutral" to com.example.myapplication.R.drawable.ic_neutral,
                    "calm" to com.example.myapplication.R.drawable.ic_calm,
                    "happy" to com.example.myapplication.R.drawable.ic_happy,
                    "sad" to com.example.myapplication.R.drawable.ic_sad,
                    "angry" to com.example.myapplication.R.drawable.ic_angry,
                    "fearful" to com.example.myapplication.R.drawable.ic_fear,  // Use ic_fear instead of ic_fearful
                    "disgust" to com.example.myapplication.R.drawable.ic_disgust,
                    "surprised" to com.example.myapplication.R.drawable.ic_surprised
                )
                
                // Set emotion icon
                emotionIcons[analysis.dominantEmotion]?.let {
                    emotionIcon.setImageResource(it)
                }
                
                // Set card background color
                emotionColors[analysis.dominantEmotion]?.let { colorRes: Int ->
                    emotionCard.setCardBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(
                            root.context,
                            colorRes
                        )
                    )
                }
                
                // Set click listener
                root.setOnClickListener {
                    onItemClick(analysis)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AnalysisEntity>() {
        override fun areItemsTheSame(oldItem: AnalysisEntity, newItem: AnalysisEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AnalysisEntity, newItem: AnalysisEntity): Boolean {
            return oldItem == newItem
        }
    }
}
