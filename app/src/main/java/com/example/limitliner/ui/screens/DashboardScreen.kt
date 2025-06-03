package com.example.limitliner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.limitliner.ui.components.AppUsageItem
import com.example.limitliner.ui.components.UsageGraph
import com.example.limitliner.ui.components.UsageRing
import com.example.limitliner.ui.viewmodels.DashboardViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLimits: () -> Unit,
    onNavigateToBlocking: () -> Unit,
    onNavigateToFocusTimer: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Today", "Week", "Month")
    
    val usageStats by viewModel.usageStats.collectAsState()
    val totalUsage by viewModel.totalUsage.collectAsState()
    val hourlyUsage by viewModel.hourlyUsage.collectAsState()
    val dailyLimit by viewModel.dailyLimit.collectAsState()

    // Update stats when tab changes
    LaunchedEffect(selectedTab) {
        viewModel.refreshUsageStats(
            when (selectedTab) {
                0 -> DashboardViewModel.TimeRange.TODAY
                1 -> DashboardViewModel.TimeRange.WEEK
                else -> DashboardViewModel.TimeRange.MONTH
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LimitLiner") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.QueryStats, "Usage") },
                    label = { Text("My Usage") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Timer, "Limits") },
                    label = { Text("Usage Limits") },
                    selected = false,
                    onClick = onNavigateToLimits
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Block, "Blocking") },
                    label = { Text("App Blocking") },
                    selected = false,
                    onClick = onNavigateToBlocking
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            // Current time and date
            item {
                val currentTime = LocalDateTime.now()
                Text(
                    text = currentTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // Usage ring
            item {
                UsageRing(
                    totalUsage = totalUsage,
                    dailyLimit = dailyLimit,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                )
            }

            // Usage graph
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(200.dp)
                ) {
                    UsageGraph(
                        usageData = hourlyUsage,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }

            // Focus Timer Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToFocusTimer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Focus Timer",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Stay focused with Pomodoro technique",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Focus Timer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Most used apps section
            item {
                Text(
                    text = "Most Used Apps",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // App list
            items(usageStats) { appStats ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = appStats.appName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Used: ${appStats.usageTimeFormatted}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = appStats.usageTimeInMillis.toFloat() / (dailyLimit * 3600000f),
                            modifier = Modifier
                                .width(100.dp)
                                .padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
} 