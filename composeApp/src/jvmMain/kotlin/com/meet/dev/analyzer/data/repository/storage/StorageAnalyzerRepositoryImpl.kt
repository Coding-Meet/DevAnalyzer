package com.meet.dev.analyzer.data.repository.storage

import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.data.models.storage.AndroidAvdInfo
import com.meet.dev.analyzer.data.models.storage.AndroidSdkInfo
import com.meet.dev.analyzer.data.models.storage.GradleInfo
import com.meet.dev.analyzer.data.models.storage.IdeDataInfo
import com.meet.dev.analyzer.data.models.storage.JdkInfo
import com.meet.dev.analyzer.data.models.storage.KonanInfo
import com.meet.dev.analyzer.data.repository.storage.helpers.AndroidSdkAnalyzer
import com.meet.dev.analyzer.data.repository.storage.helpers.AvdAnalyzer
import com.meet.dev.analyzer.data.repository.storage.helpers.GradleAnalyzer
import com.meet.dev.analyzer.data.repository.storage.helpers.IdeAnalyzer
import com.meet.dev.analyzer.data.repository.storage.helpers.KonanAnalyzer
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName

class StorageAnalyzerRepositoryImpl(
    private val pathPreferenceManger: PathPreferenceManger,
    private val ideAnalyzer: IdeAnalyzer,
    private val konanAnalyzer: KonanAnalyzer,
    private val avdAnalyzer: AvdAnalyzer,
    private val androidSdkAnalyzer: AndroidSdkAnalyzer,
    private val gradleAnalyzer: GradleAnalyzer
) : StorageAnalyzerRepository {

    private val TAG = tagName(javaClass = javaClass)

    override suspend fun analyzeIdeData(): IdeDataInfo = ideAnalyzer.analyzeIdeData()

    override suspend fun analyzeKonanData(): KonanInfo = konanAnalyzer.analyzeKonanData()

    override suspend fun analyzeAvdData(): AndroidAvdInfo = avdAnalyzer.analyzeAvdData()

    override suspend fun analyzeAndroidSdkData(): AndroidSdkInfo = androidSdkAnalyzer.analyzeAndroidSdkData()

    override suspend fun analyzeGradleData(): GradleInfo = gradleAnalyzer.analyzeGradleData()

    suspend fun loadJdkInfo(): JdkInfo = gradleAnalyzer.loadJdkInfo()
}