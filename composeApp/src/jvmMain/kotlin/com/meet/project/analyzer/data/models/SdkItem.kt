package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SdkItem(
    val name: String,
    val path: String,
    val size: String,
    val sizeBytes: Long = 0L
)
