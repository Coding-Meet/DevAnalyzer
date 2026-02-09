package com.meet.dev.analyzer.presentation.screen.app

data class AppUiState(
    val isDarkMode: Boolean = true,
    val isOnboardingDone: Boolean = false,
    val crashReportingEnabled: Boolean = false,
    val isLocalLogsEnabled: Boolean = false,
)
