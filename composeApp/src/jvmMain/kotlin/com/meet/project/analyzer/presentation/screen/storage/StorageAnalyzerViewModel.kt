package com.meet.project.analyzer.presentation.screen.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.project.analyzer.core.utility.AppLogger
import com.meet.project.analyzer.core.utility.Constant
import com.meet.project.analyzer.core.utility.Utils
import com.meet.project.analyzer.core.utility.Utils.tagName
import com.meet.project.analyzer.data.models.AvdInfo
import com.meet.project.analyzer.data.models.DevEnvironmentInfo
import com.meet.project.analyzer.data.models.GradleModulesInfo
import com.meet.project.analyzer.data.models.SdkInfo
import com.meet.project.analyzer.data.repository.StorageAnalyzerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.measureTime

class StorageAnalyzerViewModel(
    private val repository: StorageAnalyzerRepository
) : ViewModel() {

    private val TAG = tagName(javaClass = javaClass)

    private val _uiState = MutableStateFlow(StorageAnalyzerUiState())
    val uiState: StateFlow<StorageAnalyzerUiState> = _uiState.asStateFlow()

    private var loadAllJob: Job? = null
    private var loadSdkJob: Job? = null
    private var loadAvdsJob: Job? = null
    private var loadDevEnvJob: Job? = null
    private var loadGradleCachesJob: Job? = null
    private var loadGradleModulesJob: Job? = null

    init {
        handleIntent(StorageAnalyzerIntent.LoadAllData)
    }

    fun handleIntent(intent: StorageAnalyzerIntent) {
        AppLogger.d(TAG) { "Handling intent: ${intent::class.simpleName}" }
        when (intent) {
            is StorageAnalyzerIntent.LoadAllData -> dummyAllData() /*loadAllData()*/
            is StorageAnalyzerIntent.LoadAvds -> loadAvds()
            is StorageAnalyzerIntent.LoadSdkInfo -> loadSdkInfo()
            is StorageAnalyzerIntent.LoadDevEnvironment -> loadDevEnvironment()
            is StorageAnalyzerIntent.LoadGradleCaches -> loadGradleCaches()
            is StorageAnalyzerIntent.LoadGradleModules -> loadGradleModules()
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
                        Utils.formatSize(storageAnalyzerUiState.totalStorageBytes)
                    }"
                }

                // Final update after "loading"
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 1f,
                        scanStatus = "Scan completed successfully!",
                        error = null,
                        avds = storageAnalyzerUiState.avds,
                        sdkInfo = storageAnalyzerUiState.sdkInfo,
                        devEnvironmentInfo = storageAnalyzerUiState.devEnvironmentInfo,
                        gradleCaches = storageAnalyzerUiState.gradleCaches,
                        gradleModulesInfo = storageAnalyzerUiState.gradleModulesInfo,
                        totalStorageUsed = storageAnalyzerUiState.totalStorageUsed,
                        totalStorageBytes = storageAnalyzerUiState.totalStorageBytes,
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
                scanStatus = "Starting scan..."
            )
        }
        loadAllJob?.cancel()
        loadAllJob = viewModelScope.launch {
            try {
                val measureTime = measureTime {

                    _uiState.update { it.copy(scanStatus = "Loading AVDs...", scanProgress = 0.1f) }
                    val avds = repository.getAvdInfoList()

                    _uiState.update {
                        it.copy(
                            scanStatus = "Analyzing SDK info...",
                            scanProgress = 0.3f
                        )
                    }
                    val sdkInfo = repository.getSdkInfo()


                    _uiState.update {
                        it.copy(
                            scanStatus = "Scanning Development Environment...",
                            scanProgress = 0.5f
                        )
                    }
                    val devEnvironmentInfo = repository.getDevEnvironmentInfo()


                    _uiState.update {
                        it.copy(
                            scanStatus = "Checking Gradle Caches...",
                            scanProgress = 0.7f
                        )
                    }
                    val gradleCaches = repository.getGradleCacheInfos()

                    _uiState.update {
                        it.copy(
                            scanStatus = "Reading Gradle Modules...",
                            scanProgress = 0.85f
                        )
                    }
                    val gradleModulesInfo = repository.getGradleModulesInfo()


                    val totalBytes = calculateTotalStorage(
                        avds, sdkInfo, devEnvironmentInfo, gradleModulesInfo
                    )

                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            scanProgress = 1f,
                            scanStatus = "Scan completed successfully!",
                            avds = avds,
                            sdkInfo = sdkInfo,
                            devEnvironmentInfo = devEnvironmentInfo,
                            gradleCaches = gradleCaches,
                            gradleModulesInfo = gradleModulesInfo,
                            totalStorageUsed = Utils.formatSize(totalBytes),
                            totalStorageBytes = totalBytes,
                            error = null
                        )
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

    private fun loadAvds() {
        AppLogger.i(TAG) { "Loading AVDs" }
        loadAvdsJob?.cancel()
        loadAvdsJob = viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        scanProgress = 0f,
                        scanStatus = "Scanning AVDs..."
                    )
                }
                val avds = repository.getAvdInfoList()
                _uiState.update {
                    it.copy(
                        avds = avds,
                        isScanning = false,
                        scanProgress = 1f,
                        scanStatus = "AVDs loaded successfully!"
                    )
                }
                AppLogger.i(TAG) { "Loaded ${avds.size} AVDs" }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading AVDs" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 0f,
                        scanStatus = "",
                        error = "Failed to load AVDs: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadSdkInfo() {
        AppLogger.i(TAG) { "Loading SDK info" }
        loadSdkJob?.cancel()
        loadSdkJob = viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        scanProgress = 0f,
                        scanStatus = "Scanning SDK info..."
                    )
                }
                val sdkInfo = repository.getSdkInfo()
                _uiState.update {
                    it.copy(
                        sdkInfo = sdkInfo,
                        isScanning = false,
                        scanProgress = 1f,
                        scanStatus = "SDK info loaded successfully!"
                    )
                }
                AppLogger.i(TAG) { "SDK info loaded: ${sdkInfo.totalSize}" }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading SDK info" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 0f,
                        scanStatus = "",
                        error = "Failed to load SDK info: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadDevEnvironment() {
        AppLogger.i(TAG) { "Loading dev environment" }
        loadDevEnvJob?.cancel()
        loadDevEnvJob = viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        scanProgress = 0f,
                        scanStatus = "Scanning dev environment..."
                    )
                }
                val devEnv = repository.getDevEnvironmentInfo()
                _uiState.update {
                    it.copy(
                        devEnvironmentInfo = devEnv,
                        isScanning = false,
                        scanProgress = 1f,
                        scanStatus = "Dev environment loaded successfully!"
                    )
                }
                AppLogger.i(TAG) { "Dev environment loaded" }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading dev environment" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 0f,
                        scanStatus = "",
                        error = "Failed to load dev environment: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadGradleCaches() {
        AppLogger.i(TAG) { "Loading Gradle caches" }
        loadGradleCachesJob?.cancel()
        loadGradleCachesJob = viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        scanProgress = 0f,
                        scanStatus = "Scanning Gradle caches..."
                    )
                }
                val caches = repository.getGradleCacheInfos()
                _uiState.update {
                    it.copy(
                        gradleCaches = caches,
                        isScanning = false,
                        scanProgress = 1f,
                        scanStatus = "Gradle caches loaded successfully!"
                    )
                }
                AppLogger.i(TAG) { "Loaded ${caches.size} Gradle caches" }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading Gradle caches" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 0f,
                        scanStatus = "",
                        error = "Failed to load Gradle caches: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadGradleModules() {
        AppLogger.i(TAG) { "Loading Gradle modules" }
        loadGradleModulesJob?.cancel()
        loadGradleModulesJob = viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        scanProgress = 0f,
                        scanStatus = "Scanning Gradle modules..."
                    )
                }
                val modules = repository.getGradleModulesInfo()
                _uiState.update {
                    it.copy(
                        gradleModulesInfo = modules,
                        isScanning = false,
                        scanProgress = 1f,
                        scanStatus = "Gradle modules loaded successfully!"
                    )
                }
                AppLogger.i(TAG) {
                    "Gradle modules loaded: ${modules?.libraries?.size ?: 0} libraries"
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading Gradle modules" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 0f,
                        scanStatus = "",
                        error = "Failed to load Gradle modules: ${e.message}"
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
        avds: List<AvdInfo>,
        sdkInfo: SdkInfo,
        devEnvironmentInfo: DevEnvironmentInfo?,
        gradleModulesInfo: GradleModulesInfo?
    ): Long {
        val avdStorage = avds.sumOf { it.sizeBytes }
        val sdkStorage = sdkInfo.totalSizeBytes
        val gradleCacheStorage = devEnvironmentInfo?.gradleCache?.sizeBytes ?: 0L
        val ideaCacheStorage = devEnvironmentInfo?.ideaCache?.sizeBytes ?: 0L
        val konanStorage = devEnvironmentInfo?.konanInfo?.sizeBytes ?: 0L
        val skikoStorage = devEnvironmentInfo?.skikoInfo?.sizeBytes ?: 0L
        val jdkStorage = devEnvironmentInfo?.jdks?.sumOf { it.sizeBytes } ?: 0L
        val gradleModulesStorage = gradleModulesInfo?.sizeBytes ?: 0L

        return avdStorage + sdkStorage + gradleCacheStorage + ideaCacheStorage +
                konanStorage + skikoStorage + jdkStorage + gradleModulesStorage
    }

    private fun cancelAllJobs() {
        AppLogger.d(TAG) { "Cancelling all jobs" }
        loadAllJob?.cancel()
        loadSdkJob?.cancel()
        loadAvdsJob?.cancel()
        loadDevEnvJob?.cancel()
        loadGradleCachesJob?.cancel()
        loadGradleModulesJob?.cancel()

        loadAllJob = null
        loadSdkJob = null
        loadAvdsJob = null
        loadDevEnvJob = null
        loadGradleCachesJob = null
        loadGradleModulesJob = null

    }

    // Clean up when ViewModel is destroyed
    override fun onCleared() {
        AppLogger.d(TAG) { "ViewModel cleared" }
        cancelAllJobs()
        super.onCleared()
    }

}
