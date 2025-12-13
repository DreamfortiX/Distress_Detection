package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import de.hdodenhof.circleimageview.CircleImageView
import android.provider.MediaStore
import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.example.AuthManager
import com.example.LoginActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import java.io.File
import java.io.FileOutputStream

class Setting_Activity : AppCompatActivity() {

    private lateinit var profileImage: CircleImageView
    private lateinit var profileName: android.widget.TextView
    private lateinit var profileEmail: android.widget.TextView
    private lateinit var btnEditProfile: com.google.android.material.button.MaterialButton
    private lateinit var videoDurationText: android.widget.TextView
    private lateinit var switchAutoSave: SwitchMaterial
    private lateinit var switchAnalytics: SwitchMaterial
    private lateinit var switchSaveData: SwitchMaterial
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var switchEmailReports: SwitchMaterial
    private lateinit var btnLogout: com.google.android.material.button.MaterialButton
    private lateinit var btnDeleteAccount: com.google.android.material.button.MaterialButton
    private lateinit var authManager: AuthManager

    private lateinit var sharedPreferences: SharedPreferences
    private val PROFILE_IMAGE_KEY = "profile_image_uri"
    private val PROFILE_NAME_KEY = "profile_name"
    private val PROFILE_EMAIL_KEY = "profile_email"

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private const val REQUEST_CAMERA_PERMISSION = 101
        private const val REQUEST_STORAGE_PERMISSION = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Initialize auth manager
        authManager = AuthManager(this)

        // Initialize views
        initializeViews()

        // Load saved settings
        loadSavedSettings()

        // Set up click listeners
        setupClickListeners()

