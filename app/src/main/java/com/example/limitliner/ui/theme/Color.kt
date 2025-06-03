package com.example.limitliner.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Colors
val Primary = Color(0xFF4CAF50) // Green
val Secondary = Color(0xFF2196F3) // Blue
val Alert = Color(0xFFF44336) // Red

// Light Theme Colors
val Background = Color(0xFFF5F5F5) // Light gray
val Surface = Color.White
val OnPrimary = Color.White
val OnSecondary = Color.White
val OnBackground = Color(0xFF121212)
val OnSurface = Color(0xFF121212)

// Dark Theme Colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val OnDarkBackground = Color(0xFFE1E1E1)
val OnDarkSurface = Color(0xFFE1E1E1)

// Progress Colors
val ProgressGreen = Primary
val ProgressOrange = Color(0xFFFF9800)
val ProgressRed = Alert

// Container Colors
val PrimaryContainer = Primary.copy(alpha = 0.12f)
val SecondaryContainer = Secondary.copy(alpha = 0.12f)