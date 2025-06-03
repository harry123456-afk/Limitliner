package com.example.limitliner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.limitliner.data.PreferencesManager
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import com.example.limitliner.utils.FeedbackManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferencesManager(context) }
    val feedbackManager = remember { FeedbackManager(context) }
    
    var isRunning by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(25 * 60) } // 25 minutes in seconds
    var showDialog by remember { mutableStateOf(false) }
    
    val motivationalQuotes = listOf(
        "Great job! Keep up the momentum!",
        "You're making progress! Take a well-deserved break.",
        "Success is built one focused session at a time!",
        "Well done! Your dedication is inspiring.",
        "You're one step closer to your goals!"
    )
    
    LaunchedEffect(isRunning) {
        while (isRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
            
            if (timeLeft == 0) {
                showDialog = true
                if (prefs.isSoundEnabled()) {
                    feedbackManager.playNotificationSound()
                }
                if (prefs.isHapticFeedbackEnabled()) {
                    feedbackManager.triggerHapticFeedback()
                }
            }
        }
    }

    // Cleanup when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            feedbackManager.cleanup()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Timer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Timer Display
            Text(
                text = "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 72.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Start/Stop Button
            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier.size(width = 200.dp, height = 56.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Stop" else "Start"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRunning) "Stop" else "Start Focus Time")
            }
            
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Focus Session Complete!") },
                    text = { 
                        Text(
                            text = motivationalQuotes.random(),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                timeLeft = 25 * 60
                                isRunning = true
                                showDialog = false
                            }
                        ) {
                            Text("Start New Session")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                timeLeft = 25 * 60
                                isRunning = false
                                showDialog = false
                            }
                        ) {
                            Text("Take a Break")
                        }
                    }
                )
            }
        }
    }
} 