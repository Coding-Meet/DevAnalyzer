package com.meet.project.analyzer.presentation.screen.storage

import androidx.lifecycle.ViewModel
import com.meet.project.analyzer.core.utility.AppLogger
import com.meet.project.analyzer.core.utility.Constant
import com.meet.project.analyzer.core.utility.Utils
import com.meet.project.analyzer.data.models.AvdInfo
import com.meet.project.analyzer.data.models.DevEnvironmentInfo
import com.meet.project.analyzer.data.models.GradleModulesInfo
import com.meet.project.analyzer.data.models.SdkInfo
import com.meet.project.analyzer.data.repository.StorageAnalyzerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
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

    private val Any.TAG: String
        get() {
            return if (!javaClass.isAnonymousClass) {
                val name = javaClass.simpleName
                if (name.length <= 23) name else name.substring(0, 23)// first 23 chars
            } else {
                val name = javaClass.name
                if (name.length <= 23) name else name.substring(
                    name.length - 23,
                    name.length
                )// last 23 chars
            }
        }

    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _uiState = MutableStateFlow(StorageAnalyzerUiState())
    val uiState: StateFlow<StorageAnalyzerUiState> = _uiState.asStateFlow()

    private val _loadingJobs = mutableMapOf<String, Job>()

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
        }
    }

    private fun dummyAllData() {
        AppLogger.i(TAG) { "Loading all data" }
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val storageAnalyzerUiState =
                Json.decodeFromString(StorageAnalyzerUiState.serializer(), Constant.devStorageStr)
            AppLogger.i(TAG) {
                "All data loaded successfully. Total storage: ${
                    Utils.formatSize(
                        storageAnalyzerUiState.totalStorageBytes
                    )
                }"
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    avds = storageAnalyzerUiState.avds,
                    sdkInfo = storageAnalyzerUiState.sdkInfo,
                    devEnvironmentInfo = storageAnalyzerUiState.devEnvironmentInfo,
                    gradleCaches = storageAnalyzerUiState.gradleCaches,
                    gradleModulesInfo = storageAnalyzerUiState.gradleModulesInfo,
                    totalStorageUsed = storageAnalyzerUiState.totalStorageUsed,
                    totalStorageBytes = storageAnalyzerUiState.totalStorageBytes,
                    error = null
                )
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Error loading all data" }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    private fun loadAllData() {
        AppLogger.i(TAG) { "Loading all data" }
        _uiState.update { it.copy(isLoading = true, error = null) }

        val job = viewModelScope.launch {
            try {
                // Load all data in parallel
                val measureTime = measureTime {

                    val avdsDeferred = async(Dispatchers.IO) { repository.getAvdInfoList() }
                    val sdkInfoDeferred = async(Dispatchers.IO) { repository.getSdkInfo() }
                    val devEnvDeferred =
                        async(Dispatchers.IO) { repository.getDevEnvironmentInfo() }
                    val gradleCachesDeferred =
                        async(Dispatchers.IO) { repository.getGradleCacheInfos() }
                    val gradleModulesDeferred =
                        async(Dispatchers.IO) { repository.getGradleModulesInfo() }

                    val avds = avdsDeferred.await()
                    val sdkInfo = sdkInfoDeferred.await()
                    val devEnvironmentInfo = devEnvDeferred.await()
                    val gradleCaches = gradleCachesDeferred.await()
                    val gradleModulesInfo = gradleModulesDeferred.await()

                    // Calculate total storage
                    val totalBytes = calculateTotalStorage(
                        avds,
                        sdkInfo,
                        devEnvironmentInfo,
                        gradleModulesInfo
                    )


                    _uiState.update {
                        it.copy(
                            isLoading = false,
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
                        isLoading = false,
                        error = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
        _loadingJobs["loadAll"] = job
    }

    private fun loadAvds() {
        AppLogger.i(TAG) { "Loading AVDs" }
        val job = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val avds = repository.getAvdInfoList()
                _uiState.update {
                    it.copy(
                        avds = avds,
                        isLoading = false
                    )
                }
                AppLogger.i(TAG) { "Loaded ${avds.size} AVDs" }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading AVDs" }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load AVDs: ${e.message}"
                    )
                }
            }
        }
        _loadingJobs["loadAvds"] = job
    }

    private fun loadSdkInfo() {
        AppLogger.i(TAG) { "Loading SDK info" }
        val job = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val sdkInfo = repository.getSdkInfo()
                _uiState.update {
                    it.copy(
                        sdkInfo = sdkInfo,
                        isLoading = false
                    )
                }
                AppLogger.i(TAG) { "SDK info loaded: ${sdkInfo.totalSize}" }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading SDK info" }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load SDK info: ${e.message}"
                    )
                }
            }
        }
        _loadingJobs["loadSdk"] = job
    }

    private fun loadDevEnvironment() {
        AppLogger.i(TAG) { "Loading dev environment" }
        val job = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val devEnv = repository.getDevEnvironmentInfo()
                _uiState.update {
                    it.copy(
                        devEnvironmentInfo = devEnv,
                        isLoading = false
                    )
                }
                AppLogger.i(TAG) { "Dev environment loaded" }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading dev environment" }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load dev environment: ${e.message}"
                    )
                }
            }
        }
        _loadingJobs["loadDevEnv"] = job
    }

    private fun loadGradleCaches() {
        AppLogger.i(TAG) { "Loading Gradle caches" }
        val job = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val caches = repository.getGradleCacheInfos()
                _uiState.update {
                    it.copy(
                        gradleCaches = caches,
                        isLoading = false
                    )
                }
                AppLogger.i(TAG) { "Loaded ${caches.size} Gradle caches" }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading Gradle caches" }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load Gradle caches: ${e.message}"
                    )
                }
            }
        }
        _loadingJobs["loadGradleCaches"] = job
    }

    private fun loadGradleModules() {
        AppLogger.i(TAG) { "Loading Gradle modules" }
        val job = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val modules = repository.getGradleModulesInfo()
                _uiState.update {
                    it.copy(
                        gradleModulesInfo = modules,
                        isLoading = false
                    )
                }
                AppLogger.i(TAG) {
                    "Gradle modules loaded: ${modules?.libraries?.size ?: 0} libraries"
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading Gradle modules" }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load Gradle modules: ${e.message}"
                    )
                }
            }
        }
        _loadingJobs["loadGradleModules"] = job
    }

    private fun clearError() {
        AppLogger.d(TAG) { "Clearing error" }
        _uiState.update { it.copy(error = null) }
    }

    private fun refreshData() {
        AppLogger.i(TAG) { "Refreshing all data" }
        // Cancel existing jobs
        _loadingJobs.values.forEach { it.cancel() }
        _loadingJobs.clear()

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
        _loadingJobs.values.forEach { it.cancel() }
        _loadingJobs.clear()
    }

    // Clean up when ViewModel is destroyed
    override fun onCleared() {
        AppLogger.d(TAG) { "ViewModel cleared" }
        cancelAllJobs()
        super.onCleared()
    }

}
