package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AvdInfo(
    val name: String,
    val apiLevel: String?,
    val device: String?,
    val path: String,
    val configuredStorage: String,
    val actualStorage: String,
    val sizeBytes: Long = 0L
)













