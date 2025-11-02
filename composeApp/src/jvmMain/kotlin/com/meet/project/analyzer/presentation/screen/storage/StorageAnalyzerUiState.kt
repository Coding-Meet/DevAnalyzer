package com.meet.project.analyzer.presentation.screen.storage

import com.meet.project.analyzer.core.utility.StorageAnalyzerTabs
import com.meet.project.analyzer.data.models.storage.StorageAnalyzerInfo
import kotlinx.serialization.Serializable

@Serializable
data class StorageAnalyzerUiState(
    val selectedTab: StorageAnalyzerTabs = StorageAnalyzerTabs.Overview,
    val selectedTabIndex: Int = 0,
    val previousTabIndex: Int = 0,
    val error: String? = null,
    val storageAnalyzerInfo: StorageAnalyzerInfo? = null,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanStatus: String = "",
)