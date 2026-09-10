package com.example.onetomany.data.security

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("one_to_many_session_vault", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACTIVE_USER_ID = "active_user_id"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_DISPLAY_NAME = "user_display_name"
        private const val KEY_ONLINE_DB_ENDPOINT = "online_db_endpoint"
        private const val KEY_ONLINE_DB_SYNC_ENABLED = "online_db_sync_enabled"

        // Default public REST cloud database sync endpoint or fallback
        const val DEFAULT_ONLINE_DB_ENDPOINT = "https://firestore.googleapis.com/v1"
    }

    fun saveSession(userId: String, email: String, displayName: String, token: String) {
        prefs.edit()
            .putString(KEY_ACTIVE_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_DISPLAY_NAME, displayName)
            .putString(KEY_SESSION_TOKEN, token)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACTIVE_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_DISPLAY_NAME)
            .remove(KEY_SESSION_TOKEN)
            .apply()
    }

    fun getActiveUserId(): String? = prefs.getString(KEY_ACTIVE_USER_ID, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserDisplayName(): String? = prefs.getString(KEY_USER_DISPLAY_NAME, null)
    fun getSessionToken(): String? = prefs.getString(KEY_SESSION_TOKEN, null)

    fun isOnlineSyncEnabled(): Boolean = prefs.getBoolean(KEY_ONLINE_DB_SYNC_ENABLED, true)
    fun setOnlineSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ONLINE_DB_SYNC_ENABLED, enabled).apply()
    }

    fun getOnlineDbEndpoint(): String =
        prefs.getString(KEY_ONLINE_DB_ENDPOINT, DEFAULT_ONLINE_DB_ENDPOINT) ?: DEFAULT_ONLINE_DB_ENDPOINT

    fun setOnlineDbEndpoint(endpoint: String) {
        prefs.edit().putString(KEY_ONLINE_DB_ENDPOINT, endpoint).apply()
    }
}
