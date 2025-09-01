package com.meet.project.analyzer.presentation.screen.storage

sealed interface StorageAnalyzerIntent {
    data object LoadAllData : StorageAnalyzerIntent
    data object LoadAvds : StorageAnalyzerIntent
    data object LoadSdkInfo : StorageAnalyzerIntent
    data object LoadDevEnvironment : StorageAnalyzerIntent
    data object LoadGradleCaches : StorageAnalyzerIntent
    data object LoadGradleModules : StorageAnalyzerIntent
    data object ClearError : StorageAnalyzerIntent
    data object RefreshData : StorageAnalyzerIntent
}