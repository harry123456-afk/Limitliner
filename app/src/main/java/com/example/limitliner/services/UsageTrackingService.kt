package com.example.limitliner.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.app.usage.UsageStatsManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.limitliner.MainActivity
import com.example.limitliner.R
import com.example.limitliner.data.models.AppUsageStats
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit
import android.app.usage.UsageEvents
import android.content.pm.ApplicationInfo
import android.util.Log

class UsageTrackingService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var packageManager: PackageManager
    private var trackingJob: Job? = null
    private var isServiceRunning = false
    private var context: Context? = null
    private var lastError: Long = 0 // To prevent log spam
    private val errorCooldown = 5000L // 5 seconds between same error logs

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "usage_tracking_channel"
        private const val NOTIFICATION_ID = 1
        private const val TRACKING_INTERVAL = 1000L * 60 // 1 minute
        private const val TAG = "UsageTrackingService"
    }

    fun attachContext(context: Context) {
        this.context = context
        try {
            Log.d(TAG, "Attaching context to service")
            usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            packageManager = context.packageManager
            Log.d(TAG, "Context attached successfully")
        } catch (e: Exception) {
            logError("Error initializing service with context", e)
        }
    }

    private fun logError(message: String, e: Exception, force: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (force || currentTime - lastError > errorCooldown) {
            Log.e(TAG, "$message: ${e.message}")
            Log.e(TAG, "Stack trace: ", e)
            lastError = currentTime
        }
    }

    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }

    override fun onCreate() {
        super.onCreate()
        try {
            logDebug("Creating UsageTrackingService")
            attachContext(this)
            createNotificationChannel()
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
            isServiceRunning = true
            startTracking()
            logDebug("UsageTrackingService created successfully")
        } catch (e: Exception) {
            logError("Error creating service", e, true)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logDebug("Service onStartCommand called with startId: $startId")
        if (!isServiceRunning) {
            try {
                startForeground(NOTIFICATION_ID, createNotification())
                isServiceRunning = true
                startTracking()
                logDebug("Service started successfully")
            } catch (e: Exception) {
                logError("Error in onStartCommand", e, true)
                stopSelf()
            }
        } else {
            logDebug("Service already running, ignoring start command")
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        logDebug("Starting usage tracking")
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            try {
                while (isActive && isServiceRunning) {
                    logDebug("Running usage check cycle")
                    checkAppUsage()
                    delay(TRACKING_INTERVAL)
                }
            } catch (e: Exception) {
                logError("Error in tracking job", e)
                // Attempt to restart tracking if it fails
                if (isServiceRunning) {
                    logDebug("Attempting to restart tracking after error")
                    delay(1000) // Wait a second before retrying
                    startTracking()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Usage Tracking",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Tracks app usage time"
                    setShowBadge(false)
                }
                
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                logError("Error creating notification channel", e)
            }
        }
    }

    private fun createNotification(): Notification {
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            pendingIntentFlags
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("LimitLiner Active")
            .setContentText("Monitoring app usage...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun checkAppUsage() {
        try {
            val endTime = System.currentTimeMillis()
            val startTime = getStartOfDay()
            val stats = getAppUsageStats(startTime, endTime)

            stats.filter { it.usageTimeInMinutes > 0 }.forEach { appStats ->
                if (appStats.isOverLimit) {
                    notifyLimitExceeded(appStats)
                }
            }
        } catch (e: Exception) {
            logError("Error checking app usage", e)
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun notifyLimitExceeded(appStats: AppUsageStats) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Time Limit Exceeded")
            .setContentText("You've exceeded your daily limit for ${appStats.appName}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(appStats.packageName.hashCode(), notification)
    }

    private fun ensureInitialized() {
        if (!::usageStatsManager.isInitialized || !::packageManager.isInitialized) {
            logDebug("Service not initialized, attempting to initialize")
            context?.let { 
                attachContext(it)
                logDebug("Service initialized successfully")
            } ?: run {
                val error = IllegalStateException("Service not properly initialized - context is null")
                logError("Initialization failed", error, true)
                throw error
            }
        }
    }

    fun getAppUsageStats(startTime: Long, endTime: Long): List<AppUsageStats> {
        ensureInitialized()
        try {
            logDebug("Getting app usage stats from ${Date(startTime)} to ${Date(endTime)}")
            val events = usageStatsManager.queryEvents(startTime, endTime)
            if (events == null) {
                logError("Usage events query returned null", Exception("Null events"), true)
                return emptyList()
            }

            val usageMap = mutableMapOf<String, MutableList<Pair<Long, Long>>>()
            val event = UsageEvents.Event()
            var eventCount = 0

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                eventCount++
                processUsageEvent(event, usageMap)
            }

            logDebug("Processed $eventCount usage events")
            return processUsageMap(usageMap)
        } catch (e: Exception) {
            logError("Error getting app usage stats", e)
            return emptyList()
        }
    }

    private fun processUsageEvent(event: UsageEvents.Event, usageMap: MutableMap<String, MutableList<Pair<Long, Long>>>) {
        try {
            val packageName = event.packageName
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    usageMap.getOrPut(packageName) { mutableListOf() }
                        .add(Pair(event.timeStamp, -1))
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    usageMap[packageName]?.let { sessions ->
                        if (sessions.isNotEmpty() && sessions.last().second == -1L) {
                            sessions[sessions.lastIndex] = Pair(sessions.last().first, event.timeStamp)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logError("Error processing usage event", e)
        }
    }

    private fun processUsageMap(usageMap: Map<String, List<Pair<Long, Long>>>): List<AppUsageStats> {
        return usageMap.mapNotNull { (packageName, sessions) ->
            try {
                val totalTime = calculateTotalTime(sessions)
                createAppUsageStats(packageName, sessions, totalTime)
            } catch (e: Exception) {
                logError("Error processing stats for $packageName", e)
                null
            }
        }.filter { 
            !it.isSystemApp && it.usageTimeInMinutes > 0
        }.sortedByDescending { it.usageTimeInMinutes }
    }

    private fun calculateTotalTime(sessions: List<Pair<Long, Long>>): Long {
        return sessions.sumOf { session ->
            if (session.second == -1L) {
                System.currentTimeMillis() - session.first
            } else {
                session.second - session.first
            }
        }
    }

    private fun createAppUsageStats(packageName: String, sessions: List<Pair<Long, Long>>, totalTime: Long): AppUsageStats? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            AppUsageStats(
                packageName = packageName,
                appName = appInfo.loadLabel(packageManager).toString(),
                usageTimeInMinutes = TimeUnit.MILLISECONDS.toMinutes(totalTime).toInt(),
                usageTimeInMillis = totalTime,
                lastTimeUsed = sessions.lastOrNull()?.second ?: sessions.lastOrNull()?.first ?: 0L,
                isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
                appIcon = packageManager.getApplicationIcon(appInfo),
                timeUsedToday = totalTime
            )
        } catch (e: PackageManager.NameNotFoundException) {
            logError("Package not found: $packageName", e)
            null
        }
    }

    fun getDailyUsageStats(): List<AppUsageStats> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)
        return getAppUsageStats(startTime, endTime)
    }

    fun getWeeklyUsageStats(): List<AppUsageStats> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(7)
        return getAppUsageStats(startTime, endTime)
    }

    fun getMonthlyUsageStats(): List<AppUsageStats> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(30)
        return getAppUsageStats(startTime, endTime)
    }

    fun getTotalUsageTime(startTime: Long, endTime: Long): Int {
        ensureInitialized()
        return try {
            getAppUsageStats(startTime, endTime)
                .sumOf { it.usageTimeInMinutes }
        } catch (e: Exception) {
            logError("Error getting total usage time", e)
            0
        }
    }

    fun getHourlyUsageData(startTime: Long, endTime: Long): List<Int> {
        ensureInitialized()
        try {
            val hourlyUsage = MutableList(24) { 0 }
            val events = usageStatsManager.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()
            var lastEventTime = 0L
            var lastEventType = -1

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || 
                    event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                    
                    if (lastEventTime > 0 && lastEventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        val duration = event.timeStamp - lastEventTime
                        val hour = (lastEventTime / 3600000L % 24).toInt()
                        hourlyUsage[hour] += TimeUnit.MILLISECONDS.toMinutes(duration).toInt()
                    }

                    lastEventTime = event.timeStamp
                    lastEventType = event.eventType
                }
            }

            return hourlyUsage
        } catch (e: Exception) {
            logError("Error getting hourly usage data", e)
            return List(24) { 0 }
        }
    }

    override fun onDestroy() {
        logDebug("Service being destroyed")
        isServiceRunning = false
        trackingJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
} 