package com.example.Utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("emotion_detection_prefs", Context.MODE_PRIVATE)

    // User data
    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String? = sharedPreferences.getString("user_name", null)

    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("user_email", email).apply()
    }

    fun getUserEmail(): String? = sharedPreferences.getString("user_email", null)

    // App settings
    fun isFirstLaunch(): Boolean =
        sharedPreferences.getBoolean("first_launch", true)

    fun setFirstLaunch(value: Boolean) {
        sharedPreferences.edit().putBoolean("first_launch", value).apply()
    }

    fun hasUnreadNotifications(): Boolean =
        sharedPreferences.getBoolean("has_unread_notifications", false)

    fun setUnreadNotifications(value: Boolean) {
        sharedPreferences.edit().putBoolean("has_unread_notifications", value).apply()
    }

    // Analysis settings
    fun getVideoDuration(): Int =
        sharedPreferences.getInt("video_duration", 10) // Default 10 seconds

    fun setVideoDuration(seconds: Int) {
        sharedPreferences.edit().putInt("video_duration", seconds).apply()
    }

    fun isAutoSaveEnabled(): Boolean =
        sharedPreferences.getBoolean("auto_save", true)

    fun setAutoSaveEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("auto_save", enabled).apply()
    }

    // Clear all preferences
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}