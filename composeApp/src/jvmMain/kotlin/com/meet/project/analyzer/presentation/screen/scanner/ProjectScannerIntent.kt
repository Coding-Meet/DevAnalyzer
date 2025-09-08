package com.meet.project.analyzer.presentation.screen.scanner

sealed class ProjectScannerIntent {
    object BrowseProject : ProjectScannerIntent()
    data class SelectProject(val path: String) : ProjectScannerIntent()
    object AnalyzeProject : ProjectScannerIntent()
    object ClearResults : ProjectScannerIntent()
    object ClearError : ProjectScannerIntent()

    data class ToggleDependencyExpansion(val moduleOrType: String) : ProjectScannerIntent()

}