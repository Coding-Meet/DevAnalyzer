package com.meet.dev.analyzer.presentation.screen.project

import com.meet.dev.analyzer.core.utility.ProjectScreenTabs
import com.meet.dev.analyzer.data.models.project.ProjectInfo

data class ProjectAnalyzerUiState(
    val selectedTab: ProjectScreenTabs = ProjectScreenTabs.Overview,
    val selectedTabIndex: Int = 0,
    val previousTabIndex: Int = 0,
    val selectedPath: String = "",
    val projectInfo: ProjectInfo? = null,
    val error: String? = null,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanStatus: String = "",
)