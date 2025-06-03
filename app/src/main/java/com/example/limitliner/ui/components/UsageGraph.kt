package com.example.limitliner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UsageGraph(
    usageData: List<Int>, // List of usage minutes per hour
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    val outlineColor = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = width / usageData.size
        val maxUsage = usageData.maxOrNull() ?: 0
        val scale = height / maxUsage.toFloat()

        // Draw bars
        usageData.forEachIndexed { index, usage ->
            val barHeight = usage * scale
            val x = index * barWidth
            val y = height - barHeight

            drawRect(
                color = primaryColor,
                topLeft = Offset(x + barWidth * 0.1f, y),
                size = Size(barWidth * 0.8f, barHeight)
            )
        }

        // Draw baseline
        drawLine(
            color = outlineColor,
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 1.dp.toPx()
        )
    }
} 