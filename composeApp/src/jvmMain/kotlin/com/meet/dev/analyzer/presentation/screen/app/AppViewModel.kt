package com.meet.dev.analyzer.presentation.screen.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.WindowPosition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.BuildKonfig
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager
import io.github.kdroidfilter.nucleus.updater.NucleusUpdater
import io.github.kdroidfilter.nucleus.updater.UpdateResult
import io.github.kdroidfilter.nucleus.updater.provider.GitHubProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val appPreferenceManager: AppPreferenceManager
) : ViewModel() {
    val updater = NucleusUpdater {
        provider = GitHubProvider(owner = "Coding-Meet", repo = "DevAnalyzer")

        // Current app version (auto-detected from jpackage.app-version system property)
        currentVersion = BuildKonfig.VERSION_NAME

        // Release channel: "latest", "beta", or "alpha"
        channel = "latest"

        // Allow installing older versions
        allowDowngrade = true

        // Allow pre-release versions (auto-enabled if currentVersion contains "-")
        allowPrerelease = false

        // Force a specific installer format (auto-detected if null)
        executableType = null
    }
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
    var updateDialogState by mutableStateOf<UpdateDialogState?>(null)
        private set

    fun openUpdateDialog() {
        updateDialogState = UpdateDialogState.Checking
        checkUpdate()
    }

    fun closeUpdateDialog() {
        updateDialogState = null
    }

    fun checkUpdate() {
        viewModelScope.launch {
            when (val result = updater.checkForUpdates()) {
                is UpdateResult.Available -> {
                    updateDialogState = UpdateDialogState.Available(result.info.version)
                    println(
                        "Update available: ${result}"
                    )
                    updater.downloadUpdate(result.info).collect { progress ->
                        updateDialogState = UpdateDialogState.Downloading(progress.percent.toInt())
                        if (progress.file != null) {
                            updater.installAndRestart(progress.file!!)
                        }
                    }
                }
                is UpdateResult.NotAvailable -> {
                    println("No update available")
                    updateDialogState = UpdateDialogState.UpToDate
                }
                is UpdateResult.Error -> {
                    println("Error: ${result.exception}")
                    updateDialogState = UpdateDialogState.Error(
                        result.exception.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

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
