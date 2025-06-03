package com.example.limitliner.ui.screens

import android.app.AppOpsManager
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PermissionsScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    var usagePermissionGranted by remember { mutableStateOf(false) }
    var overlayPermissionGranted by remember { mutableStateOf(false) }
    var accessibilityPermissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Check initial permission states
        val appOps = context.getSystemService(AppOpsManager::class.java)
        usagePermissionGranted = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        ) == AppOpsManager.MODE_ALLOWED

        overlayPermissionGranted = Settings.canDrawOverlays(context)
        accessibilityPermissionGranted = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )?.contains(context.packageName) == true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Required Permissions",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "LimitLiner needs the following permissions to help you manage your screen time effectively",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        PermissionItem(
            title = "Usage Access",
            description = "To track app usage time",
            icon = Icons.Outlined.Timer,
            isGranted = usagePermissionGranted,
            onRequest = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        )

        PermissionItem(
            title = "Display Over Other Apps",
            description = "To show reminders and block apps",
            icon = Icons.Outlined.Layers,
            isGranted = overlayPermissionGranted,
            onRequest = {
                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
            }
        )

        PermissionItem(
            title = "Accessibility Service",
            description = "To help manage app blocking",
            icon = Icons.Outlined.AccessibilityNew,
            isGranted = accessibilityPermissionGranted,
            onRequest = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onAllPermissionsGranted,
            enabled = usagePermissionGranted && overlayPermissionGranted && accessibilityPermissionGranted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isGranted) {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Grant")
                }
            } else {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 