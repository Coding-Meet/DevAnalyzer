package com.meet.dev.analyzer.presentation.screen.app

sealed interface AppUiIntent {
    data class ChangeTheme(val isDark: Boolean) : AppUiIntent
}