package com.meet.project.analyzer.data.repository

import com.meet.project.analyzer.data.models.AvdInfo
import com.meet.project.analyzer.data.models.DevEnvironmentInfo
import com.meet.project.analyzer.data.models.GradleCacheInfo
import com.meet.project.analyzer.data.models.GradleModulesInfo
import com.meet.project.analyzer.data.models.SdkInfo

interface StorageAnalyzerRepository {
    suspend fun getAvdInfoList(): List<AvdInfo>
    suspend fun getSdkInfo(): SdkInfo
    suspend fun getDevEnvironmentInfo(): DevEnvironmentInfo
    suspend fun getGradleCacheInfos(): List<GradleCacheInfo>
    suspend fun getGradleModulesInfo(): GradleModulesInfo?
}
