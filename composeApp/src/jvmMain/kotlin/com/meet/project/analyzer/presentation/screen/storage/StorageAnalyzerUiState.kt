package com.meet.project.analyzer.presentation.screen.storage

import com.meet.project.analyzer.data.models.AvdInfo
import com.meet.project.analyzer.data.models.DevEnvironmentInfo
import com.meet.project.analyzer.data.models.GradleCacheInfo
import com.meet.project.analyzer.data.models.GradleModulesInfo
import com.meet.project.analyzer.data.models.SdkInfo
import kotlinx.serialization.Serializable

@Serializable
data class StorageAnalyzerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val avds: List<AvdInfo> = emptyList(),
    val sdkInfo: SdkInfo? = null,
    val devEnvironmentInfo: DevEnvironmentInfo? = null,
    val gradleCaches: List<GradleCacheInfo> = emptyList(),
    val gradleModulesInfo: GradleModulesInfo? = null,
    val totalStorageUsed: String = "0 B",
    val totalStorageBytes: Long = 0L
)
