package com.meet.dev.analyzer.presentation.screen.setting

data class SettingsUiState(
    val crashReportingEnabled: Boolean = true,
    val appVersion: String = "1.0.0",
    val isCheckingUpdates: Boolean = false,
    val updateAvailable: Boolean = false,
    val showPathPicker: PathPickerType? = null,
    val currentPath: String = ""
)

enum class PathPickerType(
    val title: String,
) {
    ANDROID_SDK("Select Android SDK Path"),
    GRADLE_HOME("Select Gradle Home Path"),
    AVD_LOCATION("Select AVD Location"),
    ANDROID_FOLDER("Select .android Folder"),
    KONAN_FOLDER("Select .konan Folder"),
    JDK_1("Select JDK Path 1"),
    JDK_2("Select JDK Path 2"),
    JDK_3("Select JDK Path 3"),
    IDE_JETBRAINS_1("Select JetBrains Path 1"),
    IDE_JETBRAINS_2("Select JetBrains Path 2"),
    IDE_JETBRAINS_3("Select JetBrains Path 3"),
    IDE_GOOGLE_1("Select Google Path 1"),
    IDE_GOOGLE_2("Select Google Path 2"),
    IDE_GOOGLE_3("Select Google Path 3");
}