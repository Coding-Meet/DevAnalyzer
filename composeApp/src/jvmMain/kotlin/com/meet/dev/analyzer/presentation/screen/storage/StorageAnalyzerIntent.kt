package com.meet.dev.analyzer.presentation.screen.storage

import com.meet.dev.analyzer.core.utility.StorageAnalyzerTabs

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