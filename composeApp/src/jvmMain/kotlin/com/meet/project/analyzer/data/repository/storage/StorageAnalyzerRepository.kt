package com.meet.project.analyzer.data.repository.storage

import com.meet.project.analyzer.data.models.storage.AndroidAvdInfo
import com.meet.project.analyzer.data.models.storage.AndroidSdkInfo
import com.meet.project.analyzer.data.models.storage.GradleInfo
import com.meet.project.analyzer.data.models.storage.IdeDataInfo
import com.meet.project.analyzer.data.models.storage.KonanInfo

interface StorageAnalyzerRepository {

    suspend fun analyzeIdeData(): IdeDataInfo
    suspend fun analyzeAndroidEvnData(): AndroidSdkInfo
    suspend fun analyzeAvdData(): AndroidAvdInfo
    suspend fun analyzeKonanData(): KonanInfo
    suspend fun analyzeGradleData(): GradleInfo
}
