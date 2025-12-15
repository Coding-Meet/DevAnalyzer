package com.meet.dev.analyzer.presentation.screen.cleanbuild

import com.meet.dev.analyzer.data.models.cleanbuild.ProjectBuildInfo

data class CleanBuildUiState(
    val selectedPath: String = "",
    val projectBuildInfoList: List<ProjectBuildInfo> = emptyList(),
    val isAnalyzing: Boolean = false,
    val scanProgress: Float = 0f,
    val scanStatus: String = "",
    val scanElapsedTime: String = "00:00",
    val expandedProjects: Set<String> = emptySet(),
    val showConfirmDialog: Boolean = false,
    val showResultDialog: Boolean = false,
    val deletionResult: String = "",
    val error: String? = null,
)
