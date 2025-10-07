package com.meet.project.analyzer.data.models.scanner

import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class PropertiesFileInfo(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val type: PropertiesFileType,
    val size: String,
    val sizeBytes: Long,
    val content: String,
    val readLines: List<String>,
    val file: File
)

enum class PropertiesFileType(val fileName: String) {
    GRADLE_PROPERTIES("gradle.properties"),
    LOCAL_PROPERTIES("local.properties"),
}