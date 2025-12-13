package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        loadNotifications()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Notifications"
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun setupRecyclerView() {
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            // adapter will be set when data is loaded
        }
    }

    private fun loadNotifications() {
        // Load notifications from database or preferences
        // For now, show empty state
        showEmptyState()
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = android.view.View.VISIBLE
        binding.notificationsRecyclerView.visibility = android.view.View.GONE
    }

    private fun hideEmptyState() {
        binding.emptyStateLayout.visibility = android.view.View.GONE
        binding.notificationsRecyclerView.visibility = android.view.View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