        // Set up switch listeners
        setupSwitchListeners()
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.profileImage)
        profileName = findViewById(R.id.profileName)
        profileEmail = findViewById(R.id.profileEmail)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        videoDurationText = findViewById(R.id.videoDurationText)
        switchAutoSave = findViewById(R.id.switchAutoSave)
        switchAnalytics = findViewById(R.id.switchAnalytics)
        switchSaveData = findViewById(R.id.switchSaveData)
        switchNotifications = findViewById(R.id.switchNotifications)
        switchEmailReports = findViewById(R.id.switchEmailReports)
        btnLogout = findViewById(R.id.btnLogout)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
    }

    private fun loadSavedSettings() {
        // Load profile data
        val savedImageUri = sharedPreferences.getString(PROFILE_IMAGE_KEY, null)
        if (savedImageUri != null) {
            profileImage.setImageURI(Uri.parse(savedImageUri))
        }

        val savedName = sharedPreferences.getString(PROFILE_NAME_KEY, null)
        val savedEmail = sharedPreferences.getString(PROFILE_EMAIL_KEY, null)

        val authEmail = authManager.getUserEmail()
        val authName = authManager.getUserName()

        val finalEmail = authEmail ?: savedEmail ?: "user@example.com"
        val finalName = authName
            ?: savedName
            ?: (finalEmail.substringBefore('@').takeIf { it.isNotBlank() }?.replaceFirstChar { c -> c.uppercase() } ?: "User Name")

        profileName.text = finalName
        profileEmail.text = finalEmail

        // Load settings
        switchAutoSave.isChecked = sharedPreferences.getBoolean("auto_save", true)
        switchAnalytics.isChecked = sharedPreferences.getBoolean("analytics", false)
        switchSaveData.isChecked = sharedPreferences.getBoolean("save_data", true)
        switchNotifications.isChecked = sharedPreferences.getBoolean("notifications", true)
        switchEmailReports.isChecked = sharedPreferences.getBoolean("email_reports", false)

        // Load video duration
        val videoDuration = sharedPreferences.getInt("video_duration", 10)
        updateVideoDurationText(videoDuration)
    }

    private fun setupClickListeners() {
        // Profile Image click
        profileImage.setOnClickListener {
            showImagePickerDialog()
        }

        // Edit Profile button
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        // Video duration text click
        videoDurationText.setOnClickListener {
            showVideoDurationDialog()
        }

        // Open Source Libraries click
        findViewById<android.view.View>(R.id.openSourceLayout ?: android.R.id.content).setOnClickListener {
            openOpenSourceLibraries()
        }

        // Privacy Policy click
        findViewById<android.view.View>(R.id.privacyLayout ?: android.R.id.content).setOnClickListener {
            openPrivacyPolicy()
        }

        // Logout button
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // Delete Account button
        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun setupSwitchListeners() {
        switchAutoSave.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("auto_save", isChecked).apply()
            showToast("Auto-save ${if (isChecked) "enabled" else "disabled"}")
        }

        switchAnalytics.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("analytics", isChecked).apply()
            showToast("Analytics ${if (isChecked) "enabled" else "disabled"}")
        }

        switchSaveData.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("save_data", isChecked).apply()
            showToast("Data saving ${if (isChecked) "enabled" else "disabled"}")
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notifications", isChecked).apply()
            showToast("Notifications ${if (isChecked) "enabled" else "disabled"}")
        }

        switchEmailReports.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("email_reports", isChecked).apply()
            showToast("Email reports ${if (isChecked) "enabled" else "disabled"}")
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        MaterialAlertDialogBuilder(this)
            .setTitle("Change Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> chooseFromGallery()
                    2 -> { /* Cancel */ }
                }
            }
            .show()
    }

    private fun takePhoto() {
        if (checkCameraPermission()) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_PICK)
            }
        } else {
            requestCameraPermission()
        }
    }

    private fun chooseFromGallery() {
        if (checkStoragePermission()) {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickPhotoIntent.type = "image/*"
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
        } else {
            requestStoragePermission()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    showToast("Camera permission denied")
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseFromGallery()
                } else {
                    showToast("Storage permission denied")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                profileImage.setImageURI(imageUri)
                // Save the URI as string
                sharedPreferences.edit().putString(PROFILE_IMAGE_KEY, imageUri.toString()).apply()
                showToast("Profile picture updated")
            } else if (data?.extras?.get("data") != null) {
                // Handle camera image
                val bitmap = data.extras?.get("data") as android.graphics.Bitmap
                profileImage.setImageBitmap(bitmap)
                // Save bitmap to file and save URI
                val uri = saveBitmapToFile(bitmap)
                if (uri != null) {
                    sharedPreferences.edit().putString(PROFILE_IMAGE_KEY, uri.toString()).apply()
                }
                showToast("Profile picture updated")
            }
        }
    }

    private fun saveBitmapToFile(bitmap: android.graphics.Bitmap): Uri? {
        return try {
            val file = File.createTempFile("profile_", ".jpg", cacheDir)
            val stream = FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
            stream.flush()
            stream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val editName = dialogView.findViewById<android.widget.EditText>(R.id.editName)
        val editEmail = dialogView.findViewById<android.widget.EditText>(R.id.editEmail)

        editName.setText(profileName.text)
        editEmail.setText(profileEmail.text)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = editName.text.toString().trim()
                val newEmail = editEmail.text.toString().trim()

                if (newName.isNotEmpty() && newEmail.isNotEmpty()) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        profileName.text = newName
                        profileEmail.text = newEmail

                        sharedPreferences.edit()
                            .putString(PROFILE_NAME_KEY, newName)
                            .putString(PROFILE_EMAIL_KEY, newEmail)
                            .apply()

                        showToast("Profile updated successfully")
                    } else {
                        showToast("Please enter a valid email address")
                    }
                } else {
                    showToast("Please fill all fields")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showVideoDurationDialog() {
        val currentDuration = sharedPreferences.getInt("video_duration", 10)

        val dialogView = layoutInflater.inflate(R.layout.dialog_video_duration, null)
        val slider = dialogView.findViewById<Slider>(R.id.sliderDuration)
        val textValue = dialogView.findViewById<android.widget.TextView>(R.id.textDurationValue)

        slider.valueFrom = 5f
        slider.valueTo = 60f
        slider.stepSize = 5f
        slider.value = currentDuration.toFloat()

        textValue.text = "${currentDuration} seconds"

        slider.addOnChangeListener { _, value, _ ->
            textValue.text = "${value.toInt()} seconds"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Video Duration")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val newDuration = slider.value.toInt()
                sharedPreferences.edit().putInt("video_duration", newDuration).apply()
                updateVideoDurationText(newDuration)
                showToast("Video duration set to $newDuration seconds")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateVideoDurationText(duration: Int) {
        videoDurationText.text = "$duration seconds"
    }

    private fun openOpenSourceLibraries() {
        val intent = Intent(this, OpenSourceActivity::class.java)
        startActivity(intent)
    }

    private fun openPrivacyPolicy() {
        val uri = Uri.parse("https://www.example.com/privacy-policy")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAccountConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Account")
            .setMessage("This action will permanently delete your account and all associated data. This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                showFinalDeleteConfirmation()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFinalDeleteConfirmation() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_confirmation, null)
        val editText = dialogView.findViewById<android.widget.EditText>(R.id.editConfirmText)

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Account Deletion")
            .setView(dialogView)
            .setMessage("Type 'DELETE' to confirm account deletion")
            .setPositiveButton("Delete Account") { _, _ ->
                if (editText.text.toString() == "DELETE") {
                    performAccountDeletion()
                } else {
                    showToast("Confirmation text does not match")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear user session
        sharedPreferences.edit().clear().apply()
        authManager.logout()

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        showToast("Logged out successfully")
    }

    private fun performAccountDeletion() {
        // TODO: Implement actual account deletion API call

        // Simulate API call
        showLoadingDialog("Deleting account...")

        android.os.Handler(mainLooper).postDelayed({
            // Clear all data
            sharedPreferences.edit().clear().apply()
            authManager.logout()

            // Navigate to login screen
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            showToast("Account deleted successfully")
        }, 2000)
    }

    private fun showLoadingDialog(message: String) {
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        dialog.show()

        // Auto-dismiss after 2 seconds (simulating API call)
        android.os.Handler(mainLooper).postDelayed({
            dialog.dismiss()
        }, 2000)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}