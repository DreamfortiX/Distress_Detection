package com.example.emotiondetection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GalleryAdapter(
    private var videos: MutableList<File>,
    private val onVideoClick: (File) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.VideoViewHolder>() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.videoThumbnail)
        val title: TextView = itemView.findViewById(R.id.videoTitle)
        val duration: TextView = itemView.findViewById(R.id.videoDuration)
        val size: TextView = itemView.findViewById(R.id.videoSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoFile = videos[position]
        
        holder.title.text = videoFile.name
        holder.duration.text = formatDuration(videoFile)
        holder.size.text = formatFileSize(videoFile.length())
        
        // Set thumbnail placeholder (you could implement video thumbnail extraction here)
        holder.thumbnail.setImageResource(R.drawable.ic_video_file)
        
        holder.itemView.setOnClickListener {
            onVideoClick(videoFile)
        }
    }

    override fun getItemCount(): Int = videos.size

    fun updateVideos(newVideos: MutableList<File>) {
        videos.clear()
        videos.addAll(newVideos)
        notifyDataSetChanged()
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
        }
    }

    private fun formatDuration(file: File): String {
        // This is a placeholder - you could implement actual video duration extraction
        // For now, we'll return the file modification time as a simple format
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return dateFormat.format(Date(file.lastModified()))
    }
}
