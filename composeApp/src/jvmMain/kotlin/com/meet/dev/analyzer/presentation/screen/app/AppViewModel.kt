package com.meet.dev.analyzer.presentation.screen.app

import androidx.compose.ui.window.WindowPosition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val appPreferenceManager: AppPreferenceManager
) : ViewModel() {

    val appUiState = combine(
        appPreferenceManager.isDarkMode,
        appPreferenceManager.isOnboardingDone,
        appPreferenceManager.crashReportingEnabled,
        appPreferenceManager.isLocalLogsEnabled,
    ) { isDarkMode, isOnboardingDone, crashReportingEnabled, isLocalLogsEnabled ->
        AppUiState(
            isDarkMode = isDarkMode,
            isOnboardingDone = isOnboardingDone,
            crashReportingEnabled = crashReportingEnabled,
            isLocalLogsEnabled = isLocalLogsEnabled,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppUiState()
    )

    fun saveWindowWidthHeight(width: Float, height: Float) {
        viewModelScope.launch {
            appPreferenceManager.saveWindowWidthHeight(
                width = width,
                height = height,
            )
        }
    }

    fun saveWindowPosition(position: WindowPosition) {
        viewModelScope.launch {
            appPreferenceManager.saveWindowPosition(position = position)
        }
    }

    fun handleIntent(intent: AppUiIntent) {
        when (intent) {
            is AppUiIntent.ChangeTheme -> changeTheme(isDark = intent.isDark)
        }
    }

    private fun changeTheme(isDark: Boolean) {
        viewModelScope.launch {
            appPreferenceManager.saveTheme(isDark = !isDark)
        }
    }
}
