package com.meet.dev.analyzer.presentation.screen.setting

import com.meet.dev.analyzer.data.models.setting.LogFile
import com.meet.dev.analyzer.data.models.setting.PathPickerType

data class SettingsUiState(
    val crashReportingEnabled: Boolean = true,
    val localLogsEnabled: Boolean = true,
    val isCheckingUpdates: Boolean = false,
    val updateAvailable: Boolean = false,
    val logFile: LogFile? = null,
    val showCrashLogDialog: Boolean = false,
    val showPathPicker: PathPickerType? = null,
    val currentPath: String = ""
)