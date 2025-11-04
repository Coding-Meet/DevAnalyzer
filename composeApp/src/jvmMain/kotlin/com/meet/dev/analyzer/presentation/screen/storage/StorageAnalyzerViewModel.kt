package com.meet.dev.analyzer.presentation.screen.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.core.utility.AppLogger
import com.meet.dev.analyzer.core.utility.Constant
import com.meet.dev.analyzer.core.utility.Utils
import com.meet.dev.analyzer.core.utility.Utils.tagName
import com.meet.dev.analyzer.data.models.storage.AndroidAvdInfo
import com.meet.dev.analyzer.data.models.storage.AndroidSdkInfo
import com.meet.dev.analyzer.data.models.storage.GradleInfo
import com.meet.dev.analyzer.data.models.storage.IdeDataInfo
import com.meet.dev.analyzer.data.models.storage.KonanInfo
import com.meet.dev.analyzer.data.models.storage.StorageAnalyzerInfo
import com.meet.dev.analyzer.data.models.storage.StorageBreakdownItem
import com.meet.dev.analyzer.data.models.storage.StorageBreakdownItemColor
import com.meet.dev.analyzer.data.repository.storage.StorageAnalyzerRepository
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
import kotlinx.serialization.json.Json
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
        AppLogger.d(TAG) { "Handling intent: ${intent::class.simpleName}" }
        when (intent) {
            is StorageAnalyzerIntent.LoadAllData -> loadAllData() /*dummyAllData()*/
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

    private fun dummyAllData() {
        AppLogger.i(TAG) { "Loading all data" }
        loadAllJob?.cancel()
        loadAllJob = viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        scanProgress = 0f,
                        scanStatus = "Starting scan...",
                        error = null
                    )
                }

                // Fake progress updates
                val totalSteps = 10
                for (i in 1..totalSteps) {
                    delay(200) // simulate work
                    val progress = i / totalSteps.toFloat()
                    _uiState.update {
                        it.copy(
                            scanProgress = progress,
                            scanStatus = when (i) {
                                1 -> "Initializing..."
                                3 -> "Fetching AVD info..."
                                5 -> "Reading SDK info..."
                                7 -> "Analyzing Gradle cache..."
                                9 -> "Finalizing scan..."
                                else -> "Scanning in progress..."
                            }
                        )
                    }
                }

                // Load dummy data from JSON
                val storageAnalyzerUiState =
                    Json.decodeFromString(
                        StorageAnalyzerUiState.serializer(),
                        Constant.devStorageStr
                    )

                AppLogger.i(TAG) {
                    "All data loaded successfully. Total storage: ${
                        storageAnalyzerUiState.storageAnalyzerInfo?.totalStorageBytes?.let {
                            Utils.formatSize(
                                it
                            )
                        }
                    }"
                }

                // Final update after "loading"
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 1f,
                        scanStatus = "Scan completed successfully!",
                        error = null,
                        storageAnalyzerInfo = storageAnalyzerUiState.storageAnalyzerInfo
                    )
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading all data" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 0f,
                        scanStatus = "",
                        error = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadAllData() {
        AppLogger.i(TAG) { "Loading all data" }
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
                        val elapsedMillis = System.currentTimeMillis() - startTime
                        val seconds = (elapsedMillis / 1000) % 60
                        val minutes = (elapsedMillis / 1000) / 60
                        val formatted = String.format("%02d:%02d", minutes, seconds)
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
                    val totalStorageUsed = Utils.formatSize(totalBytes)
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
                                    storageBreakdownItemList = storageBreakdownItemList
                                ),
                                error = null
                            )
                        }
                    }

                    AppLogger.i(TAG) {
                        "All data loaded successfully. Total storage: ${
                            Utils.formatSize(
                                totalBytes
                            )
                        }"
                    }
                }

                val totalSeconds = measureTime.inWholeSeconds
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                AppLogger.i(TAG) { "All data loaded in ${minutes}m ${seconds}s" }

            } catch (e: IOException) {
                AppLogger.e(TAG, e) { "File read error" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanStatus = "",
                        error = "Failed to access some files"
                    )
                }
            } catch (e: SecurityException) {
                AppLogger.e(TAG, e) { "Permission denied" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanStatus = "",
                        error = "Permission denied for storage access"
                    )
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading all data" }
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
        AppLogger.d(TAG) { "Clearing error" }
        _uiState.update { it.copy(error = null) }
    }

    private fun refreshData() {
        AppLogger.i(TAG) { "Refreshing all data" }
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
        AppLogger.d(TAG) { "Cancelling all jobs" }
        loadAllJob?.cancel()
        loadAllJob = null

    }

    override fun onCleared() {
        AppLogger.d(TAG) { "ViewModel cleared" }
        cancelAllJobs()
        super.onCleared()
    }

}
