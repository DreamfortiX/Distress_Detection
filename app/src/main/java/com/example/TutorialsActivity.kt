package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.models.Tutorial
import com.example.myapplication.R

class TutorialsActivity : AppCompatActivity() {

    private lateinit var adapter: TutorialsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorials)

        setupToolbar()
        setupRecyclerView()
        loadTutorials()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Tutorials"
        }

        toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun setupRecyclerView() {
        val tutorialsRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.tutorialsRecyclerView)
        adapter = TutorialsAdapter { tutorial ->
            tutorial.videoUrl?.let { url ->
                runCatching {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
        }
        tutorialsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TutorialsActivity)
            adapter = this@TutorialsActivity.adapter
            setHasFixedSize(true)
            itemAnimator = null
            isNestedScrollingEnabled = false
        }
    }

    private fun loadTutorials() {
        // TODO: Replace with real data source if available
        val items = listOf(
            Tutorial(
                title = "Getting Started",
                subtitle = "Learn the basics of EmoScan AI",
                iconResId = R.drawable.ic_video_record,
                videoUrl = null
            ),
            Tutorial(
                title = "Recording Videos",
                subtitle = "How to record quality videos for analysis",
                iconResId = R.drawable.ic_video_record,
                videoUrl = null
            ),
            Tutorial(
                title = "Understanding Results",
                subtitle = "How to read and interpret emotion analysis",
                iconResId = R.drawable.ic_history,
                videoUrl = null
            ),
            Tutorial(
                title = "Tips & Tricks",
                subtitle = "Pro tips for better emotion analysis",
                iconResId = R.drawable.ic_help,
                videoUrl = null
            )
        )

        adapter.submitList(items)
        findViewById<View>(R.id.loadingProgressBar).visibility = View.GONE
        findViewById<View>(R.id.contentLayout).visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
