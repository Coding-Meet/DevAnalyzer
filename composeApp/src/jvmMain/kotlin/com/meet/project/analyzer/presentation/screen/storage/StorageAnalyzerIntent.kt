package com.meet.project.analyzer.presentation.screen.storage

import com.meet.project.analyzer.core.utility.StorageAnalyzerTabs

sealed interface StorageAnalyzerIntent {
    data object LoadAllData : StorageAnalyzerIntent
    data object ClearError : StorageAnalyzerIntent
    data object RefreshData : StorageAnalyzerIntent
    data class SelectTab(
        val previousTabIndex: Int,
        val currentTabIndex: Int,
        val storageAnalyzerTabs: StorageAnalyzerTabs
    ) : StorageAnalyzerIntent
}