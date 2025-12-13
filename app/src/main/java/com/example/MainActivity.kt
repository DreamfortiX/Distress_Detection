package com.example.emotiondetection

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.HistoryAdapter
import com.example.AnalysisDetailsActivity
import com.example.HelpActivity
import com.example.StatisticsActivity
import com.example.SubscriptionActivity
import com.example.TutorialsActivity
import com.example.ViewHistory
import com.example.Utils.PermissionManager
import com.example.Utils.PreferencesManager
import com.example.database.AnalysisDatabase
import com.example.database.AnalysisEntity
import com.example.modals.AnalysisHistory
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.R
import com.example.myapplication.Setting_Activity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.Results
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var database: AnalysisDatabase
    private lateinit var recentAdapter: RecentAdapter
    private var shouldNavigateToRecordVideo = false

    // Permission handling
    private val permissionManager = PermissionManager(this)
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }

        if (allGranted) {
            Log.d(TAG, "All permissions granted")
            showWelcomeMessage()

            // Navigate to RecordVideoActivity if user was trying to access it
            if (shouldNavigateToRecordVideo) {
                shouldNavigateToRecordVideo = false
                navigateToRecordVideo()
            }
        } else {
            val shouldShowRationale = permissions.any {
                shouldShowRequestPermissionRationale(it.key)
            }

            if (shouldShowRationale) {
                showPermissionRationaleDialog()
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    private val drawerListener = object : DrawerLayout.DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
        override fun onDrawerOpened(drawerView: View) {}
        override fun onDrawerClosed(drawerView: View) {}
        override fun onDrawerStateChanged(newState: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeComponents()
        setupUI()
        setupListeners()
        setupObservers()
        checkInitialPermissions()

        // Check if coming from successful analysis
        val fromAnalysis = intent.getBooleanExtra("from_analysis", false)
        if (fromAnalysis) {
            showSuccessSnackbar()
        }

        // Debug: Check database status
        checkDatabaseStatus()
    }

    private fun initializeComponents() {
        preferencesManager = PreferencesManager(this)
        database = AnalysisDatabase.getDatabase(this)

        // Create RecentAdapter - clicking an item opens Results with that analysis data
        recentAdapter = RecentAdapter { analysis ->
            Log.d(TAG, "Recent item clicked: ${analysis.id}, ${analysis.filename}")
            navigateToResultsFromRecent(analysis)
        }

        // Debug: Check database
        lifecycleScope.launch {
            val count = database.analysisDao().getCount()
            Log.d(TAG, "Database initialized. Total records: $count")

            if (count > 0) {
                // Use synchronous DAO method that returns a List for debug logging
                val recent = database.analysisDao().getRecentAnalysesSync(5)
                Log.d(TAG, "Recent analyses (sync): ${recent.size}")
                recent.forEach {
                    Log.d(TAG, "  - ${it.filename} (${it.analysisDate})")
                }
            }
        }
    }

    private fun setupUI() {
        setupToolbar()
        setupNavigation()
        setupRecyclerView()
        setupSwipeRefresh()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun setupNavigation() {
        binding.navigationView.setNavigationItemSelectedListener(this)

        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.drawerLayout.addDrawerListener(drawerListener)
    }

    private fun setupRecyclerView() {
        binding.recentRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity,
                LinearLayoutManager.HORIZONTAL, false)
            adapter = recentAdapter
            setHasFixedSize(true)
            visibility = View.VISIBLE
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshAllData()
        }

        // Configure refresh colors
        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.primary),
            ContextCompat.getColor(this, R.color.secondary)
        )
    }

    private fun setupListeners() {
        binding.recordVideoCard.setOnClickListener {
            Log.d(TAG, "Record Video card clicked")
            navigateToRecordVideo()
        }

        binding.viewHistoryCard.setOnClickListener {
            Log.d(TAG, "View History card clicked")
            navigateToHistory()
        }

        binding.tutorialsCard.setOnClickListener {
            Log.d(TAG, "Tutorials card clicked")
            navigateToTutorials()
        }

        binding.settingsCard.setOnClickListener {
            Log.d(TAG, "Settings card clicked")
            navigateToSettings()
        }

        binding.fabRecord.setOnClickListener {
            Log.d(TAG, "FAB Record clicked")
            navigateToRecordVideo()
        }

        binding.btnStartFirst.setOnClickListener {
            Log.d(TAG, "Start First button clicked")
            navigateToRecordVideo()
        }

        // Debug button - long press on start button to show database info
        binding.btnStartFirst.setOnLongClickListener {
            lifecycleScope.launch {
                val count = database.analysisDao().getCount()
                val message = "Database has $count record(s)"
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                Log.d(TAG, message)

                // Show all records
                if (count > 0) {
                    val allRecords = database.analysisDao().getAllAnalysesSync()
                    Log.d(TAG, "All records:")
                    allRecords.forEachIndexed { index, record ->
                        Log.d(TAG, "  ${index + 1}. ${record.filename} - ${record.dominantEmotion} (${record.analysisDate})")
                    }
                }
            }
            true
        }
    }

    private fun setupObservers() {
        Log.d(TAG, "Setting up observers for recent analyses")

        // Observe recent analyses with debugging
        database.analysisDao().getRecentAnalyses(5).observe(this) { analyses ->
            Log.d(TAG, "Recent analyses observer triggered. Received ${analyses?.size ?: 0} items")

            analyses?.forEachIndexed { index, analysis ->
                Log.d(TAG, "  [$index] ID: ${analysis.id}, File: ${analysis.filename}, " +
                        "Emotion: ${analysis.dominantEmotion}, Date: ${analysis.analysisDate}")
            }

            if (analyses.isNullOrEmpty()) {
                Log.d(TAG, "No analyses found, showing empty state")
                showEmptyState()
            } else {
                Log.d(TAG, "Analyses found, hiding empty state and updating adapter")
                hideEmptyState()
                recentAdapter.submitList(analyses)
            }
        }
    }

    private fun checkInitialPermissions() {
        if (!permissionManager.hasAllPermissions(permissions)) {
            Log.d(TAG, "Permissions missing, requesting...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissionLauncher.launch(permissions)
            }
        } else {
            Log.d(TAG, "All permissions already granted")
            showWelcomeMessage()

            // Check if first launch
            if (preferencesManager.isFirstLaunch()) {
                Log.d(TAG, "First launch detected, showing welcome dialog")
                showWelcomeDialog()
                preferencesManager.setFirstLaunch(false)
            }
        }
    }

    private fun refreshAllData() {
        Log.d(TAG, "Refreshing all data")
        loadUserData()

        // Force refresh recent analyses
        lifecycleScope.launch {
            val recent = database.analysisDao().getRecentAnalysesSync(5)
            Log.d(TAG, "Refresh - Recent analyses count: ${recent.size}")
            withContext(Dispatchers.Main) {
                recentAdapter.submitList(recent)
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this@MainActivity, "Refreshed: ${recent.size} items", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData() {
        Log.d(TAG, "Loading user data")
        val headerView = binding.navigationView.getHeaderView(0) ?: return

        val userName = headerView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.navUserName)
        val userEmail = headerView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.navUserEmail)

        val name = preferencesManager.getUserName() ?: "Guest User"
        val email = preferencesManager.getUserEmail() ?: "guest@example.com"

        userName?.text = name
        userEmail?.text = email

        Log.d(TAG, "User loaded: $name ($email)")
    }

    private fun showEmptyState() {
        Log.d(TAG, "Showing empty state")
        binding.emptyState.visibility = View.VISIBLE
        binding.recentRecyclerView.visibility = View.GONE
        binding.recentTitle.visibility = View.GONE
    }

    private fun hideEmptyState() {
        Log.d(TAG, "Hiding empty state")
        binding.emptyState.visibility = View.GONE
        binding.recentRecyclerView.visibility = View.VISIBLE
        binding.recentTitle.visibility = View.VISIBLE
    }

    // Permission Dialogs
    private fun showWelcomeMessage() {
        Log.d(TAG, "App ready to use")
    }

    private fun showWelcomeDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("👋 Welcome to EmoScan AI!")
            .setMessage("This app uses AI to analyze emotions from videos. To get started:\n\n" +
                    "1. Record a short video of yourself\n" +
                    "2. Our AI will analyze audio, facial features, and head movements\n" +
                    "3. Get instant emotion detection results")
            .setPositiveButton("Get Started") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permissions Required")
            .setMessage("EmoScan needs camera and microphone access to record videos for emotion analysis.")
            .setPositiveButton("Grant Permissions") { _, _ ->
                requestPermissionLauncher.launch(permissions)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showPermissionDeniedDialog()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permissions Denied")
            .setMessage("EmoScan cannot function without camera and microphone permissions.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }

    private fun showSuccessSnackbar() {
        Log.d(TAG, "Showing success snackbar from analysis")
        Snackbar.make(
            binding.root,
            "🎉 Analysis completed successfully!",
            Snackbar.LENGTH_LONG
        ).setAction("View") {
            navigateToHistory()
        }.show()
    }

    // Navigation Methods
    private fun navigateToRecordVideo() {
        Log.d(TAG, "Attempting to navigate to RecordVideo")

        if (!permissionManager.hasAllPermissions(permissions)) {
            Log.d(TAG, "Permissions missing, requesting...")
            shouldNavigateToRecordVideo = true
            requestPermissionLauncher.launch(permissions)
            return
        }

        Intent(this, Record_Video::class.java).apply {
            startActivity(this)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun navigateToHistory() {
        Log.d(TAG, "Navigating to ViewHistory")
        Intent(this, ViewHistory::class.java).apply {
            startActivity(this)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun navigateToTutorials() {
        Log.d(TAG, "Navigating to Tutorials")
        Intent(this, TutorialsActivity::class.java).apply {
            startActivity(this)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun navigateToSettings() {
        Log.d(TAG, "Navigating to Settings")
        Intent(this, Setting_Activity::class.java).apply {
            startActivity(this)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    // Open Results screen from a recent analysis item using the saved AnalysisEntity
    private fun navigateToResultsFromRecent(analysis: AnalysisEntity) {
        try {
            val gson = com.google.gson.Gson()
            val probabilities =
                gson.fromJson(analysis.probabilities, Map::class.java) as Map<String, Float>

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

            Intent(this, Results::class.java).apply {
                putExtra("prediction_response", mockResponse)
                putExtra("video_path", analysis.filePath)
                putExtra("is_new_recording", false)
                putExtra("media_type", analysis.mediaType)
                startActivity(this)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to Results from recent: ${e.message}")
            Toast.makeText(this,
                "Unable to open results for this analysis", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToHelp() {
        Log.d(TAG, "Navigating to Help")
        try {
            Intent(this, HelpActivity::class.java).apply {
                startActivity(this)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        } catch (e: Exception) {
            Log.e(TAG, "HelpActivity not found: ${e.message}")
            Toast.makeText(this, "Help feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    // Menu Handling
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_notifications -> {
                Toast.makeText(this, "Notifications coming soon", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_refresh -> {
                refreshAllData()
                return true
            }
            R.id.action_settings -> {
                navigateToSettings()
                return true
            }
            R.id.action_help -> {
                navigateToHelp()
                return true
            }
            R.id.action_about -> {
                showAboutDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Navigation Drawer Handling
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                Log.d(TAG, "Dashboard selected")
            }
            R.id.nav_record -> {
                Log.d(TAG, "Record selected from drawer")
                navigateToRecordVideo()
            }
            R.id.nav_history -> {
                Log.d(TAG, "History selected from drawer")
                navigateToHistory()
            }
            R.id.nav_tutorials -> {
                Log.d(TAG, "Tutorials selected from drawer")
                navigateToTutorials()
            }
            R.id.nav_settings -> {
                Log.d(TAG, "Settings selected from drawer")
                navigateToSettings()
            }
            R.id.nav_help -> {
                Log.d(TAG, "Help selected from drawer")
                navigateToHelp()
            }
            R.id.nav_profile -> {
                Log.d(TAG, "Profile selected")
                Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_statistics -> {
                Log.d(TAG, "Statistics selected")
                Toast.makeText(this, "Statistics coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_share -> {
                Log.d(TAG, "Share selected")
                shareApp()
            }
            R.id.nav_rate -> {
                Log.d(TAG, "Rate selected")
                rateApp()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Utility Methods
    private fun shareApp() {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out EmoScan AI: https://play.google.com/store/apps/details?id=$packageName")
            startActivity(Intent.createChooser(this, "Share App"))
        }
    }

    private fun rateApp() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About EmoScan AI")
            .setMessage("Version: 1.0.0\n\nEmoScan AI uses advanced machine learning to detect emotions from video recordings.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun checkDatabaseStatus() {
        lifecycleScope.launch {
            val count = database.analysisDao().getCount()
            Log.d(TAG, "Database status check - Total records: $count")

            if (count == 0) {
                Log.d(TAG, "Database is empty. Recent analyses will not show.")
                // For testing, automatically add a debug record so Recent Analysis shows an item
                addTestRecordForDebugging()
            }
        }
    }

    // For debugging - add a test record
    private fun addTestRecordForDebugging() {
        lifecycleScope.launch {
            try {
                val testRecord = AnalysisEntity(
                    filename = "Test Recording",
                    filePath = "/test/path",
                    mediaType = "video",
                    dominantEmotion = "happy",
                    emotionId = 1,
                    confidence = 0.85f,
                    probabilities = "{\"happy\":0.85,\"sad\":0.10,\"neutral\":0.05}",
                    analysisDate = System.currentTimeMillis(),
                    description = "Debug test record",
                    audioFeatures = "[]",
                    videoFeatures = "[]",
                    imageFeatures = "[]"
                )
                database.analysisDao().insertAnalysis(testRecord)
                Log.d(TAG, "Test record added for debugging")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding test record: ${e.message}")
            }
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.drawerLayout.removeDrawerListener(drawerListener)
        Log.d(TAG, "MainActivity destroyed")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity resumed")
        loadUserData()

        // Refresh recent analyses when returning to the activity
        lifecycleScope.launch {
            val recent = database.analysisDao().getRecentAnalysesSync(5)
            Log.d(TAG, "OnResume - Recent analyses: ${recent.size}")
            withContext(Dispatchers.Main) {
                if (recent.isNotEmpty()) {
                    recentAdapter.submitList(recent)
                }
            }
        }
    }
}

// New RecentAdapter class - reuses item_history layout for recent analyses
class RecentAdapter(private val onItemClick: (AnalysisEntity) -> Unit) :
    ListAdapter<AnalysisEntity, RecentAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.filenameText)
        val date: TextView = view.findViewById(R.id.dateText)
        val emotion: TextView = view.findViewById(R.id.emotionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val analysis = getItem(position)
        holder.title.text = analysis.filename
        holder.date.text = formatDate(analysis.analysisDate)
        holder.emotion.text = analysis.dominantEmotion.uppercase()

        holder.itemView.setOnClickListener {
            onItemClick(analysis)
        }
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
        } catch (e: Exception) {
            "Recent"
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