package com.example.limitliner.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object AgeVerification : Screen("age_verification")
    object Permissions : Screen("permissions")
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
    object UsageLimits : Screen("usage_limits")
    object InAppBlocking : Screen("in_app_blocking")
    object Analytics : Screen("analytics")
    object Customization : Screen("customization")
    object FocusTimer : Screen("focus_timer")
} 