package com.meet.dev.analyzer.presentation.screen.setting

sealed interface SettingsUiIntent {
    data class UpdateAndroidSdkPath(val path: String) : SettingsUiIntent
    data class UpdateGradleHomePath(val path: String) : SettingsUiIntent
    data class UpdateAvdLocationPath(val path: String) : SettingsUiIntent
    data class UpdateAndroidFolderPath(val path: String) : SettingsUiIntent
    data class UpdateKonanFolderPath(val path: String) : SettingsUiIntent

    data class UpdateJdkPath1(val path: String) : SettingsUiIntent
    data class UpdateJdkPath2(val path: String) : SettingsUiIntent
    data class UpdateJdkPath3(val path: String) : SettingsUiIntent

    data class UpdateIdeJetBrains1(val path: String) : SettingsUiIntent
    data class UpdateIdeJetBrains2(val path: String) : SettingsUiIntent
    data class UpdateIdeJetBrains3(val path: String) : SettingsUiIntent

    data class UpdateIdeGoogle1(val path: String) : SettingsUiIntent
    data class UpdateIdeGoogle2(val path: String) : SettingsUiIntent
    data class UpdateIdeGoogle3(val path: String) : SettingsUiIntent

    data class ToggleCrashReporting(val enabled: Boolean) : SettingsUiIntent

    data class ToggleLocalLogs(val enabled: Boolean) : SettingsUiIntent

    data object UploadLatestLogToGitHub : SettingsUiIntent


    data object CheckForUpdates : SettingsUiIntent
    data class ShowPathPicker(val path: String, val type: PathPickerType?) : SettingsUiIntent
}
