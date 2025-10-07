package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GradleLibraryInfo(
    val groupId: String,
    val artifactId: String,
    val versions: List<GradleVersionInfo>,
    val totalSize: String,
    val totalSizeBytes: Long
)