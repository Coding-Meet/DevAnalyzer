package com.meet.project.analyzer.data.repository

import com.meet.project.analyzer.data.models.storage.AvdInfo
import com.meet.project.analyzer.data.models.storage.DevEnvironmentInfo
import com.meet.project.analyzer.data.models.storage.GradleCacheInfo
import com.meet.project.analyzer.data.models.storage.GradleModulesInfo
import com.meet.project.analyzer.data.models.storage.SdkInfo

interface StorageAnalyzerRepository {
    suspend fun getAvdInfoList(): List<AvdInfo>
    suspend fun getSdkInfo(): SdkInfo
    suspend fun getDevEnvironmentInfo(): DevEnvironmentInfo
    suspend fun getGradleCacheInfos(): List<GradleCacheInfo>
    suspend fun getGradleModulesInfo(): GradleModulesInfo?
}
