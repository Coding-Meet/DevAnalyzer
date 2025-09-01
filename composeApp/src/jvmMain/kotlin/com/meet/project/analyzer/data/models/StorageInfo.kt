package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class StorageInfo(
    val path: String,
    val exists: Boolean,
    val sizeReadable: String,
    val sizeBytes: Long = 0L
)