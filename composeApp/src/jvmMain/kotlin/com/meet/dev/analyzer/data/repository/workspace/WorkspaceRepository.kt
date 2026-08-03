package com.meet.dev.analyzer.data.repository.workspace

import com.meet.dev.analyzer.data.models.workspace.WorkspaceAnalysisResult

interface WorkspaceRepository {
    suspend fun analyzeWorkspace(
        workspacePaths: List<String>,
        sdkPath: String,
        gradleHomePath: String,
        konanPath: String,
        onProgress: (progress: Float, status: String) -> Unit
    ): WorkspaceAnalysisResult

    suspend fun deleteResource(path: String): Pair<Boolean, String?>
}
