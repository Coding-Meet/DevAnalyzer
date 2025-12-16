package com.meet.dev.analyzer.presentation.screen.cleanbuild

sealed interface CleanBuildIntent {
    data class OnPathSelected(val path: String) : CleanBuildIntent
    data object OnAnalyzeProjects : CleanBuildIntent
    data class OnExpandChange(val uniqueId: String, val isExpanded: Boolean) : CleanBuildIntent
    data object OnExpandAll : CleanBuildIntent
    data object OnCollapseAll : CleanBuildIntent
    data class OnModuleSelectionChange(
        val uniqueId: String,
        val moduleIndex: Int,
        val isSelected: Boolean
    ) : CleanBuildIntent
    data class OnSelectAllInProject(val uniqueId: String, val isSelected: Boolean) :
        CleanBuildIntent
    data object OnSelectAllProjects : CleanBuildIntent
    data object OnDeselectAllProjects : CleanBuildIntent
    data object OnDeleteClicked : CleanBuildIntent
    data object OnConfirmDelete : CleanBuildIntent
    data object OnConfirmDismissDialog : CleanBuildIntent
    data object OnResultDismissDialog : CleanBuildIntent
    data object OnClearError : CleanBuildIntent
    data object OnToggleProjectSelection : CleanBuildIntent
}
