package com.meet.dev.analyzer.presentation.screen.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.data.models.setting.PathPickerType
import com.meet.dev.analyzer.data.repository.setting.SettingsRepository
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val TAG = tagName(javaClass)

    val pathSettingsState =
        settingsRepository.pathSettings
            .map { paths ->

                PathUiState(
                    sdkPath = paths.sdkPath,
                    gradleHomePath = paths.gradleUserHomePath,
                    avdLocationPath = paths.avdLocationPath,
                    androidFolderPath = paths.androidFolderPath,
                    konanFolderPath = paths.konanFolderPath,

                    jdkPath1 = paths.jdkPath1,
                    jdkPath2 = paths.jdkPath2,
                    jdkPath3 = paths.jdkPath3,

                    ideJetBrains1 = paths.ideJetBrains1,
                    ideJetBrains2 = paths.ideJetBrains2,
                    ideJetBrains3 = paths.ideJetBrains3,

                    ideGoogle1 = paths.ideGoogle1,
                    ideGoogle2 = paths.ideGoogle2,
                    ideGoogle3 = paths.ideGoogle3,

                    sdkPathStatus = validateSdkPath(paths.sdkPath),
                    gradleHomePathStatus = validateGradlePath(paths.gradleUserHomePath),
                    avdLocationPathStatus = validateAvdPath(paths.avdLocationPath),
                    androidFolderPathStatus = validateAndroidPath(paths.androidFolderPath),
                    konanFolderPathStatus = validateKonanPath(paths.konanFolderPath),

                    jdkPath1Status = validateJdkPath(paths.jdkPath1),
                    jdkPath2Status = validateJdkPath(paths.jdkPath2),
                    jdkPath3Status = validateJdkPath(paths.jdkPath3),

                    ideJetBrains1Status = validateIdePath(paths.ideJetBrains1),
                    ideJetBrains2Status = validateIdePath(paths.ideJetBrains2),
                    ideJetBrains3Status = validateIdePath(paths.ideJetBrains3),

                    ideGoogle1Status = validateIdePath(paths.ideGoogle1),
                    ideGoogle2Status = validateIdePath(paths.ideGoogle2),
                    ideGoogle3Status = validateIdePath(paths.ideGoogle3),
                )
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, PathUiState())


    fun onIntent(intent: SettingsUiIntent) {
        when (intent) {
            is SettingsUiIntent.UpdateAndroidSdkPath -> updateAndroidSdkPath(intent.path)
            is SettingsUiIntent.UpdateGradleHomePath -> updateGradleHomePath(intent.path)
            is SettingsUiIntent.UpdateAvdLocationPath -> updateAvdLocationPath(intent.path)
            is SettingsUiIntent.UpdateAndroidFolderPath -> updateAndroidFolderPath(intent.path)
            is SettingsUiIntent.UpdateKonanFolderPath -> updateKonanFolderPath(intent.path)

            is SettingsUiIntent.UpdateJdkPath1 -> updateJdkPath1(intent.path)
            is SettingsUiIntent.UpdateJdkPath2 -> updateJdkPath2(intent.path)
            is SettingsUiIntent.UpdateJdkPath3 -> updateJdkPath3(intent.path)

            is SettingsUiIntent.UpdateIdeJetBrains1 -> updateIdeJetBrains1(intent.path)
            is SettingsUiIntent.UpdateIdeJetBrains2 -> updateIdeJetBrains2(intent.path)
            is SettingsUiIntent.UpdateIdeJetBrains3 -> updateIdeJetBrains3(intent.path)

            is SettingsUiIntent.UpdateIdeGoogle1 -> updateIdeGoogle1(intent.path)
            is SettingsUiIntent.UpdateIdeGoogle2 -> updateIdeGoogle2(intent.path)
            is SettingsUiIntent.UpdateIdeGoogle3 -> updateIdeGoogle3(intent.path)

            is SettingsUiIntent.ToggleCrashReporting -> toggleCrashReporting(intent.enabled)
            is SettingsUiIntent.ToggleLocalLogs -> toggleLocalLogs(intent.enabled)
            is SettingsUiIntent.ShowCrashLogDialog -> showCrashLogDialog()
            is SettingsUiIntent.DismissCrashLogDialog -> dismissCrashLogDialog()

            is SettingsUiIntent.CheckForUpdates -> checkForUpdates()
            is SettingsUiIntent.ShowPathPicker -> showPathPicker(intent.path, intent.type)
        }
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.crashReportingEnabled,
                settingsRepository.localLogsEnabled,
            ) { crashReportingEnabled, localLogsEnabled ->
                Pair(crashReportingEnabled, localLogsEnabled)
            }.collect { (crashReportingEnabled, localLogsEnabled) ->
                _uiState.update {
                    it.copy(
                        crashReportingEnabled = crashReportingEnabled,
                        localLogsEnabled = localLogsEnabled
                    )
                }

            }
        }
    }


    private fun updateAndroidSdkPath(path: String) {
        viewModelScope.launch {
            settingsRepository.saveSdkPath(path)
        }
    }

    private fun updateGradleHomePath(path: String) {
        viewModelScope.launch {
            settingsRepository.saveGradleUserHomePath(path)
        }
    }

    private fun updateAvdLocationPath(path: String) {
        viewModelScope.launch {
            settingsRepository.saveAvdLocationPath(path)
        }
    }

    private fun updateAndroidFolderPath(path: String) {
        viewModelScope.launch {
            settingsRepository.saveAndroidFolderPath(path)
        }
    }

    private fun updateKonanFolderPath(path: String) {
        viewModelScope.launch {
            settingsRepository.saveKonanFolderPath(path)
        }
    }

    private fun updateJdkPath1(path: String) {
        viewModelScope.launch {
            settingsRepository.saveJdkPath1(path)
        }
    }

    private fun updateJdkPath2(path: String) {
        viewModelScope.launch {
            settingsRepository.saveJdkPath2(path)
        }
    }

    private fun updateJdkPath3(path: String) {
        viewModelScope.launch {
            settingsRepository.saveJdkPath3(path)
        }
    }

    private fun updateIdeJetBrains1(path: String) {
        viewModelScope.launch {
            settingsRepository.saveIdeJetBrainsPath1(path)
        }
    }

    private fun updateIdeJetBrains2(path: String) {
        viewModelScope.launch {
            settingsRepository.saveIdeJetBrainsPath2(path)
        }
    }

    private fun updateIdeJetBrains3(path: String) {
        viewModelScope.launch {
            settingsRepository.saveIdeJetBrainsPath3(path)
        }
    }

    private fun updateIdeGoogle1(path: String) {
        viewModelScope.launch {
            settingsRepository.saveIdeGooglePath1(path)
        }
    }

    private fun updateIdeGoogle2(path: String) {
        viewModelScope.launch {
            settingsRepository.saveIdeGooglePath2(path)
        }
    }

    private fun updateIdeGoogle3(path: String) {
        viewModelScope.launch {
            settingsRepository.saveIdeGooglePath3(path)
        }
    }


    private fun toggleCrashReporting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCrashReporting(enabled)
            _uiState.update { it.copy(crashReportingEnabled = enabled) }
        }
    }

    private fun toggleLocalLogs(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLocalLogs(enabled)
            _uiState.update { it.copy(localLogsEnabled = enabled) }
        }
    }


    private fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdates = true) }
            delay(2000) // Simulate network call
            _uiState.update {
                it.copy(
                    isCheckingUpdates = false,
                    updateAvailable = false // Check actual update
                )
            }
        }
    }

    private fun showPathPicker(path: String, type: PathPickerType?) {
        _uiState.update {
            it.copy(
                currentPath = path,
                showPathPicker = type
            )
        }
    }

    private fun showCrashLogDialog() {
        val logFile = settingsRepository.getLatestLogFile()
        _uiState.update { it.copy(logFile = logFile, showCrashLogDialog = true) }
    }

    private fun dismissCrashLogDialog() {
        _uiState.update { it.copy(showCrashLogDialog = false, logFile = null) }
    }


    private fun validateSdkPath(path: String): PathStatus {
        if (path.isBlank()) return PathStatus.INVALID

        val file = File(path)
        if (!file.exists()) return PathStatus.INVALID
        if (!file.isDirectory) return PathStatus.INVALID

        // Check for SDK structure
        val hasPlatforms = File(file, "platforms").exists()
        val hasBuildTools = File(file, "build-tools").exists()

        return when {
            hasPlatforms && hasBuildTools -> PathStatus.VALID
            else -> PathStatus.INVALID
        }
    }

    private fun validateGradlePath(path: String): PathStatus {
        if (path.isBlank()) return PathStatus.INVALID

        val file = File(path)
        if (!file.exists()) return PathStatus.INVALID
        if (!file.isDirectory) return PathStatus.INVALID

        // Check for Gradle structure
        val hasCaches = File(file, "caches").exists()
        val hasWrapper = File(file, "wrapper").exists()

        return when {
            hasCaches || hasWrapper -> PathStatus.VALID
            else -> PathStatus.INVALID
        }
    }

    private fun validateAvdPath(path: String): PathStatus {
        if (path.isBlank()) return PathStatus.INVALID

        val file = File(path)
        if (!file.exists()) return PathStatus.INVALID
        if (!file.isDirectory) return PathStatus.INVALID

        // Check for AVD files
        val hasIniFiles = file.listFiles { _, name -> name.endsWith(".ini") }?.isNotEmpty() ?: false
        val hasAvdFolders =
            file.listFiles { f -> f.isDirectory && f.name.endsWith(".avd") }?.isNotEmpty() ?: false

        return when {
            hasIniFiles || hasAvdFolders -> PathStatus.VALID
            else -> PathStatus.INVALID
        }
    }


    private fun validateIdePath(path: String): PathStatus {
        if (path.isBlank()) return PathStatus.INVALID
        val file = File(path)
        if (!file.exists()) return PathStatus.INVALID
        if (!file.isDirectory) return PathStatus.INVALID
        val hasJetBrains = file.listFiles()?.any {
            it.isDirectory && it.name.contains("JetBrains", ignoreCase = true)
        } ?: false
        val hasGoogle = file.listFiles()?.any {
            it.isDirectory && it.name.contains("Google", ignoreCase = true)
        } ?: false
        return when {
            hasJetBrains || hasGoogle -> PathStatus.VALID
            file.listFiles()?.isNotEmpty() == true -> PathStatus.VALID
            else -> PathStatus.INVALID
        }
    }

    private fun validateAndroidPath(path: String): PathStatus {
        if (path.isBlank()) return PathStatus.INVALID

        val file = File(path)
        if (!file.exists()) return PathStatus.INVALID
        if (!file.isDirectory) return PathStatus.INVALID

        // Check for .android folder structure (avd folder expected)
        val hasAvd = File(file, "avd").exists()

        return when {
            hasAvd -> PathStatus.VALID
            else -> PathStatus.INVALID
        }
    }

    private fun validateKonanPath(path: String): PathStatus {
        if (path.isBlank()) return PathStatus.INVALID

        val file = File(path)
        if (!file.exists()) return PathStatus.INVALID
        if (!file.isDirectory) return PathStatus.INVALID

        // Check for .konan structure (dependencies or kotlin-native folders)
        val hasDependencies = File(file, "dependencies").exists()
        val hasKotlinNative = file.listFiles()?.any {
            it.isDirectory && it.name.contains("kotlin-native", ignoreCase = true)
        } ?: false

        return when {
            hasDependencies || hasKotlinNative -> PathStatus.VALID
            else -> PathStatus.INVALID
        }
    }

    private fun validateJdkPath(path: String): PathStatus {
        if (path.isBlank()) return PathStatus.INVALID
        val file = File(path)
        if (!file.exists()) return PathStatus.INVALID
        if (!file.isDirectory) return PathStatus.INVALID
        val hasBin = File(file, "bin").exists()
        val hasLib = File(file, "lib").exists()
        val hasRelease = File(file, "release").exists()
        val hasContentsHome = File(file, "Contents/Home").exists()
        return when {
            (hasBin && hasLib) || hasRelease || hasContentsHome -> PathStatus.VALID
            file.listFiles()?.isNotEmpty() == true -> PathStatus.VALID
            else -> PathStatus.INVALID
        }
    }

}
