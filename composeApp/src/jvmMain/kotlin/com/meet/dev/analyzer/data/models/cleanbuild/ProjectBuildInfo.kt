@file:OptIn(ExperimentalUuidApi::class)

package com.meet.dev.analyzer.data.models.cleanbuild

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ProjectBuildInfo(
    val uniqueId: String = Uuid.random().toString(),
    val projectName: String,
    val projectPath: String,
    val modules: List<ModuleBuild>,
    val sizeBytes: Long,
    val sizeFormatted: String
) {
    val totalSize: Long get() = modules.sumOf { it.sizeBytes }
    val selectedModules: List<ModuleBuild> get() = modules.filter { it.isSelected }
    val selectedSize: Long get() = selectedModules.sumOf { it.sizeBytes }
}

data class ModuleBuild(
    val uniqueId: String = Uuid.random().toString(),
    val moduleName: String,
    val path: String,
    val sizeBytes: Long,
    val sizeFormatted: String,
    val isSelected: Boolean = false
)