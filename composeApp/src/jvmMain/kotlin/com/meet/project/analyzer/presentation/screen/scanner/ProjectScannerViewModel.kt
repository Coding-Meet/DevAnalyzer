package com.meet.project.analyzer.presentation.screen.scanner

import androidx.lifecycle.ViewModel
import com.meet.project.analyzer.core.utility.AppLogger
import com.meet.project.analyzer.data.repository.scanner.ProgressCallback
import com.meet.project.analyzer.data.repository.scanner.ProjectScannerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectScannerViewModel(
    private val repository: ProjectScannerRepository
) : ViewModel() {
    private val TAG = "ProjectScannerViewModel"
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _uiState = MutableStateFlow(ProjectScannerUiState())
    val uiState = _uiState.asStateFlow()

    fun handleIntent(intent: ProjectScannerIntent) {
        when (intent) {
            is ProjectScannerIntent.SelectProject -> {
                _uiState.update {
                    it.copy(selectedPath = intent.path)
                }
            }

            is ProjectScannerIntent.AnalyzeProject -> analyzeProject()
            is ProjectScannerIntent.ClearResults -> clearResults()
            is ProjectScannerIntent.ClearError -> clearError()
            is ProjectScannerIntent.ToggleDependencyExpansion -> toggleDependencyExpansion(intent.moduleOrType)
            else -> {}
        }
    }

    private fun analyzeProject() {
        val currentPath = _uiState.value.selectedPath
        if (currentPath.isEmpty()) {
            _uiState.update {
                it.copy(error = "Please select a project directory first")
            }
            return
        }

        AppLogger.i(TAG) { "Starting project analysis for: $currentPath" }

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        error = null,
                        scanProgress = 0f,
                        scanStatus = "Initializing scan..."
                    )
                }

                val progressCallback = object : ProgressCallback {
                    override suspend fun updateProgress(progress: Float, status: String) {
                        _uiState.update {
                            it.copy(
                                scanProgress = progress,
                                scanStatus = status
                            )
                        }
                    }
                }

                val projectInfo = repository.analyzeProject(currentPath, progressCallback)

                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanResult = projectInfo,
                        scanProgress = 1f,
                        scanStatus = "Analysis complete"
                    )
                }

                AppLogger.i(TAG) { "Project analysis completed successfully" }

            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error analyzing project" }
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        error = "Analysis failed: ${e.message}",
                        scanProgress = 0f,
                        scanStatus = ""
                    )
                }
            }
        }
    }

    private fun toggleDependencyExpansion(moduleOrType: String) {
        _uiState.update { state ->
            val expanded = state.expandedDependencies.toMutableSet()
            if (expanded.contains(moduleOrType)) {
                expanded.remove(moduleOrType)
            } else {
                expanded.add(moduleOrType)
            }
            state.copy(expandedDependencies = expanded.toSet())
        }
    }

    private fun clearResults() {
        AppLogger.d(TAG) { "Clearing scan results" }
        _uiState.update {
            ProjectScannerUiState()
        }
    }

    private fun clearError() {
        AppLogger.d(TAG) { "Clearing error" }
        _uiState.update {
            it.copy(error = null)
        }
    }

    // Clean up when ViewModel is destroyed
    override fun onCleared() {
        AppLogger.d(TAG) { "ViewModel cleared" }
        super.onCleared()
    }

}
