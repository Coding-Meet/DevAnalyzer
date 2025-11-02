package com.meet.dev.analyzer.presentation.screen.project

import com.meet.dev.analyzer.core.utility.ProjectScreenTabs

sealed interface ProjectAnalyzerIntent {
    data class SelectProject(val projectPath: String) : ProjectAnalyzerIntent
    data object AnalyzeProject : ProjectAnalyzerIntent
    data object ClearResults : ProjectAnalyzerIntent
    data object ClearError : ProjectAnalyzerIntent

    data class SelectTab(
        val previousTabIndex: Int,
        val currentTabIndex: Int,
        val projectScreenTabs: ProjectScreenTabs
    ) : ProjectAnalyzerIntent

}