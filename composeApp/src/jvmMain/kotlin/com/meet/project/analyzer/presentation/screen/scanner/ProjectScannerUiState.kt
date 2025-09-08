package com.meet.project.analyzer.presentation.screen.scanner

import com.meet.project.analyzer.data.models.scanner.ProjectInfo

data class ProjectScannerUiState(
    val isScanning: Boolean = false,
    val selectedPath: String = "/Users/meet/AndroidStudioProjects/NotesApp",
    val scanResult: ProjectInfo? = null,
    val error: String? = null,
    val scanProgress: Float = 0f,
    val scanStatus: String = "",
    val expandedDependencies: Set<String> = emptySet()
)