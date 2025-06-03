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
import com.example.limitliner.data.DataStoreManager
import kotlinx.coroutines.launch
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageLimitsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { DataStoreManager(context) }
    
    var dailyLimitHours by remember { mutableFloatStateOf(5f) }
    var customHours by remember { mutableStateOf("5") }
    val usageStats by viewModel.usageStats.collectAsState()

    LaunchedEffect(Unit) {
        dataStore.usageLimitHours.collect { hours ->
            dailyLimitHours = hours
            customHours = hours.toInt().toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usage Limits") },
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
            // Daily Limit Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Daily Usage Limit",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Custom time input
                        OutlinedTextField(
                            value = customHours,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.toFloatOrNull() != null) {
                                    customHours = value
                                    value.toFloatOrNull()?.let { hours ->
                                        if (hours in 1f..24f) {
                                            dailyLimitHours = hours
                                            scope.launch {
                                                dataStore.setUsageLimitHours(hours)
                                            }
                                        }
                                    }
                                }
                            },
                            label = { Text("Hours (1-24)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Slider(
                            value = dailyLimitHours,
                            onValueChange = { 
                                dailyLimitHours = it
                                customHours = it.toInt().toString()
                                scope.launch {
                                    dataStore.setUsageLimitHours(it)
                                }
                            },
                            valueRange = 1f..24f,
                            steps = 23,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // App-specific limits section
            item {
                Text(
                    text = "App Usage Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(usageStats) { appStats ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
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
                                    text = "Used: ${appStats.usageTimeFormatted}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Switch(
                                checked = appStats.isOverLimit,
                                onCheckedChange = { /* TODO: Implement app-specific limit toggle */ }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Usage Bar Chart
                        val usageRatio = (appStats.usageTimeInMillis / (dailyLimitHours * 3600000f))
                            .coerceIn(0f, 1f)
                        
                        LinearProgressIndicator(
                            progress = usageRatio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .padding(vertical = 4.dp),
                            color = when {
                                usageRatio > 0.9f -> MaterialTheme.colorScheme.error
                                usageRatio > 0.7f -> Color(0xFFFF9800) // Orange
                                else -> Color(0xFF4CAF50) // Green
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
} 