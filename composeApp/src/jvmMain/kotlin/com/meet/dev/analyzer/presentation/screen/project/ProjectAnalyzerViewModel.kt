package com.meet.dev.analyzer.presentation.screen.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.data.models.project.BuildFileType
import com.meet.dev.analyzer.data.models.project.SettingsGradleFileType
import com.meet.dev.analyzer.data.repository.project.ProjectAnalyzerRepository
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils.formatElapsedTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import java.io.File

class ProjectAnalyzerViewModel(
    private val repository: ProjectAnalyzerRepository
) : ViewModel() {

    private val TAG = tagName(javaClass = javaClass)

    private val _uiState = MutableStateFlow(ProjectAnalyzerUiState())
    val uiState = _uiState.asStateFlow()

    fun handleIntent(intent: ProjectAnalyzerIntent) {
        when (intent) {
            is ProjectAnalyzerIntent.SelectProject -> {
                _uiState.update {
                    it.copy(
                        selectedPath = intent.projectPath,
                        projectInfo = null,
                    )
                }
            }

            is ProjectAnalyzerIntent.AnalyzeProject -> analyzeProject()
            is ProjectAnalyzerIntent.ClearResults -> clearResults()
            is ProjectAnalyzerIntent.ClearError -> clearError()
            is ProjectAnalyzerIntent.SelectTab -> {
                _uiState.update {
                    it.copy(
                        previousTabIndex = intent.previousTabIndex,
                        selectedTabIndex = intent.currentTabIndex,
                        selectedTab = intent.projectScreenTabs
                    )
                }
            }
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

        AppLogger.i(tag = TAG) { "Starting project analysis for: $currentPath" }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        error = null,
                        scanProgress = 0f,
                        scanStatus = "Initializing scan...",
                        scanElapsedTime = "00:00"
                    )
                }
                // 🧩 Main analysis logic
                if (validateProject(currentPath)) {
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
                    val projectInfo = repository.analyzeProject(currentPath) { progress, status ->
                        AppLogger.d(tag = TAG) { "Progress: $progress, Status: $status" }
                        _uiState.update {
                            it.copy(
                                scanProgress = progress.coerceIn(0f, 1f),
                                scanStatus = status
                            )
                        }
                    }
                    timerJob.cancelAndJoin()
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isScanning = false,
                                projectInfo = projectInfo,
                                scanProgress = 1f,
                                scanStatus = "Analysis complete"
                            )
                        }
                    }

                    // 🕓 Final elapsed log
                    val formatted = formatElapsedTime(startTime)
                    AppLogger.i(tag = TAG) { "Project analysis completed successfully in $formatted" }
                }
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
                AppLogger.e(tag = TAG, throwable = e) { "Error analyzing project" }
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

    private fun validateProject(projectPath: String): Boolean {
        val projectDir = File(projectPath)

        // 1. Directory check
        if (!projectDir.exists() || !projectDir.isDirectory) {
            updateError("Project directory not found: $projectPath")
            return false
        }

        // 2. settings.gradle check
        val isSettingsGradleFound = SettingsGradleFileType.entries.any {
            File(projectDir, it.fileName).exists()
        }
        if (!isSettingsGradleFound) {
            updateError("Invalid project: settings.gradle(.kts) not found")
            return false
        }

        // 3. build.gradle(.kts) - at least ONE anywhere
        val buildFiles = projectDir.walkTopDown()
            .filter {
                it.isFile && BuildFileType.entries.any { type ->
                    it.name == type.fileName
                }
            }
            .toList()

        if (buildFiles.isEmpty()) {
            updateError("Invalid project: no build.gradle(.kts) found")
            return false
        }
        return true
    }

    private fun updateError(message: String) {
        _uiState.update {
            it.copy(
                isScanning = false,
                error = message,
                scanProgress = 0f,
                scanStatus = ""
            )
        }
    }

    private fun clearResults() {
        AppLogger.d(tag = TAG) { "Clearing scan results" }
        _uiState.update {
            ProjectAnalyzerUiState()
        }
    }

    private fun clearError() {
        AppLogger.d(tag = TAG) { "Clearing error" }
        _uiState.update {
            it.copy(error = null)
        }
    }

    // Clean up when ViewModel is destroyed
    override fun onCleared() {
        AppLogger.d(tag = TAG) { "ViewModel cleared" }
        super.onCleared()
    }

}
