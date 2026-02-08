package com.meet.dev.analyzer.data.repository.cleanbuild

import com.meet.dev.analyzer.data.models.cleanbuild.ProjectBuildInfo

interface CleanBuildRepository {

    suspend fun scanProjects(
        rootPath: String,
        updateProgress: (progress: Float, status: String) -> Unit
    ): List<ProjectBuildInfo>

    suspend fun deleteBuildFolder(path: String): Pair<Boolean, String?>

}

