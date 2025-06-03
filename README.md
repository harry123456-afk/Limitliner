# LimitLiner

LimitLiner is an Android application designed to help users manage their app usage and maintain digital wellbeing. It provides features for tracking app usage, setting time limits, and maintaining focus.

## Features

- App usage tracking
- Custom time limits for apps
- Focus timer
- App blocking functionality
- Usage statistics and insights
- Modern Material Design UI with Jetpack Compose

## Technical Stack

- Kotlin
- Jetpack Compose for UI
- Android Architecture Components
- Material Design 3
- Kotlin Coroutines
- DataStore for preferences
- Accessibility Services for app blocking

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Run the app on an Android device or emulator (minimum SDK 26)

## Permissions Required

- PACKAGE_USAGE_STATS - For tracking app usage
- FOREGROUND_SERVICE - For background tracking
- POST_NOTIFICATIONS - For notifications
- SYSTEM_ALERT_WINDOW - For app blocking overlay
- Accessibility Service - For app blocking functionality

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## License

[Add your chosen license here] 