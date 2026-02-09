@file:OptIn(ExperimentalUuidApi::class)

package com.meet.dev.analyzer.data.models.cleanbuild

import com.meet.dev.analyzer.utility.platform.FolderFileUtils.formatSize
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
    val allSelected = modules.all { it.isSelected }
    val someSelected = modules.any { it.isSelected } && !allSelected

    val selectedModules = modules.filter { it.isSelected }
    val selectedSize = selectedModules.sumOf { it.sizeBytes }
    val selectedSizeFormatted = formatSize(selectedSize)
}

data class ModuleBuild(
    val uniqueId: String = Uuid.random().toString(),
    val moduleName: String,
    val projectName: String?,
    val path: String,
    val sizeBytes: Long,
    val sizeFormatted: String,
    val isSelected: Boolean = false
)