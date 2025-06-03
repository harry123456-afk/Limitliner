package com.example.limitliner.data

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "limitliner_preferences"
        private const val PREFIX_APP_LIMIT = "app_limit_"
        private const val PREFIX_APP_ENABLED = "app_enabled_"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_STRICT_MODE = "strict_mode"
        
        // Default values
        private const val DEFAULT_APP_LIMIT = 7200000L // 2 hours in milliseconds
    }

    fun setAppLimit(packageName: String, limitInMillis: Long) {
        prefs.edit().putLong(PREFIX_APP_LIMIT + packageName, limitInMillis).apply()
    }

    fun getAppLimit(packageName: String): Long {
        return prefs.getLong(PREFIX_APP_LIMIT + packageName, DEFAULT_APP_LIMIT)
    }

    fun setAppEnabled(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean(PREFIX_APP_ENABLED + packageName, enabled).apply()
    }

    fun isAppEnabled(packageName: String): Boolean {
        return prefs.getBoolean(PREFIX_APP_ENABLED + packageName, false)
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    fun isHapticFeedbackEnabled(): Boolean {
        return prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setStrictMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STRICT_MODE, enabled).apply()
    }

    fun isStrictMode(): Boolean {
        return prefs.getBoolean(KEY_STRICT_MODE, false)
    }

    fun clearAllSettings() {
        prefs.edit().clear().apply()
    }

    // Utility methods
    fun convertHoursToMillis(hours: Int): Long {
        return TimeUnit.HOURS.toMillis(hours.toLong())
    }

    fun convertMinutesToMillis(minutes: Int): Long {
        return TimeUnit.MINUTES.toMillis(minutes.toLong())
    }

    fun convertMillisToHours(millis: Long): Int {
        return TimeUnit.MILLISECONDS.toHours(millis).toInt()
    }

    fun convertMillisToMinutes(millis: Long): Int {
        return TimeUnit.MILLISECONDS.toMinutes(millis).toInt()
    }
} 