package com.meet.dev.analyzer.data.models.storage

import kotlinx.serialization.Serializable

@Serializable
data class StorageAnalyzerInfo(
    val totalStorageUsed: String = "0 B",
    val totalStorageBytes: Long = 0L,

    val ideDataInfo: IdeDataInfo,
    val androidAvdInfo: AndroidAvdInfo,
    val androidSdkInfo: AndroidSdkInfo,
    val konanInfo: KonanInfo,
    val gradleInfo: GradleInfo,
    val storageBreakdown: StorageBreakdown,
    val storageBreakdownItemList: List<StorageBreakdownItem>
)