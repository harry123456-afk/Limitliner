package com.example.limitliner.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

private val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = "limitliner_settings")

class DataStoreManager(private val context: Context) {
    companion object {
        private val BIRTH_YEAR = intPreferencesKey("birth_year")
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val USAGE_LIMIT_HOURS = floatPreferencesKey("usage_limit_hours")
        private val LAST_USAGE_UPDATE = longPreferencesKey("last_usage_update")
    }

    suspend fun saveBirthYear(year: Int) {
        context.dataStore.edit { preferences ->
            preferences[BIRTH_YEAR] = year
        }
    }

    val birthYear: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[BIRTH_YEAR]
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME] = enabled
        }
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME] ?: false
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setUsageLimitHours(hours: Float) {
        context.dataStore.edit { preferences ->
            preferences[USAGE_LIMIT_HOURS] = hours
        }
    }

    val usageLimitHours: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[USAGE_LIMIT_HOURS] ?: 2f
    }

    suspend fun updateLastUsageTime() {
        context.dataStore.edit { preferences ->
            preferences[LAST_USAGE_UPDATE] = System.currentTimeMillis()
        }
    }

    val lastUsageUpdate: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_USAGE_UPDATE] ?: 0L
    }
} 