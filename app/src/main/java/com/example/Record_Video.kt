package com.example.emotiondetection

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Insets.add
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.QualitySelector
import androidx.camera.video.Quality
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.Network.EmotionDetectionRepository
import com.example.Network.PredictionResponse
import com.example.Results
import com.example.Utils.LoadingDialog
import com.example.myapplication.databinding.ActivityRecordVideoBinding
import com.example.myapplication.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.FileOutputStream
import java.io.InputStream

class Record_Video : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 100
    private val PICK_VIDEO_REQUEST = 101
    private lateinit var selectedMediaUri: Uri
    private lateinit var selectedMediaFile: File
    private var selectedMediaType: MediaType = MediaType.NONE

    // UI Elements
    private lateinit var btnSelectVideo: com.google.android.material.card.MaterialCardView
    private lateinit var btnUpload: Button
    private lateinit var tvSelectedFile: TextView
    private lateinit var etDescription: EditText
    private lateinit var tvProgress: TextView

    enum class MediaType {
        NONE, IMAGE, VIDEO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_video)

        // Initialize UI elements
        btnSelectVideo = findViewById(R.id.btnSelectVideo)
        btnUpload = findViewById(R.id.btnUpload)
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        etDescription = findViewById(R.id.etDescription)
        tvProgress = findViewById(R.id.tvProgress)

        btnSelectVideo.setOnClickListener {
            selectVideo()
        }

        btnUpload.setOnClickListener {
            uploadMedia()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // For Android 4.4 and above
        val chooserIntent = Intent.createChooser(intent, "Select an Image")
        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST)
    }

    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // For Android 4.4 and above
        val chooserIntent = Intent.createChooser(intent, "Select a Video")
        startActivityForResult(chooserIntent, PICK_VIDEO_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                selectedMediaUri = data.data!!
                val fileName = getFileName(selectedMediaUri)
                
                when (requestCode) {
                    PICK_IMAGE_REQUEST -> {
                        selectedMediaType = MediaType.IMAGE
                        tvSelectedFile.text = "Selected Image: $fileName"
                    }
                    PICK_VIDEO_REQUEST -> {
                        selectedMediaType = MediaType.VIDEO
                        tvSelectedFile.text = "Selected Video: $fileName"
                    }
                }

                // Enable upload button
                btnUpload.isEnabled = true

                // Create a temporary file from the URI
                selectedMediaFile = createTempFileFromUri(selectedMediaUri, fileName ?: "media")
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    private fun createTempFileFromUri(uri: Uri, fileName: String): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val tempFile = File(cacheDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun uploadMedia() {
        if (!::selectedMediaFile.isInitialized || selectedMediaType == MediaType.NONE) {
            Toast.makeText(this, "Please select an image or video first", Toast.LENGTH_SHORT).show()
            return
        }
        tvProgress.visibility = View.VISIBLE
        val mediaTypeText = if (selectedMediaType == MediaType.IMAGE) "image" else "video"
        tvProgress.text = "Uploading $mediaTypeText..."
        btnUpload.isEnabled = false
        btnSelectVideo.isEnabled = false

        // Get description
        val description = etDescription.text.toString().trim()

        // Upload in background thread using Coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update progress messages during upload
                withContext(Dispatchers.Main) {
                    tvProgress.text = "Connecting to server..."
                }
                
                // Simulate upload progress with much faster timing
                for (progress in 20..80 step 30) {
                    withContext(Dispatchers.Main) {
                        tvProgress.text = "Uploading $mediaTypeText... $progress%"
                    }
                    Thread.sleep(100) // Reduced from 500ms to 100ms for faster upload
                }

                val result = performUpload(selectedMediaFile, description, selectedMediaType)

                withContext(Dispatchers.Main) {
                    tvProgress.text = "Processing response..."
                    tvProgress.visibility = View.GONE
                    btnSelectVideo.isEnabled = true

                    if (result.first) {
                        // Show success message
                        Toast.makeText(this@Record_Video,
                            "Upload successful! Analyzing $mediaTypeText...", Toast.LENGTH_LONG).show()
                        
                        // Show completion dialog
                        showUploadCompletionDialog(mediaTypeText)
                        
                        // Navigate to Results with mock prediction response
                        navigateToResults(result.second)
                        resetUI()
                    } else {
                        Toast.makeText(this@Record_Video,
                            "Upload failed: ${result.second}", Toast.LENGTH_LONG).show()
                        btnUpload.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvProgress.visibility = View.GONE
                    btnSelectVideo.isEnabled = true
                    btnUpload.isEnabled = true
                    Toast.makeText(this@Record_Video,
                        "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performUpload(mediaFile: File, description: String, mediaType: MediaType): Pair<Boolean, String> {
        return try {
            // Create OkHttpClient with timeout
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            // Create multipart request body
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", // Backend expects 'file' field name
                    mediaFile.name,
                    mediaFile.asRequestBody((if (mediaType == MediaType.IMAGE) "image/*" else "video/*".toMediaTypeOrNull()) as okhttp3.MediaType?)
                )
                .addFormDataPart("description", description)
                .addFormDataPart("media_type", mediaType.name.lowercase())
                .addFormDataPart("upload_time", System.currentTimeMillis().toString())
                .build()

            // Create request - Point to local backend server
            val request = Request.Builder()
                .url("http://10.112.0.244:5000/predict") // Local backend server
                .post(requestBody)
                .addHeader("Content-Type", "multipart/form-data")
                .build()

            // Execute request
            client.newCall(request).execute().use { response ->
                Log.d("Upload", "Response code: ${response.code}")
                val responseBody = response.body?.string()
                Log.d("Upload", "Response body: $responseBody")
                
                if (response.isSuccessful && responseBody != null) {
                    // Return the actual server response
                    Pair(true, responseBody)
                } else {
                    Pair(false, "Server returned error: ${response.code} - ${responseBody ?: "No response body"}")
                }
            }
        } catch (e: Exception) {
            Log.e("Upload", "Upload failed: ${e.message}", e)
            Pair(false, "Network error: ${e.message}")
        }
    }

    private fun createMockPredictionResponse(filename: String, mediaType: MediaType): String {
        // Create a mock JSON response that matches the PredictionResponse data model
        val dominantEmotion = "happy"
        val confidence = if (mediaType == MediaType.IMAGE) 0.75f else 0.85f
        val emotionId = 1

        val probabilities = if (mediaType == MediaType.IMAGE) {
            mapOf(
                "happy" to 0.75f,
                "neutral" to 0.15f,
                "sad" to 0.05f,
                "angry" to 0.03f,
                "fearful" to 0.02f
            )
        } else {
            mapOf(
                "happy" to 0.85f,
                "neutral" to 0.10f,
                "sad" to 0.03f,
                "angry" to 0.01f,
                "fearful" to 0.01f
            )
        }

        val features = mapOf(
            "audio_shape" to listOf(1, 1),
            "video_shape" to if (mediaType == MediaType.VIDEO) listOf(30, 1) else emptyList(),
            "image_shape" to if (mediaType == MediaType.IMAGE) listOf(224, 224, 3) else emptyList()
        )

        // Build JSON manually to ensure proper formatting
        val json = """
        {
            "status": "success",
            "filename": "$filename",
            "prediction": {
                "emotion": "$dominantEmotion",
                "emotion_id": $emotionId,
                "confidence": $confidence
            },
            "probabilities": {
                "happy": ${probabilities["happy"]},
                "neutral": ${probabilities["neutral"]},
                "sad": ${probabilities["sad"]},
                "angry": ${probabilities["angry"]},
                "fearful": ${probabilities["fearful"]}
            },
            "features": {
                "audio_shape": ${features["audio_shape"]},
                "video_shape": ${features["video_shape"]},
                "image_shape": ${features["image_shape"]}
            }
        }
        """.trimIndent().replace("\n", "").replace(" ", "")

        return json
    }

    private fun navigateToResults(predictionResponseJson: String) {
        try {
            // Navigate to Results activity with the prediction response
            val intent = Intent(this, Results::class.java).apply {
                putExtra("prediction_response", predictionResponseJson)
                putExtra("video_path", selectedMediaFile.absolutePath)
                putExtra("is_new_recording", false)
                putExtra("media_type", selectedMediaType.name.lowercase())
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } catch (e: Exception) {
            Log.e("Navigation", "Failed to navigate to results: ${e.message}", e)
            Toast.makeText(this, "Error navigating to results", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUploadCompletionDialog(mediaType: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Upload Complete!")
        builder.setMessage("Your $mediaType has been successfully uploaded and is being analyzed for emotion detection.")
        builder.setPositiveButton("View Results") { dialog, _ ->
            dialog.dismiss()
            // Navigation will happen automatically after this dialog
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun resetUI() {
        tvSelectedFile.text = "No media selected"
        etDescription.text.clear()
        btnUpload.isEnabled = false
        selectedMediaType = MediaType.NONE
    }
}