package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class JdkInfo(
    val path: String,
    val version: String?,
    val sizeReadable: String,
    val sizeBytes: Long = 0L
)
