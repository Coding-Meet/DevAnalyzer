package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CacheInfo(
    val name: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long = 0L
)
