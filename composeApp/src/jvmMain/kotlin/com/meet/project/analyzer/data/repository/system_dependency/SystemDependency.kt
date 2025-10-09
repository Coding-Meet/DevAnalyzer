package com.meet.project.analyzer.data.repository.system_dependency

interface SystemDependency {
    suspend fun getAllDependencies(rootDirPath: String)
}