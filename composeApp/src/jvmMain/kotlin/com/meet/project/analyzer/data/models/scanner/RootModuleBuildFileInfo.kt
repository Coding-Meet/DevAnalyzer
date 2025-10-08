package com.meet.project.analyzer.data.models.scanner

import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class RootModuleBuildFileInfo(
    val uniqueId: String = Uuid.random().toString(),
    val moduleName: String,
    val path: String,
    val type: BuildFileType,
    val size: String,
    val sizeBytes: Long,
    val content: String,
    val readLines: List<String>,
    val file: File,
    val plugins: List<Plugin> = emptyList(),
    val dependencies: List<Dependency> = emptyList(),
)

enum class BuildFileType(val fileName: String) {
    BUILD_GRADLE_KTS("build.gradle.kts"),
    BUILD_GRADLE("build.gradle"),
}