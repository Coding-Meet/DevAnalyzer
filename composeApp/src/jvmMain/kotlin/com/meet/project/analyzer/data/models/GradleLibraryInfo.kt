package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GradleLibraryInfo(
    val groupId: String,
    val artifactId: String,
    val versions: List<String>,
    val totalSize: String = "Unknown",
    val totalSizeBytes: Long = 0L
)