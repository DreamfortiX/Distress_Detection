package com.example

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.models.Tutorial
import com.example.myapplication.R

class TutorialsAdapter(
    private val onItemClick: (Tutorial) -> Unit
) : ListAdapter<Tutorial, TutorialsAdapter.TutorialViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<Tutorial>() {
        override fun areItemsTheSame(oldItem: Tutorial, newItem: Tutorial): Boolean =
            oldItem.title == newItem.title && oldItem.subtitle == newItem.subtitle
        override fun areContentsTheSame(oldItem: Tutorial, newItem: Tutorial): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tutorial, parent, false)
        return TutorialViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TutorialViewHolder(
        itemView: View,
        private val onItemClick: (Tutorial) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tutorialTitle)
        private val description: TextView = itemView.findViewById(R.id.tutorialDescription)
        private val duration: TextView = itemView.findViewById(R.id.tutorialDuration)
        private val level: TextView = itemView.findViewById(R.id.tutorialLevel)
        private val play: ImageView = itemView.findViewById(R.id.playButton)
        private val thumb: ImageView = itemView.findViewById(R.id.tutorialThumbnail)

        fun bind(tutorial: Tutorial) {
            title.text = tutorial.title
            description.text = tutorial.subtitle
            // Optional static meta
            duration.text = "5:30"
            level.text = "Beginner"
            thumb.setImageResource(tutorial.iconResId)

            itemView.setOnClickListener { onItemClick(tutorial) }
            play.setOnClickListener { onItemClick(tutorial) }
        }
    }
}
