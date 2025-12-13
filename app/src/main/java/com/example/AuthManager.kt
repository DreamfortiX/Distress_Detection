package com.example

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun signUp(name: String, email: String, password: String) {
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
        // Auto-login after signup
        saveSession(email)
    }

    fun login(email: String, password: String): Boolean {
        val storedEmail = prefs.getString(KEY_EMAIL, null)
        val storedPassword = prefs.getString(KEY_PASSWORD, null)
        // If we have stored credentials, validate them. Otherwise accept first login as session.
        val valid = if (storedEmail != null && storedPassword != null) {
            email.equals(storedEmail, ignoreCase = true) && password == storedPassword
        } else {
            email.isNotBlank() && password.isNotBlank()
        }
        if (valid) saveSession(email)
        return valid
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_SESSION_EMAIL)
            .apply()
    }

    fun getUserName(): String? = prefs.getString(KEY_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_SESSION_EMAIL, prefs.getString(KEY_EMAIL, null))

    private fun saveSession(email: String) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_SESSION_EMAIL, email)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_NAME = "user_name"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_PASSWORD = "user_password"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_SESSION_EMAIL = "session_email"
    }
}
