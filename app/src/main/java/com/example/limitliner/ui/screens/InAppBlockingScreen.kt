package com.example.limitliner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.limitliner.ui.viewmodels.DashboardViewModel
import com.example.limitliner.data.PreferencesManager
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppBlockingScreen(
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    val usageStats by viewModel.usageStats.collectAsState()
    var strictMode by remember { mutableStateOf(prefs.isStrictMode()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Blocking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Strict Mode Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Strict Mode",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Prevent disabling app blocking",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = strictMode,
                                onCheckedChange = { enabled ->
                                    strictMode = enabled
                                    prefs.setStrictMode(enabled)
                                }
                            )
                        }
                    }
                }
            }

            // Blocking Settings Section
            item {
                Text(
                    text = "App Blocking Settings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(usageStats) { appStats ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        appStats.appIcon?.let { icon ->
                            Icon(
                                painter = rememberDrawablePainter(icon),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = appStats.appName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (prefs.isAppEnabled(appStats.packageName)) 
                                    "Blocking enabled" else "Not blocked",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = prefs.isAppEnabled(appStats.packageName),
                            onCheckedChange = { enabled ->
                                prefs.setAppEnabled(appStats.packageName, enabled)
                            }
                        )
                    }
                }
            }

            // Information Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "How it works",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "When an app is blocked, you'll see a reminder when you try to open it. " +
                                  "In strict mode, you won't be able to disable blocking until the next day.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
} 