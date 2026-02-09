package com.meet.dev.analyzer.presentation.screen.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.data.models.storage.AndroidAvdInfo
import com.meet.dev.analyzer.data.models.storage.AndroidSdkInfo
import com.meet.dev.analyzer.data.models.storage.GradleInfo
import com.meet.dev.analyzer.data.models.storage.IdeDataInfo
import com.meet.dev.analyzer.data.models.storage.KonanInfo
import com.meet.dev.analyzer.data.models.storage.StorageAnalyzerInfo
import com.meet.dev.analyzer.data.models.storage.StorageBreakdown
import com.meet.dev.analyzer.data.models.storage.StorageBreakdownItem
import com.meet.dev.analyzer.data.models.storage.StorageBreakdownItemColor
import com.meet.dev.analyzer.data.repository.storage.StorageAnalyzerRepository
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import com.meet.dev.analyzer.utility.platform.FolderFileUtils.formatElapsedTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlin.time.measureTime

class StorageAnalyzerViewModel(
    private val storageAnalyzerRepository: StorageAnalyzerRepository
) : ViewModel() {

    private val TAG = tagName(javaClass = javaClass)

    private val _uiState = MutableStateFlow(StorageAnalyzerUiState())
    val uiState: StateFlow<StorageAnalyzerUiState> = _uiState.asStateFlow()

    private var loadAllJob: Job? = null

    init {
        handleIntent(StorageAnalyzerIntent.LoadAllData)
    }

    fun handleIntent(intent: StorageAnalyzerIntent) {
        when (intent) {
            is StorageAnalyzerIntent.LoadAllData -> loadAllData()
            is StorageAnalyzerIntent.ClearError -> clearError()
            is StorageAnalyzerIntent.RefreshData -> refreshData()
            is StorageAnalyzerIntent.SelectTab -> {
                _uiState.update {
                    it.copy(
                        previousTabIndex = intent.previousTabIndex,
                        selectedTabIndex = intent.currentTabIndex,
                        selectedTab = intent.storageAnalyzerTabs
                    )
                }
            }
        }
    }

    private fun loadAllData() {
        AppLogger.i(tag = TAG) { "Loading all data" }
        _uiState.update {
            it.copy(
                isScanning = true,
                scanProgress = 0f,
                scanStatus = "Starting scan...",
                scanElapsedTime = "00:00",
                error = null
            )
        }
        loadAllJob?.cancel()
        loadAllJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()

                // Start elapsed time counter
                val timerJob = launch {
                    while (isActive) {
                        val formatted = formatElapsedTime(
                            startTime = startTime
                        )
                        _uiState.update { it.copy(scanElapsedTime = formatted) }
                        delay(1000)
                    }
                }

                val measureTime = measureTime {

                    _uiState.update {
                        it.copy(
                            scanStatus = "Loading AVDs...",
                            scanProgress = 0.1f
                        )
                    }
                    val androidAvdInfo = storageAnalyzerRepository.analyzeAvdData()

                    _uiState.update {
                        it.copy(
                            scanStatus = "Analyzing SDK info...",
                            scanProgress = 0.3f
                        )
                    }
                    val androidSdkInfo = storageAnalyzerRepository.analyzeAndroidSdkData()

                    _uiState.update {
                        it.copy(
                            scanStatus = "Analyzing Kotlin/Native info...",
                            scanProgress = 0.4f
                        )
                    }
                    val konanInfo = storageAnalyzerRepository.analyzeKonanData()

                    _uiState.update {
                        it.copy(
                            scanStatus = "Analyzing IDE info...",
                            scanProgress = 0.5f
                        )
                    }
                    val ideDataInfo = storageAnalyzerRepository.analyzeIdeData()

                    _uiState.update {
                        it.copy(
                            scanStatus = "Checking Gradle Caches...",
                            scanProgress = 0.7f
                        )
                    }
                    val gradleInfo = storageAnalyzerRepository.analyzeGradleData()

                    val totalBytes = calculateTotalStorage(
                        androidAvdInfo = androidAvdInfo,
                        androidSdkInfo = androidSdkInfo,
                        konanInfo = konanInfo,
                        ideDataInfo = ideDataInfo,
                        gradleInfo = gradleInfo
                    )
                    val totalStorageUsed = FolderFileUtils.formatSize(totalBytes)
                    val storageBreakdownItemList = listOf(
                        StorageBreakdownItem(
                            name = "Gradle",
                            sizeByte = gradleInfo.totalSizeBytes,
                            sizeReadable = gradleInfo.sizeReadable,
                            storageBreakdownItemColor = StorageBreakdownItemColor.GradleCache
                        ),
                        StorageBreakdownItem(
                            name = "IDE",
                            sizeByte = ideDataInfo.totalSizeBytes,
                            sizeReadable = ideDataInfo.totalSizeReadable,
                            storageBreakdownItemColor = StorageBreakdownItemColor.IdeCache
                        ),
                        StorageBreakdownItem(
                            name = "Kotlin/Native",
                            sizeByte = konanInfo.totalSizeBytes,
                            sizeReadable = konanInfo.sizeReadable,
                            storageBreakdownItemColor = StorageBreakdownItemColor.KotlinNativeInfo
                        ),
                        StorageBreakdownItem(
                            name = "SDK",
                            sizeByte = androidSdkInfo.totalSizeBytes,
                            sizeReadable = androidSdkInfo.sizeReadable,
                            storageBreakdownItemColor = StorageBreakdownItemColor.Sdk
                        ),
                        StorageBreakdownItem(
                            name = "AVD",
                            sizeByte = androidAvdInfo.totalSizeBytes,
                            sizeReadable = androidAvdInfo.sizeReadable,
                            storageBreakdownItemColor = StorageBreakdownItemColor.Avd
                        ),
                        StorageBreakdownItem(
                            name = "Jdk",
                            sizeByte = gradleInfo.jdkInfo.totalSizeBytes,
                            sizeReadable = gradleInfo.jdkInfo.sizeReadable,
                            storageBreakdownItemColor = StorageBreakdownItemColor.Jdk
                        )
                    ).sortedByDescending {
                        it.sizeByte
                    }
                    val storageBreakdownItemTotalSize = storageBreakdownItemList.sumOf {
                        it.sizeByte
                    }
                    val storageBreakdownItemTotalSizeReadable =
                        FolderFileUtils.formatSize(storageBreakdownItemTotalSize)
                    val storageBreakdown = StorageBreakdown(
                        totalSizeByte = storageBreakdownItemTotalSize,
                        totalSizeReadable = storageBreakdownItemTotalSizeReadable,
                        storageBreakdownItemList = storageBreakdownItemList.map {
                            val percentage =
                                it.sizeByte.toFloat() / storageBreakdownItemTotalSize * 100
                            val percentageReadable = String.format("%.2f", percentage)
                            it.copy(
                                percentage = percentage,
                                percentageReadable = percentageReadable
                            )
                        },
                    )
                    timerJob.cancelAndJoin()

                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isScanning = false,
                                scanProgress = 1f,
                                scanStatus = "Scan completed successfully!",
                                storageAnalyzerInfo = StorageAnalyzerInfo(
                                    ideDataInfo = ideDataInfo,
                                    konanInfo = konanInfo,
                                    androidAvdInfo = androidAvdInfo,
                                    androidSdkInfo = androidSdkInfo,
                                    gradleInfo = gradleInfo,
                                    totalStorageUsed = totalStorageUsed,
                                    totalStorageBytes = totalBytes,
                                    storageBreakdown = storageBreakdown,
                                    storageBreakdownItemList = storageBreakdownItemList
                                ),
                                error = null
                            )
                        }
                    }

                    AppLogger.i(tag = TAG) {
                        "All data loaded successfully. Total storage: ${
                            FolderFileUtils.formatSize(
                                totalBytes
                            )
                        }"
                    }
                }

                val totalSeconds = measureTime.inWholeSeconds
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                AppLogger.i(tag = TAG) { "All data loaded in ${minutes}m ${seconds}s" }

            } catch (e: IOException) {
                AppLogger.e(tag = TAG, throwable = e) { "File read error" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanStatus = "",
                        error = "Failed to access some files"
                    )
                }
            } catch (e: SecurityException) {
                AppLogger.e(tag = TAG, throwable = e) { "Permission denied" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanStatus = "",
                        error = "Permission denied for storage access"
                    )
                }
            } catch (e: Exception) {
                AppLogger.e(tag = TAG, throwable = e) { "Error loading all data" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanStatus = "",
                        error = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }

    private fun clearError() {
        AppLogger.d(tag = TAG) { "Clearing error" }
        _uiState.update { it.copy(error = null) }
    }

    private fun refreshData() {
        AppLogger.i(tag = TAG) { "Refreshing all data" }
        // Cancel existing jobs
        cancelAllJobs()

        // Reload all data
        loadAllData()
    }

    private fun calculateTotalStorage(
        androidAvdInfo: AndroidAvdInfo,
        androidSdkInfo: AndroidSdkInfo,
        konanInfo: KonanInfo,
        ideDataInfo: IdeDataInfo,
        gradleInfo: GradleInfo
    ): Long {
        val avdStorage = androidAvdInfo.totalSizeBytes
        val sdkStorage = androidSdkInfo.totalSizeBytes
        val konanStorage = konanInfo.totalSizeBytes
        val ideStorage = ideDataInfo.totalSizeBytes
        val gradleStorage = gradleInfo.totalSizeBytes
        return avdStorage + sdkStorage + konanStorage + ideStorage + gradleStorage
    }

    private fun cancelAllJobs() {
        AppLogger.d(tag = TAG) { "Cancelling all jobs" }
        loadAllJob?.cancel()
        loadAllJob = null

    }

    override fun onCleared() {
        AppLogger.d(tag = TAG) { "ViewModel cleared" }
        cancelAllJobs()
        super.onCleared()
    }

}
