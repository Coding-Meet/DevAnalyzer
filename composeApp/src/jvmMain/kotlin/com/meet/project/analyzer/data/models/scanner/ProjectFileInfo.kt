package com.meet.project.analyzer.data.models.scanner

import java.io.File

data class ProjectFileInfo(
    val uniqueId: String,
    val name: String,
    val path: String,
    val relativePath: String,
    val type: FileType,
    val size: String,
    val sizeBytes: Long,
    val extension: String,
    val file: File,
    val content: String? = null,
    val isReadable: Boolean = true
)

enum class FileType {
    SOURCE_KOTLIN,
    SOURCE_JAVA,
    BUILD_SCRIPT,
    CONFIGURATION,
    RESOURCE,
    MANIFEST,
    LAYOUT,
    DRAWABLE,
    VALUES,
    ASSETS,
    PROPERTIES,
    JSON,
    XML,
    TEXT,
    MARKDOWN,
    IMAGE,
    OTHER
}