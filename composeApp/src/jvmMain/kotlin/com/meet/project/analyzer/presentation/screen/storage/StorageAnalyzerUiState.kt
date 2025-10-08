package com.meet.project.analyzer.presentation.screen.storage

import com.meet.project.analyzer.core.utility.StorageAnalyzerTabs
import com.meet.project.analyzer.data.models.storage.AvdInfo
import com.meet.project.analyzer.data.models.storage.DevEnvironmentInfo
import com.meet.project.analyzer.data.models.storage.GradleCacheInfo
import com.meet.project.analyzer.data.models.storage.GradleModulesInfo
import com.meet.project.analyzer.data.models.storage.SdkInfo
import kotlinx.serialization.Serializable

@Serializable
data class StorageAnalyzerUiState(
    val selectedTab: StorageAnalyzerTabs = StorageAnalyzerTabs.Overview,
    val selectedTabIndex: Int = 0,
    val previousTabIndex: Int = 0,
    val error: String? = null,
    val avds: List<AvdInfo> = emptyList(),
    val sdkInfo: SdkInfo? = null,
    val devEnvironmentInfo: DevEnvironmentInfo? = null,
    val gradleCaches: List<GradleCacheInfo> = emptyList(),
    val gradleModulesInfo: GradleModulesInfo? = null,
    val totalStorageUsed: String = "0 B",
    val totalStorageBytes: Long = 0L,

    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanStatus: String = "",
)
