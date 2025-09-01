package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GradleCacheInfo(
    val version: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long = 0L
)


