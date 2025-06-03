package com.example.limitliner.data.models

import android.graphics.drawable.Drawable
import java.util.concurrent.TimeUnit

data class AppUsageStats(
    val packageName: String,
    val appName: String,
    val usageTimeInMinutes: Int,
    val usageTimeInMillis: Long,
    val lastTimeUsed: Long,
    val isSystemApp: Boolean,
    val appIcon: Drawable? = null,
    val dailyLimit: Long = 120, // Default 2 hours in minutes
    val timeUsedToday: Long = 0
) {
    val usageTimeFormatted: String
        get() = when {
            usageTimeInMinutes >= 60 -> "${usageTimeInMinutes / 60}h ${usageTimeInMinutes % 60}m"
            else -> "${usageTimeInMinutes}m"
        }

    val progressPercentage: Float
        get() = (timeUsedToday.toFloat() / dailyLimit).coerceIn(0f, 1f)

    val isOverLimit: Boolean
        get() = timeUsedToday >= dailyLimit

    val remainingTime: Long
        get() = maxOf(0, dailyLimit - timeUsedToday)

    fun formatTimeUsed(): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeUsedToday)
        return when {
            minutes >= 60 -> "${minutes / 60}h ${minutes % 60}m"
            else -> "${minutes}m"
        }
    }

    fun formatRemainingTime(): String {
        val remaining = remainingTime
        val hours = TimeUnit.MILLISECONDS.toHours(remaining)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
        return String.format("%02d:%02d", hours, minutes)
    }
} 