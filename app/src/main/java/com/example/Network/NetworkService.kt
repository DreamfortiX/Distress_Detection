package com.example.Network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.concurrent.TimeUnit

// Data classes for API response
@Parcelize
data class PredictionResponse(
    val status: String,
    val filename: String,
    val prediction: Prediction,
    val probabilities: Map<String, Float>,
    val features: Features? = null
) : Parcelable

@Parcelize
data class Prediction(
    val emotion: String,
    val emotion_id: Int,
    val confidence: Float
) : Parcelable

@Parcelize
data class Features(
    val audio_shape: List<Int>,
    val video_shape: List<Int>,
    val image_shape: List<Int>
) : Parcelable

data class ApiError(
    val error: String? = null,
    val message: String? = null,
    val status: String? = null
)

// Retrofit service interface
interface EmotionRecognitionApi {
    @Multipart
    @POST("predict")
    suspend fun predictEmotion(
        @Part file: MultipartBody.Part
    ): Response<PredictionResponse>
}

// Singleton object for Retrofit instance
object RetrofitClient {
    private const val BASE_URL = "http://10.112.0.244:5000/" // Change to your server IP
    // For emulator use: "http://10.0.2.2:5000/"
    // For Genymotion use: "http://10.0.3.2:5000/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Increased for video upload
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: EmotionRecognitionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmotionRecognitionApi::class.java)
    }
}

// Helper class for API calls
class EmotionDetectionRepository {
    private val apiService = RetrofitClient.instance

    suspend fun uploadVideo(videoFile: java.io.File): Result<PredictionResponse> {
        return try {
            // Create request body
            val requestFile = RequestBody.create(
                "video/*".toMediaTypeOrNull(),
                videoFile
            )

            // Create multipart part
            val videoPart = MultipartBody.Part.createFormData(
                "file",
                videoFile.name,
                requestFile
            )

            // Make API call
            val response = apiService.predictEmotion(videoPart)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}