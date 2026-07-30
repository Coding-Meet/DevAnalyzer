package com.meet.dev.analyzer.presentation.screen.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.data.models.workspace.ResourceCategory
import com.meet.dev.analyzer.data.models.workspace.UnusedResourceItem
import com.meet.dev.analyzer.data.repository.workspace.WorkspaceRepository
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils.formatElapsedTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class WorkspaceViewModel(
    private val repository: WorkspaceRepository,
    private val pathPreferenceManger: PathPreferenceManger
) : ViewModel() {

    private val TAG = tagName(javaClass)

    private val _uiState = MutableStateFlow(WorkspaceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pathPreferenceManger.recentWorkspaces.collect { paths ->
                _uiState.update { it.copy(selectedPaths = paths) }
            }
        }
    }

    fun handleIntent(intent: WorkspaceIntent) {
        when (intent) {
            is WorkspaceIntent.OnAddPath -> {
                viewModelScope.launch {
                    pathPreferenceManger.saveRecentWorkspace(intent.path)
                }
            }
            is WorkspaceIntent.OnRemovePath -> {
                viewModelScope.launch {
                    pathPreferenceManger.removeRecentWorkspace(intent.path)
                }
            }
            WorkspaceIntent.OnClearPaths -> {
                viewModelScope.launch {
                    pathPreferenceManger.clearRecentWorkspaces()
                }
            }
            WorkspaceIntent.OnAnalyzeWorkspace -> handleAnalyzeWorkspace()
            is WorkspaceIntent.OnResourceSelectionChange -> handleResourceSelectionChange(intent.uniqueId, intent.isSelected)
            is WorkspaceIntent.OnCategorySelectionChange -> handleCategorySelectionChange(intent.category, intent.isSelected)
            WorkspaceIntent.OnSelectAll -> handleSelectAll()
            WorkspaceIntent.OnDeselectAll -> handleDeselectAll()
            WorkspaceIntent.OnDeleteClicked -> handleDeleteClicked()
            WorkspaceIntent.OnConfirmDelete -> handleConfirmDelete()
            WorkspaceIntent.OnConfirmDismissDialog -> handleConfirmDismissDialog()
            WorkspaceIntent.OnResultDismissDialog -> handleResultDismissDialog()
            WorkspaceIntent.OnClearError -> handleClearError()
            is WorkspaceIntent.OnSearchQueryChange -> _uiState.update { it.copy(searchQuery = intent.query) }
            is WorkspaceIntent.OnResourceFilterChange -> _uiState.update { it.copy(resourceFilter = intent.filter, highlightedProjectName = null) }
            is WorkspaceIntent.OnResourceClicked -> {
                handleResourceSelectionChange(intent.resource.uniqueId, !intent.resource.isSelected)
            }
            is WorkspaceIntent.OnProjectHighlight -> _uiState.update { it.copy(highlightedProjectName = intent.projectName) }
            WorkspaceIntent.OnClearHighlights -> _uiState.update { it.copy(highlightedProjectName = null) }
        }
    }

    private fun handleAnalyzeWorkspace() {
        val rootPaths = _uiState.value.selectedPaths
        if (rootPaths.isEmpty()) {
            _uiState.update { it.copy(error = "Please select or add at least one workspace folder path first.") }
            return
        }

        // Validate all paths are actual directories
        val invalidPath = rootPaths.firstOrNull { path ->
            val rootDir = File(path)
            !rootDir.exists() || !rootDir.isDirectory
        }
        if (invalidPath != null) {
            _uiState.update { it.copy(error = "Path does not exist or is not a directory: $invalidPath") }
            return
        }

        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                _uiState.update {
                    it.copy(
                        isAnalyzing = true,
                        scanProgress = 0f,
                        scanStatus = "Loading configurations...",
                        error = null,
                        projects = emptyList(),
                        unusedResources = emptyList(),
                        activeResources = emptyList(),
                        searchQuery = "",
                        resourceFilter = ResourceFilter.ALL,
                        highlightedProjectName = null
                    )
                }

                // Retrieve settings paths
                val sdkPath = pathPreferenceManger.sdkPath.first()
                val gradleHome = pathPreferenceManger.gradleUserHomePath.first()
                val konanPath = pathPreferenceManger.konanFolderPath.first()

                val result = repository.analyzeWorkspace(
                    workspacePaths = rootPaths,
                    sdkPath = sdkPath,
                    gradleHomePath = gradleHome,
                    konanPath = konanPath
                ) { progress, status ->
                    _uiState.update {
                        it.copy(
                            scanProgress = progress,
                            scanStatus = status,
                            scanElapsedTime = formatElapsedTime(startTime)
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        projects = result.projects,
                        unusedResources = result.unusedResources,
                        activeResources = result.activeResources,
                        scanProgress = 1f,
                        scanStatus = if (result.unusedResources.isEmpty()) "No unused resources found" else "Scan completed"
                    )
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error performing workspace analysis" }
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        error = "Analysis failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private fun handleResourceSelectionChange(uniqueId: String, isSelected: Boolean) {
        _uiState.update { state ->
            val updatedUnused = state.unusedResources.map { item ->
                if (item.uniqueId == uniqueId) item.copy(isSelected = isSelected) else item
            }
            val updatedActive = state.activeResources.map { item ->
                if (item.uniqueId == uniqueId) item.copy(isSelected = isSelected) else item
            }
            state.copy(unusedResources = updatedUnused, activeResources = updatedActive)
        }
    }

    private fun handleCategorySelectionChange(category: ResourceCategory, isSelected: Boolean) {
        _uiState.update { state ->
            val updatedUnused = state.unusedResources.map { item ->
                if (item.category == category) item.copy(isSelected = isSelected) else item
            }
            val updatedActive = state.activeResources.map { item ->
                if (item.category == category) item.copy(isSelected = isSelected) else item
            }
            state.copy(unusedResources = updatedUnused, activeResources = updatedActive)
        }
    }

    private fun handleSelectAll() {
        _uiState.update { state ->
            state.copy(
                unusedResources = state.unusedResources.map { it.copy(isSelected = true) }
            )
        }
    }

    private fun handleDeselectAll() {
        _uiState.update { state ->
            state.copy(
                unusedResources = state.unusedResources.map { it.copy(isSelected = false) },
                activeResources = state.activeResources.map { it.copy(isSelected = false) }
            )
        }
    }

    private fun handleDeleteClicked() {
        if (_uiState.value.totalSelectedCount == 0) return
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    private fun handleConfirmDelete() {
        viewModelScope.launch {
            val selected = _uiState.value.selectedResources
            if (selected.isEmpty()) return@launch

            _uiState.update {
                it.copy(
                    showConfirmDialog = false,
                    showDeletionProgressDialog = true,
                    isDeletionComplete = false,
                    deletionProgressList = emptyList()
                )
            }

            var deletedCount = 0
            var failedCount = 0

            selected.forEach { resource ->
                val progressItem = WorkspaceDeletionProgress(
                    resourceItem = resource,
                    status = WorkspaceDeletionStatus.DELETING
                )
                _uiState.update { state ->
                    state.copy(deletionProgressList = listOf(progressItem) + state.deletionProgressList)
                }

                val (success, errorMsg) = repository.deleteResource(resource.path)

                _uiState.update { state ->
                    state.copy(
                        deletionProgressList = state.deletionProgressList.map {
                            if (it.resourceItem.uniqueId == resource.uniqueId) {
                                it.copy(
                                    status = if (success) WorkspaceDeletionStatus.SUCCESS else WorkspaceDeletionStatus.FAILED,
                                    error = errorMsg
                                )
                            } else it
                        }
                    )
                }

                if (success) deletedCount++ else failedCount++
                delay(150) // visual feedback delay
            }

            _uiState.update {
                it.copy(
                    isDeletionComplete = true,
                    deletionResult = buildString {
                        append("Deleted $deletedCount resource(s) successfully.")
                        if (failedCount > 0) {
                            append("\nFailed to delete $failedCount resource(s). Check logs for details.")
                        }
                    }
                )
            }
        }
    }

    private fun handleConfirmDismissDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    private fun handleResultDismissDialog() {
        _uiState.update { it.copy(showDeletionProgressDialog = false) }
        handleDeselectAll()
        handleAnalyzeWorkspace()
    }

    private fun handleClearError() {
        _uiState.update {
            it.copy(
                error = null,
                projects = emptyList(),
                unusedResources = emptyList(),
                activeResources = emptyList(),
                searchQuery = "",
                resourceFilter = ResourceFilter.ALL,
                selectedPaths = emptyList(),
                highlightedProjectName = null
            )
        }
        viewModelScope.launch {
            pathPreferenceManger.clearRecentWorkspaces()
        }
    }
}
