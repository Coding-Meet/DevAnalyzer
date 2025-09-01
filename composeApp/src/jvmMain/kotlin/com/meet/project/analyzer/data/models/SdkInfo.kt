package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SdkInfo(
    val sdkPath: String,
    val totalSize: String,
    val freeSpace: String,
    val platforms: List<SdkItem>,
    val buildTools: List<SdkItem>,
    val systemImages: List<SdkItem>,
    val extras: List<SdkItem>,
    val totalSizeBytes: Long = 0L
)