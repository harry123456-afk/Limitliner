package com.example.limitliner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun UsageRing(
    totalUsage: Int, // in minutes
    dailyLimit: Int, // in minutes
    modifier: Modifier = Modifier
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val headlineMedium = MaterialTheme.typography.headlineMedium
    val bodyMedium = MaterialTheme.typography.bodyMedium

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.width * 0.1f
            val radius = (min(size.width, size.height) - strokeWidth) / 2
            val sweepAngle = (totalUsage.toFloat() / dailyLimit) * 360f

            // Background ring
            drawArc(
                color = surfaceVariant,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress ring
            drawArc(
                color = when {
                    totalUsage >= dailyLimit -> error
                    totalUsage >= dailyLimit * 0.8f -> tertiary
                    else -> primary
                },
                startAngle = -90f,
                sweepAngle = sweepAngle.coerceIn(0f, 360f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${totalUsage / 60}h ${totalUsage % 60}m",
                style = headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "of ${dailyLimit / 60}h limit",
                style = bodyMedium,
                color = onSurfaceVariant
            )
        }
    }
} 