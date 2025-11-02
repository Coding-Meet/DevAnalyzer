package com.meet.dev.analyzer.data.models.project

import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class GradleWrapperPropertiesFileInfo(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val size: String,
    val sizeBytes: Long,
    val content: String,
    val readLines: List<String>,
    val file: File
)