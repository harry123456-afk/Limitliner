package com.example.limitliner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.limitliner.data.models.AppUsageStats
import com.example.limitliner.services.UsageTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val usageTrackingService = UsageTrackingService()

    private val _appUsageStats = MutableStateFlow<List<AppUsageStats>>(emptyList())
    val appUsageStats: StateFlow<List<AppUsageStats>> = _appUsageStats.asStateFlow()

    private val _selectedApp = MutableStateFlow<AppUsageStats?>(null)
    val selectedApp: StateFlow<AppUsageStats?> = _selectedApp.asStateFlow()

    init {
        refreshUsageStats()
    }

    fun refreshUsageStats() {
        viewModelScope.launch {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1)
            _appUsageStats.value = usageTrackingService.getAppUsageStats(startTime, endTime)
        }
    }

    fun selectApp(app: AppUsageStats) {
        _selectedApp.value = app
    }

    fun updateAppLimit(packageName: String, limitInMillis: Long) {
        // Implementation needed
    }

    fun enableAppTracking(packageName: String, enabled: Boolean) {
        // Implementation needed
    }
} 