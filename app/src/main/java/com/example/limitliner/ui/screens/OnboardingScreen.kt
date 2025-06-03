package com.example.limitliner.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.limitliner.R
import java.util.*

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onAgeVerified: (Int) -> Unit
) {
    var birthYear by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.onboarding_illustration),
            contentDescription = "Welcome to LimitLiner",
            modifier = Modifier.size(200.dp)
        )

        Text(
            text = "Welcome to LimitLiner",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Take control of your digital wellbeing",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = birthYear,
            onValueChange = {
                if (it.length <= 4) birthYear = it
                showError = false
            },
            label = { Text("Birth Year") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError,
            supportingText = if (showError) {
                { Text("Please enter a valid birth year") }
            } else null
        )

        Button(
            onClick = {
                val year = birthYear.toIntOrNull()
                if (year != null && year in 1900..currentYear && currentYear - year >= 13) {
                    onAgeVerified(year)
                    onComplete()
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }

        Text(
            text = "You must be at least 13 years old to use LimitLiner",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 