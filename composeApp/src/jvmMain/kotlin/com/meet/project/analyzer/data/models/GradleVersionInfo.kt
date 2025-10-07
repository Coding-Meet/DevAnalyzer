package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GradleVersionInfo(
    val version: String,
    val sizeReadable: String,
    val sizeBytes: Long
)
