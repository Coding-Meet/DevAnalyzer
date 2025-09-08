package com.meet.project.analyzer.data.repository.scanner

import com.meet.project.analyzer.data.models.scanner.ProjectInfo

interface ProjectScannerRepository {
    suspend fun analyzeProject(projectPath: String, progressCallback: ProgressCallback): ProjectInfo
}
