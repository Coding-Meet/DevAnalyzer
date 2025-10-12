package com.meet.project.analyzer.data.models.storage

import kotlinx.serialization.Serializable

@Serializable
data class GradleVersionInfo(
    val version: String,
    val sizeReadable: String,
    val sizeBytes: Long,
    val path: String,
)
