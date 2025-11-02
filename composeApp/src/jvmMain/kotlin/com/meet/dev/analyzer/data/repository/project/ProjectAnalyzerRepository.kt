package com.meet.dev.analyzer.data.repository.project

import com.meet.dev.analyzer.data.models.project.ProjectInfo

interface ProjectAnalyzerRepository {
    suspend fun analyzeProject(
        projectPath: String,
        updateProgress: (progress: Float, status: String) -> Unit
    ): ProjectInfo
}
