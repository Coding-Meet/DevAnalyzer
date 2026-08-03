package com.meet.dev.analyzer.presentation.screen.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.WindowPosition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager
import com.meet.dev.analyzer.data.repository.updater.UpdaterRepository
import com.meet.dev.analyzer.utility.analytics.AnalyticsEvent
import com.meet.dev.analyzer.utility.analytics.AnalyticsManager
import com.meet.dev.analyzer.utility.platform.DesktopConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val appPreferenceManager: AppPreferenceManager,
    private val updaterRepository: UpdaterRepository,
    private val analyticsManager: AnalyticsManager,
    private val appConfig: DesktopConfig,
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

    var updateDialogState by mutableStateOf<UpdateDialogState?>(null)
        private set

    init {
        openUpdateDialog()
        reportAppUpdateIfNeeded()
    }

    fun openUpdateDialog() {
        updateDialogState = UpdateDialogState.Checking
        checkUpdate()
    }

    /**
     * Fires [AnalyticsEvent.AppUpdated] only when the app version genuinely changes.
     * Skipped on first install (blank -> current version).
     * The stored version is always synchronized after the check.
     */
    private fun reportAppUpdateIfNeeded() {
        viewModelScope.launch {
            val lastVersion = appPreferenceManager.lastTrackedAnalyticsVersion.first()
            val currentVersion = appConfig.version

            if (lastVersion.isNotBlank() && lastVersion != currentVersion) {
                analyticsManager.capture(
                    AnalyticsEvent.AppUpdated(
                        fromVersion = lastVersion,
                        toVersion = currentVersion,
                    )
                )
            }

            // Always sync stored version (first install sets it for future updates)
            if (lastVersion != currentVersion) {
                appPreferenceManager.saveLastTrackedAnalyticsVersion(currentVersion)
            }
        }
    }

    fun closeUpdateDialog() {
        updateDialogState = null
    }

    fun trackUpdateDialogShown() {
        analyticsManager.capture(AnalyticsEvent.UpdateDialogShown)
    }

    fun trackUpdateClicked() {
        analyticsManager.capture(AnalyticsEvent.UpdateClicked)
    }

    fun trackUpdateDismissed() {
        analyticsManager.capture(AnalyticsEvent.UpdateDismissed)
    }

    fun checkUpdate() {
        viewModelScope.launch {
            updaterRepository.checkForUpdates()
                .onSuccess { release ->
                    if (release != null) {
                        val latestVersion = release.tagName.removePrefix("v").trim()
                        val currentVersion = appConfig.version.removePrefix("v").trim()

                        updateDialogState = if (isNewerVersion(currentVersion, latestVersion)) {
                            UpdateDialogState.Available(
                                version = release.tagName,
                                releaseNotes = release.body,
                                htmlUrl = release.htmlUrl
                            )
                        } else {
                            UpdateDialogState.UpToDate
                        }
                    } else {
                        updateDialogState = UpdateDialogState.UpToDate
                    }
                }
                .onFailure { exception ->
                    updateDialogState = UpdateDialogState.Error(
                        exception.message ?: "Failed to check for updates"
                    )
                }
        }
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val cleanCurrent = current.substringBefore("-").trim()
        val cleanLatest = latest.substringBefore("-").trim()

        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val currVal = currentParts.getOrElse(i) { 0 }
            val latVal = latestParts.getOrElse(i) { 0 }
            if (latVal > currVal) return true
            if (currVal > latVal) return false
        }
        return false
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
