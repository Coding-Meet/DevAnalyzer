package com.meet.project.analyzer.data.models.scanner

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.ui.graphics.vector.ImageVector
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
data class SubModuleBuildFileInfo(
    val uniqueId: String = Uuid.random().toString(),
    val moduleName: String,
    val path: String,
    val type: BuildFileType,
    val size: String,
    val sizeBytes: Long,
    val content: String,
    val readLines: List<String>,
    val file: File,
    val modulePath: String,
    val moduleIcon: ImageVector = Icons.Default.Folder,
    val plugins: List<Plugin> = emptyList(),
    val dependencies: List<Dependency> = emptyList()
)