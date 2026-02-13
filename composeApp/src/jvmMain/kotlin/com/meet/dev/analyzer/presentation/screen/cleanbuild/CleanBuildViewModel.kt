package com.meet.dev.analyzer.presentation.screen.cleanbuild

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.data.repository.cleanbuild.CleanBuildRepository
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils.formatElapsedTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import java.io.File

class CleanBuildViewModel(
    private val repository: CleanBuildRepository
) : ViewModel() {
    private val TAG = tagName(javaClass)

    private val _uiState = MutableStateFlow(CleanBuildUiState())
    val uiState = _uiState.asStateFlow()

    fun handleIntent(intent: CleanBuildIntent) {
        when (intent) {
            is CleanBuildIntent.OnPathSelected -> handlePathSelected(intent.path)
            CleanBuildIntent.OnAnalyzeProjects -> handleAnalyzeProjects()
            is CleanBuildIntent.OnExpandChange -> handleExpandChange(
                intent.uniqueId,
                intent.isExpanded
            )

            CleanBuildIntent.OnExpandAll -> handleExpandAll()
            CleanBuildIntent.OnCollapseAll -> handleCollapseAll()
            is CleanBuildIntent.OnModuleSelectionChange -> handleModuleSelectionChange(
                intent.uniqueId,
                intent.moduleIndex,
                intent.isSelected
            )

            is CleanBuildIntent.OnSelectAllInProject -> handleSelectAllInProject(
                intent.uniqueId,
                intent.isSelected
            )

            CleanBuildIntent.OnSelectAllProjects -> handleSelectAllProjects()
            CleanBuildIntent.OnDeselectAllProjects -> handleDeselectAllProjects()
            CleanBuildIntent.OnDeleteClicked -> handleDeleteClicked()
            CleanBuildIntent.OnConfirmDelete -> handleConfirmDelete()
            CleanBuildIntent.OnConfirmDismissDialog -> handleConfirmDismissDialog()
            CleanBuildIntent.OnResultDismissDialog -> handleResultDismissDialog()
            CleanBuildIntent.OnClearError -> handleClearError()
            CleanBuildIntent.OnToggleProjectSelection -> handleToggleProjectSelection()
        }
    }

    private fun handlePathSelected(path: String) {
        _uiState.update { it.copy(selectedPath = path) }
    }

    private fun handleExpandChange(uniqueId: String, isExpanded: Boolean) {
        _uiState.update {
            val newExpanded = if (isExpanded) {
                it.expandedProjects + uniqueId
            } else {
                it.expandedProjects - uniqueId
            }
            it.copy(expandedProjects = newExpanded)
        }
    }

    private fun handleExpandAll() {
        _uiState.update {
            it.copy(
                expandedProjects = it.projectBuildInfoList.map { project ->
                    project.uniqueId
                }.toSet()
            )
        }
    }

    private fun handleCollapseAll() {
        _uiState.update { it.copy(expandedProjects = emptySet()) }
    }

    private fun handleModuleSelectionChange(
        uniqueId: String,
        moduleIndex: Int,
        isSelected: Boolean
    ) {
        _uiState.update { state ->
            val updatedProjects = state.projectBuildInfoList.map { project ->
                if (project.uniqueId == uniqueId) {
                    project.copy(
                        modules = project.modules.mapIndexed { index, module ->
                            if (index == moduleIndex) {
                                module.copy(isSelected = isSelected)
                            } else module
                        }
                    )
                } else project
            }
            state.copy(projectBuildInfoList = updatedProjects)
        }
    }

    private fun handleSelectAllInProject(uniqueId: String, isSelected: Boolean) {
        _uiState.update { state ->
            val updatedProjects = state.projectBuildInfoList.map { project ->
                if (project.uniqueId == uniqueId) {
                    project.copy(
                        modules = project.modules.map { module ->
                            module.copy(isSelected = isSelected)
                        }
                    )
                } else project
            }
            state.copy(projectBuildInfoList = updatedProjects)
        }
    }

    private fun handleSelectAllProjects() {
        _uiState.update { state ->
            val updatedProjects = state.projectBuildInfoList.map { project ->
                project.copy(
                    modules = project.modules.map { module ->
                        module.copy(isSelected = true)
                    }
                )
            }
            state.copy(projectBuildInfoList = updatedProjects)
        }
    }

    private fun handleDeselectAllProjects() {
        _uiState.update { state ->
            val updatedProjects = state.projectBuildInfoList.map { project ->
                project.copy(
                    modules = project.modules.map { module ->
                        module.copy(isSelected = false)
                    }
                )
            }
            state.copy(projectBuildInfoList = updatedProjects)
        }
    }

    private fun handleDeleteClicked() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    private fun handleConfirmDelete() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showConfirmDialog = false,
                    showDeletionProgressDialog = true,
                    isDeletionComplete = false,
                    deletionProgressList = emptyList()
                )
            }

            val selectedProjects = _uiState.value.projectBuildInfoList
            var deletedCount = 0
            var failedCount = 0

            // Process deletions one by one
            selectedProjects.forEach { project ->
                project.modules.filter { it.isSelected }.forEach { module ->
                    // Add item with DELETING status at the TOP
                    val newItem = DeletionProgress(
                        moduleBuild = module,
                        status = DeletionStatus.DELETING
                    )

                    _uiState.update { state ->
                        state.copy(
                            deletionProgressList = listOf(newItem) + state.deletionProgressList
                        )
                    }

                    // Perform deletion
                    val (success, errorMessage) = repository.deleteBuildFolder(module.path)
                    // Update status to SUCCESS or FAILED
                    _uiState.update { state ->
                        state.copy(
                            deletionProgressList = state.deletionProgressList.map {
                                if (it.moduleBuild.uniqueId == module.uniqueId) {
                                    it.copy(
                                        status = if (success) DeletionStatus.SUCCESS else DeletionStatus.FAILED,
                                        error = errorMessage
                                    )
                                } else it
                            }
                        )
                    }

                    if (success) deletedCount++ else failedCount++

                    // Small delay to show progress
                    delay(100)
                }
            }

            // Mark deletion as complete
            _uiState.update {
                it.copy(
                    isDeletionComplete = true,
                    deletionResult = buildDeletionResult(deletedCount, failedCount)
                )
            }
        }
    }

    private fun buildDeletionResult(deletedCount: Int, failedCount: Int): String {
        return buildString {
            append("Successfully deleted $deletedCount build folder(s)")
            if (failedCount > 0) {
                append("\n$failedCount folder(s) failed to delete")
            }
            append("!")
        }
    }

    private fun handleConfirmDismissDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    private fun handleResultDismissDialog() {
        _uiState.update { it.copy(showDeletionProgressDialog = false) }
        handleDeselectAllProjects()
        handleAnalyzeProjects()
    }

    private fun handleClearError() {
        _uiState.update {
            it.copy(
                error = null,
                projectBuildInfoList = emptyList(),
                expandedProjects = emptySet(),
                selectedPath = "",
                deletionProgressList = emptyList(),
                deletionResult = ""
            )
        }
    }

    private fun handleToggleProjectSelection() {
        _uiState.update {
            it.copy(isProjectSelectionExpanded = !it.isProjectSelectionExpanded)
        }
    }

    private fun handleAnalyzeProjects() {
        viewModelScope.launch {
            try {
                val rootPath = _uiState.value.selectedPath
                if (!validateRootPath(rootPath)) {
                    AppLogger.e(tag = TAG) { "Invalid root path: $rootPath" }
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            error = "Invalid root path: $rootPath",
                            scanProgress = 0f,
                            scanStatus = "",
                            projectBuildInfoList = emptyList(),
                            expandedProjects = emptySet(),
                        )
                    }
                    return@launch
                }

                val startTime = System.currentTimeMillis()

                _uiState.update {
                    it.copy(
                        isAnalyzing = true,
                        scanProgress = 0f,
                        scanStatus = "Starting scan...",
                        error = null,
                        projectBuildInfoList = emptyList()
                    )
                }

                val result = repository.scanProjects(
                    rootPath = rootPath
                ) { progress, status ->
                    _uiState.update {
                        it.copy(
                            scanProgress = progress,
                            scanStatus = status,
                            scanElapsedTime = formatElapsedTime(startTime)
                        )
                    }
                }

                val expandedProjects = result.map { it.uniqueId }.toSet()

                _uiState.update {
                    it.copy(
                        projectBuildInfoList = result,
                        isAnalyzing = false,
                        scanProgress = 1f,
                        scanStatus = if (result.isEmpty()) "No Gradle projects found" else "Done",
                        expandedProjects = expandedProjects
                    )
                }

                AppLogger.i(TAG) {
                    "Clean build analysis completed successfully in ${formatElapsedTime(startTime)}"
                }

            } catch (e: IOException) {
                AppLogger.e(tag = TAG, throwable = e) { "File read error" }
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        error = "Failed to access some files",
                        scanProgress = 0f,
                        scanStatus = "",
                        projectBuildInfoList = emptyList(),
                        expandedProjects = emptySet(),
                    )
                }
            } catch (e: SecurityException) {
                AppLogger.e(tag = TAG, throwable = e) { "Permission denied" }
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        error = "Permission denied for storage access",
                        scanProgress = 0f,
                        scanStatus = "",
                        projectBuildInfoList = emptyList(),
                        expandedProjects = emptySet(),
                    )
                }
            } catch (e: Exception) {
                AppLogger.e(tag = TAG, throwable = e) { "Error analyzing project" }
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        error = "Analysis failed: ${e.message}",
                        scanProgress = 0f,
                        scanStatus = "",
                        projectBuildInfoList = emptyList(),
                        expandedProjects = emptySet(),
                    )
                }
            }
        }
    }

    private fun validateRootPath(rootPath: String): Boolean {
        val rootDir = File(rootPath)
        return rootDir.walkTopDown()
            .maxDepth(3)
            .any { it.isDirectory && it.name == "build" }
    }
}
