package com.meet.project.analyzer.data.repository.system_dependency

interface SystemDependencyRepository {
    suspend fun getAllDependencies(rootDirPath: String)
}