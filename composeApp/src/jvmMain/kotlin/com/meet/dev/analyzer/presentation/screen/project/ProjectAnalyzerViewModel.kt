package com.meet.dev.analyzer.presentation.screen.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.core.utility.AppLogger
import com.meet.dev.analyzer.core.utility.Utils.tagName
import com.meet.dev.analyzer.data.repository.project.ProjectAnalyzerRepository
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

    init {
        AppLogger.d(TAG) { "ViewModel initialized" }
    }

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

        AppLogger.i(TAG) { "Starting project analysis for: $currentPath" }

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

                // 🧩 Main analysis logic
                if (validateProject(currentPath)) {
                    val projectInfo = repository.analyzeProject(currentPath) { progress, status ->
                        AppLogger.d(TAG) { "Progress: $progress, Status: $status" }
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
                    val totalMillis = System.currentTimeMillis() - startTime
                    val totalSeconds = totalMillis / 1000
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60
                    AppLogger.i(TAG) { "Project analysis completed successfully in ${minutes}m ${seconds}s" }
                } else {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            scanProgress = 0f,
                            scanStatus = "",
                            error = "Invalid project directory"
                        )
                    }
                }

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

    private fun validateProject(projectPath: String): Boolean {
        val projectDir = File(projectPath)
        var isProjectValid = true

        // 1. Directory check
        if (!projectDir.exists() || !projectDir.isDirectory) {
            updateError("Project directory not found: $projectPath")
            isProjectValid = false
        }

        // 2. Gradlew check
        if (!File(projectDir, "gradlew").exists() && !File(projectDir, "gradlew.bat").exists()) {
            updateError("Invalid project: gradlew/gradlew.bat not found")
            isProjectValid = false
        }

        // 3. settings.gradle check
        if (!(File(projectDir, "settings.gradle").exists() ||
                    File(projectDir, "settings.gradle.kts").exists())
        ) {
            updateError("Invalid project: settings.gradle(.kts) not found")
            isProjectValid = false
        }

        // 4. build.gradle(.kts) check
        val buildFiles = projectDir.walkTopDown()
            .maxDepth(2)
            .filter { it.isFile && (it.name == "build.gradle" || it.name == "build.gradle.kts") }
            .toList()

        if (buildFiles.isEmpty()) {
            updateError("Invalid project: no build.gradle(.kts) found")
            isProjectValid = false
        }
        return isProjectValid
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
        AppLogger.e(TAG) { message }
    }

    private fun clearResults() {
        AppLogger.d(TAG) { "Clearing scan results" }
        _uiState.update {
            ProjectAnalyzerUiState()
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
