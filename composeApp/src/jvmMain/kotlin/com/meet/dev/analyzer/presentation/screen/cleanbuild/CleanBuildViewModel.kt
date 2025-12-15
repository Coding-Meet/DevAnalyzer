package com.meet.dev.analyzer.presentation.screen.cleanbuild

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.core.utility.AppLogger
import com.meet.dev.analyzer.core.utility.Utils.formatElapsedTime
import com.meet.dev.analyzer.core.utility.Utils.tagName
import com.meet.dev.analyzer.data.models.project.BuildFileType
import com.meet.dev.analyzer.data.repository.cleanbuild.CleanBuildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class CleanBuildViewModel(
    private val repository: CleanBuildRepository
) : ViewModel() {
    private val TAG = tagName(javaClass)


    private val _uiState = MutableStateFlow(CleanBuildUiState())
    val uiState = _uiState.asStateFlow()

    fun handleIntent(intent: CleanBuildIntent) {
        when (intent) {
            is CleanBuildIntent.OnPathSelected -> {
                _uiState.update { it.copy(selectedPath = intent.path) }
            }

            CleanBuildIntent.OnAnalyzeProjects -> {
                scanProjects(_uiState.value.selectedPath)
            }

            is CleanBuildIntent.OnExpandChange -> {
                _uiState.update {
                    val newExpanded = if (intent.isExpanded) {
                        it.expandedProjects + intent.uniqueId
                    } else {
                        it.expandedProjects - intent.uniqueId
                    }
                    it.copy(expandedProjects = newExpanded)
                }
            }

            CleanBuildIntent.OnExpandAll -> {
                _uiState.update {
                    it.copy(expandedProjects = it.projectBuildInfoList.map { project -> project.uniqueId }
                        .toSet())
                }
            }

            CleanBuildIntent.OnCollapseAll -> {
                _uiState.update { it.copy(expandedProjects = emptySet()) }
            }

            is CleanBuildIntent.OnModuleSelectionChange -> {
                _uiState.update { state ->
                    val updatedProjects = state.projectBuildInfoList.map { project ->
                        if (project.uniqueId == intent.uniqueId) {
                            project.copy(
                                modules = project.modules.mapIndexed { index, module ->
                                    if (index == intent.moduleIndex) {
                                        module.copy(isSelected = intent.isSelected)
                                    } else module
                                }
                            )
                        } else project
                    }
                    state.copy(projectBuildInfoList = updatedProjects)
                }
            }

            is CleanBuildIntent.OnSelectAllInProject -> {
                _uiState.update { state ->
                    val updatedProjects = state.projectBuildInfoList.map { project ->
                        if (project.uniqueId == intent.uniqueId) {
                            project.copy(
                                modules = project.modules.map { module ->
                                    module.copy(isSelected = intent.isSelected)
                                }
                            )
                        } else project
                    }
                    state.copy(projectBuildInfoList = updatedProjects)
                }
            }

            CleanBuildIntent.OnSelectAllProjects -> {
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

            CleanBuildIntent.OnDeselectAllProjects -> {
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

            CleanBuildIntent.OnDeleteClicked -> {
                _uiState.update { it.copy(showConfirmDialog = true) }
            }

            CleanBuildIntent.OnConfirmDelete -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(showConfirmDialog = false, isAnalyzing = true) }
                    val selectedProjects = _uiState.value.projectBuildInfoList
                    var deletedCount = 0

                    selectedProjects.forEach { project ->
                        project.modules.filter { it.isSelected }.forEach { module ->
                            if (repository.deleteBuildFolder(module.path)) {
                                deletedCount++
                            }
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            deletionResult = "Successfully deleted $deletedCount build folder(s)!",
                            showResultDialog = true,
                        )
                    }
                }
            }

            CleanBuildIntent.OnConfirmDismissDialog -> {
                _uiState.update { it.copy(showConfirmDialog = false) }
            }

            CleanBuildIntent.OnResultDismissDialog -> {
                _uiState.update { it.copy(showResultDialog = false) }
                scanProjects(_uiState.value.selectedPath)
            }

            CleanBuildIntent.OnClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun scanProjects(rootPath: String) {
        viewModelScope.launch {

            if (!validateRootPath(rootPath)) return@launch

            val startTime = System.currentTimeMillis()

            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    scanProgress = 0f,
                    scanStatus = "Starting scan...",
                    error = null
                )
            }

            try {
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

            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error during project scan" }
                updateError(
                    e.message ?: "Unexpected error occurred while scanning projects"
                )
            }
        }
    }

    private fun validateRootPath(rootPath: String): Boolean {
        val rootDir = File(rootPath)

        val hasAtLeastOneGradleProject = rootDir.listFiles()
            ?.filter { it.isDirectory }
            ?.any { projectDir ->

                // build.gradle or build.gradle.kts
                val hasBuildGradle = projectDir.walkTopDown()
                    .any { file ->
                        file.isFile && BuildFileType.entries.any {
                            file.name == it.fileName
                        }
                    }

                // OR already built
                val hasBuildFolder = File(projectDir, "build").exists()

                hasBuildGradle || hasBuildFolder
            } ?: false

        if (!hasAtLeastOneGradleProject) {
            updateError("Selected folder does not contain any Gradle project")
            return false
        }

        return true
    }

    private fun updateError(message: String) {
        _uiState.update {
            it.copy(
                isAnalyzing = false,
                error = message,
                scanProgress = 0f,
                scanStatus = ""
            )
        }
        AppLogger.e(TAG) { message }
    }
}
