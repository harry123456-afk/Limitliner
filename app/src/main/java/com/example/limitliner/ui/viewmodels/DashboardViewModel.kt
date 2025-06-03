package com.example.limitliner.ui.viewmodels

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.limitliner.data.DataStoreManager
import com.example.limitliner.data.models.AppUsageStats
import com.example.limitliner.services.UsageTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.Date

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val dataStoreManager = DataStoreManager(context)
    private val usageTrackingService = UsageTrackingService().apply {
        attachContext(context)
    }

    private val _usageStats = MutableStateFlow<List<AppUsageStats>>(emptyList())
    val usageStats: StateFlow<List<AppUsageStats>> = _usageStats.asStateFlow()

    private val _totalUsage = MutableStateFlow(0)
    val totalUsage: StateFlow<Int> = _totalUsage.asStateFlow()

    private val _hourlyUsage = MutableStateFlow<List<Int>>(List(24) { 0 })
    val hourlyUsage: StateFlow<List<Int>> = _hourlyUsage.asStateFlow()

    private val _dailyLimit = MutableStateFlow(300) // Default 5 hours
    val dailyLimit: StateFlow<Int> = _dailyLimit.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val TAG = "DashboardViewModel"

    init {
        logDebug("Initializing DashboardViewModel")
        viewModelScope.launch {
            try {
                logDebug("Starting usage limit collection")
                dataStoreManager.usageLimitHours.collect { hours ->
                    logDebug("Received new usage limit: $hours hours")
                    _dailyLimit.value = (hours * 60).toInt()
                }
            } catch (e: Exception) {
                logError("Error collecting usage limit hours", e)
                _error.value = "Failed to load usage limits: ${e.message}"
            }
        }
        refreshUsageStats(TimeRange.TODAY)
    }

    fun refreshUsageStats(timeRange: TimeRange) {
        viewModelScope.launch {
            try {
                val endTime = System.currentTimeMillis()
                val startTime = when (timeRange) {
                    TimeRange.TODAY -> endTime - TimeUnit.DAYS.toMillis(1)
                    TimeRange.WEEK -> endTime - TimeUnit.DAYS.toMillis(7)
                    TimeRange.MONTH -> endTime - TimeUnit.DAYS.toMillis(30)
                }

                logDebug("Refreshing usage stats from ${Date(startTime)} to ${Date(endTime)}")

                val stats = usageTrackingService.getAppUsageStats(startTime, endTime)
                _usageStats.value = stats
                logDebug("Retrieved ${stats.size} app usage records")

                val total = usageTrackingService.getTotalUsageTime(startTime, endTime)
                _totalUsage.value = total
                logDebug("Total usage time: $total minutes")

                val hourly = usageTrackingService.getHourlyUsageData(startTime, endTime)
                _hourlyUsage.value = hourly
                logDebug("Hourly usage data updated")

                _error.value = null
            } catch (e: Exception) {
                logError("Error refreshing usage stats", e)
                _error.value = "Failed to refresh usage stats: ${e.message}"
                // Set default values in case of error
                _usageStats.value = emptyList()
                _totalUsage.value = 0
                _hourlyUsage.value = List(24) { 0 }
            }
        }
    }

    private fun logError(message: String, e: Exception) {
        Log.e(TAG, "$message: ${e.message}")
        Log.e(TAG, "Stack trace: ", e)
    }

    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }

    enum class TimeRange {
        TODAY, WEEK, MONTH
    }

    override fun onCleared() {
        logDebug("ViewModel being cleared")
        super.onCleared()
    }
} 