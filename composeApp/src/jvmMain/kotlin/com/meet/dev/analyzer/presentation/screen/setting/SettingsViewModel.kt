package com.meet.dev.analyzer.presentation.screen.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager
import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

class SettingsViewModel(
    private val pathPreferenceManger: PathPreferenceManger,
    private val appPreferenceManager: AppPreferenceManager,
) : ViewModel() {

    val pathSettingsState = combine(
        pathPreferenceManger.sdkPath,
        pathPreferenceManger.gradleUserHomePath,
        pathPreferenceManger.avdLocationPath,
        pathPreferenceManger.androidFolderPath,
        pathPreferenceManger.konanFolderPath,

        pathPreferenceManger.jdkPath1,
        pathPreferenceManger.jdkPath2,
        pathPreferenceManger.jdkPath3,

        pathPreferenceManger.ideJetBrainsPath1,
        pathPreferenceManger.ideJetBrainsPath2,
        pathPreferenceManger.ideJetBrainsPath3,

        pathPreferenceManger.ideGooglePath1,
        pathPreferenceManger.ideGooglePath2,
        pathPreferenceManger.ideGooglePath3
    ) { paths ->

        val sdk = paths[0]
        val gradleHome = paths[1]
        val avd = paths[2]
        val androidFolder = paths[3]
        val konan = paths[4]

        val jdk1 = paths[5]
        val jdk2 = paths[6]
        val jdk3 = paths[7]

        val jb1 = paths[8]
        val jb2 = paths[9]
        val jb3 = paths[10]

        val google1 = paths[11]
        val google2 = paths[12]
        val google3 = paths[13]

        PathUiState(
            sdkPath = sdk,
            gradleHomePath = gradleHome,
            avdLocationPath = avd,
            androidFolderPath = androidFolder,
            konanFolderPath = konan,

            jdkPath1 = jdk1,
            jdkPath2 = jdk2,
            jdkPath3 = jdk3,

            ideJetBrains1 = jb1,
            ideJetBrains2 = jb2,
            ideJetBrains3 = jb3,

            ideGoogle1 = google1,
            ideGoogle2 = google2,
            ideGoogle3 = google3,

            sdkPathStatus = validateSdkPath(sdk),
            gradleHomePathStatus = validateGradlePath(gradleHome),
            avdLocationPathStatus = validateAvdPath(avd),
            androidFolderPathStatus = validateAndroidPath(androidFolder),
            konanFolderPathStatus = validateKonanPath(konan),

            jdkPath1Status = validateJdkPath(jdk1),
            jdkPath2Status = validateJdkPath(jdk2),
            jdkPath3Status = validateJdkPath(jdk3),

            ideJetBrains1Status = validateIdePath(jb1),
            ideJetBrains2Status = validateIdePath(jb2),
            ideJetBrains3Status = validateIdePath(jb3),

            ideGoogle1Status = validateIdePath(google1),
            ideGoogle2Status = validateIdePath(google2),
            ideGoogle3Status = validateIdePath(google3)
        )


    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PathUiState()
    )

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
            is SettingsUiIntent.UploadLatestLogToGitHub -> uploadLatestLogToGitHub()

            is SettingsUiIntent.CheckForUpdates -> checkForUpdates()
            is SettingsUiIntent.ShowPathPicker -> showPathPicker(intent.path, intent.type)
        }
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appPreferenceManager.crashReportingEnabled,
                appPreferenceManager.isLocalLogsEnabled,
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
            pathPreferenceManger.saveSdkPath(path)
        }
    }

    private fun updateGradleHomePath(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveGradleUserHomePath(path)
        }
    }

    private fun updateAvdLocationPath(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveAvdLocationPath(path)
        }
    }

    private fun updateAndroidFolderPath(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveAndroidFolderPath(path)
        }
    }

    private fun updateKonanFolderPath(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveKonanFolderPath(path)
        }
    }

    private fun updateJdkPath1(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveJdkPath1(path)
        }
    }

    private fun updateJdkPath2(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveJdkPath2(path)
        }
    }

    private fun updateJdkPath3(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveJdkPath3(path)
        }
    }

    private fun updateIdeJetBrains1(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveIdeJetBrainsPath1(path)
        }
    }

    private fun updateIdeJetBrains2(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveIdeJetBrainsPath2(path)
        }
    }

    private fun updateIdeJetBrains3(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveIdeJetBrainsPath3(path)
        }
    }

    private fun updateIdeGoogle1(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveIdeGooglePath1(path)
        }
    }

    private fun updateIdeGoogle2(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveIdeGooglePath2(path)
        }
    }

    private fun updateIdeGoogle3(path: String) {
        viewModelScope.launch {
            pathPreferenceManger.saveIdeGooglePath3(path)
        }
    }


    private fun toggleCrashReporting(enabled: Boolean) {
        viewModelScope.launch {
            appPreferenceManager.saveCrashReportingEnabled(enabled)
            _uiState.update { it.copy(crashReportingEnabled = enabled) }
        }
    }

    private fun toggleLocalLogs(enabled: Boolean) {
        viewModelScope.launch {
            appPreferenceManager.saveLocalLogsEnabled(enabled)
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

    private fun uploadLatestLogToGitHub() {
        fun getLatestLogFile(): File? {
            val dir = File(System.getProperty("user.home"), ".dev_analyzer")

            return dir.listFiles { f ->
                f.extension == "log"
            }?.maxByOrNull { it.lastModified() }
        }

        val file = getLatestLogFile() ?: return

        val content = file.readText().trimIndent()

        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(content), null)
//        Desktop.getDesktop().browse(URI(REPORT_BUG))
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
