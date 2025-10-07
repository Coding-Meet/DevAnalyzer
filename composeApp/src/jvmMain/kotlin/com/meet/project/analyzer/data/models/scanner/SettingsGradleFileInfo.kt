package com.meet.project.analyzer.data.models.scanner

import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class SettingsGradleFileInfo(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val type: SettingsGradleFileType,
    val size: String,
    val sizeBytes: Long,
    val content: String,
    val readLines: List<String>,
    val file: File
)

enum class SettingsGradleFileType(val fileName: String) {
    SETTINGS_GRADLE_KTS("settings.gradle.kts"),
    SETTINGS_GRADLE("settings.gradle"),
}