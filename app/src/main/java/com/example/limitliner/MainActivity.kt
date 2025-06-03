package com.example.limitliner

import android.app.AppOpsManager
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.limitliner.navigation.Screen
import com.example.limitliner.services.UsageTrackingService
import com.example.limitliner.ui.screens.*
import com.example.limitliner.ui.theme.LimitLinerTheme
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.example.limitliner.data.DataStoreManager

class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 123
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "Starting MainActivity onCreate")
            
            // Check and request permissions before starting the service
            if (checkAndRequestPermissions()) {
                Log.d(TAG, "All permissions granted, starting service")
                startUsageTrackingService()
            } else {
                Log.d(TAG, "Permissions not granted yet")
            }

            setContent {
                val dataStore = remember { DataStoreManager(this) }
                var darkTheme by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    dataStore.isDarkTheme.collect { isDark ->
                        darkTheme = isDark
                    }
                }

                LimitLinerTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        var hasUsagePermission by remember { mutableStateOf(checkUsageStatsPermission()) }

                        LaunchedEffect(hasUsagePermission) {
                            if (!hasUsagePermission) {
                                Log.d(TAG, "Usage permission not granted, navigating to permissions screen")
                                navController.navigate(Screen.Permissions.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = if (hasUsagePermission) Screen.Dashboard.route else Screen.Permissions.route
                        ) {
                            composable(Screen.Onboarding.route) {
                                OnboardingScreen(
                                    onComplete = { navController.navigate(Screen.Permissions.route) },
                                    onAgeVerified = { /* Save age */ }
                                )
                            }

                            composable(Screen.Permissions.route) {
                                PermissionsScreen(
                                    onAllPermissionsGranted = {
                                        hasUsagePermission = true
                                        startUsageTrackingService()
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(Screen.Dashboard.route) {
                                DashboardScreen(
                                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                                    onNavigateToLimits = { navController.navigate(Screen.UsageLimits.route) },
                                    onNavigateToBlocking = { navController.navigate(Screen.InAppBlocking.route) },
                                    onNavigateToFocusTimer = { navController.navigate(Screen.FocusTimer.route) }
                                )
                            }

                            composable(Screen.Settings.route) {
                                SettingsScreen(onNavigateBack = { navController.popBackStack() })
                            }

                            composable(Screen.UsageLimits.route) {
                                UsageLimitsScreen(onNavigateBack = { navController.popBackStack() })
                            }

                            composable(Screen.InAppBlocking.route) {
                                InAppBlockingScreen(onNavigateBack = { navController.popBackStack() })
                            }

                            composable(Screen.FocusTimer.route) {
                                FocusTimerScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "An error occurred while starting the app", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        try {
            Log.d(TAG, "Checking permissions")
            val permissions = mutableListOf<String>()
            
            // Check notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Check overlay permission
            if (!Settings.canDrawOverlays(this)) {
                Log.d(TAG, "Overlay permission not granted")
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching overlay permission settings", e)
                    Toast.makeText(this, "Please enable overlay permission manually", Toast.LENGTH_LONG).show()
                }
                return false
            }

            // Request other permissions if needed
            if (permissions.isNotEmpty()) {
                Log.d(TAG, "Requesting permissions: $permissions")
                ActivityCompat.requestPermissions(
                    this,
                    permissions.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
                return false
            }

            // Check usage stats permission
            if (!checkUsageStatsPermission()) {
                Log.d(TAG, "Usage stats permission not granted")
                try {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching usage access settings", e)
                    Toast.makeText(this, "Please enable usage access manually", Toast.LENGTH_LONG).show()
                }
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            return false
        }
    }

    private fun startUsageTrackingService() {
        try {
            if (checkUsageStatsPermission()) {
                Log.d(TAG, "Starting UsageTrackingService")
                val serviceIntent = Intent(this, UsageTrackingService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            } else {
                Log.d(TAG, "Cannot start service - usage stats permission not granted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service", e)
            Toast.makeText(this, "Error starting usage tracking", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        try {
            val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking usage stats permission", e)
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(TAG, "All permissions granted")
                    startUsageTrackingService()
                } else {
                    Log.d(TAG, "Some permissions were denied")
                    Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in permission result", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (Settings.canDrawOverlays(this)) {
                    Log.d(TAG, "Overlay permission granted")
                    startUsageTrackingService()
                } else {
                    Log.d(TAG, "Overlay permission denied")
                    Toast.makeText(this, "Overlay permission required for app blocking", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in activity result", e)
        }
    }
}