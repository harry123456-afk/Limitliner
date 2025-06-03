package com.example.limitliner.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView
import com.example.limitliner.R
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager.LayoutParams

class AppBlockerAccessibilityService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var blockingView: View? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.packageName?.toString()?.let { packageName ->
            if (shouldBlockApp(packageName)) {
                showBlockingOverlay(packageName)
                provideHapticFeedback()
            }
        }
    }

    private fun shouldBlockApp(packageName: String): Boolean {
        // TODO: Implement checking if app should be blocked based on usage stats
        // For now, returning false to prevent blocking
        return false
    }

    private fun showBlockingOverlay(packageName: String) {
        if (blockingView != null) return

        val inflater = LayoutInflater.from(this)
        blockingView = inflater.inflate(R.layout.overlay_app_blocked, null)

        val params = WindowManager.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                LayoutParams.TYPE_PHONE,
            LayoutParams.FLAG_NOT_FOCUSABLE or
                    LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        blockingView?.let { view ->
            // Set up the blocking view
            view.findViewById<TextView>(R.id.messageText)?.text =
                "You've reached your daily limit for this app"

            view.findViewById<Button>(R.id.closeButton)?.setOnClickListener {
                removeBlockingOverlay()
                // Return to home screen
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(homeIntent)
            }

            try {
                windowManager?.addView(view, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun removeBlockingOverlay() {
        blockingView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            blockingView = null
        }
    }

    private fun provideHapticFeedback() {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(200)
            }
        }
    }

    override fun onInterrupt() {
        removeBlockingOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeBlockingOverlay()
    }
} 