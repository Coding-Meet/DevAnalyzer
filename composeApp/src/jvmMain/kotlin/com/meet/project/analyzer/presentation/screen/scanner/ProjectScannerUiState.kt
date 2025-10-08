package com.meet.project.analyzer.presentation.screen.scanner

import com.meet.project.analyzer.core.utility.ProjectScreenTabs
import com.meet.project.analyzer.data.models.scanner.ProjectInfo

data class ProjectScannerUiState(
    val selectedTab: ProjectScreenTabs = ProjectScreenTabs.Overview,
    val selectedTabIndex: Int = 0,
    val previousTabIndex: Int = 0,
    val selectedPath: String = "/Users/meet/AndroidStudioProjects/KAppMaker-KMP",
    val projectInfo: ProjectInfo? = null,
    val error: String? = null,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanStatus: String = "",
)