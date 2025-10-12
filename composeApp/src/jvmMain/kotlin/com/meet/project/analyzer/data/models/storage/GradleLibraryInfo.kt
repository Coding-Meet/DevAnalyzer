package com.meet.project.analyzer.data.models.storage

import kotlinx.serialization.Serializable

@Serializable
data class GradleLibraryInfo(
    val groupId: String,
    val artifactId: String,
    val versions: List<GradleVersionInfo>,
    val path: String,
    val sizeReadable: String,
    val totalSizeBytes: Long
)