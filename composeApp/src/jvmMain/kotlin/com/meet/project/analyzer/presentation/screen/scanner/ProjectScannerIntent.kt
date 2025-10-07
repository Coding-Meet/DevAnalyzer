package com.meet.project.analyzer.presentation.screen.scanner

import com.meet.project.analyzer.core.utility.ProjectScreenTabs

sealed interface ProjectScannerIntent {
    data class SelectProject(val projectPath: String) : ProjectScannerIntent
    data object AnalyzeProject : ProjectScannerIntent
    data object ClearResults : ProjectScannerIntent
    data object ClearError : ProjectScannerIntent

    data class SelectTab(val index: Int, val projectScreenTabs: ProjectScreenTabs) :
        ProjectScannerIntent

}