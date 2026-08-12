package com.meet.dev.analyzer.presentation.screen.workspace

import com.meet.dev.analyzer.data.models.workspace.ResourceCategory
import com.meet.dev.analyzer.data.models.workspace.UnusedResourceItem

sealed interface WorkspaceIntent {
    data class OnAddPath(val path: String) : WorkspaceIntent
    data class OnRemovePath(val path: String) : WorkspaceIntent
    data object OnClearPaths : WorkspaceIntent
    data object OnAnalyzeWorkspace : WorkspaceIntent
    data class OnResourceSelectionChange(val uniqueId: String, val isSelected: Boolean) :
        WorkspaceIntent

    data class OnCategorySelectionChange(val category: ResourceCategory, val isSelected: Boolean) :
        WorkspaceIntent

    data object OnSelectAll : WorkspaceIntent
    data object OnDeselectAll : WorkspaceIntent
    data object OnDeleteClicked : WorkspaceIntent
    data object OnConfirmDelete : WorkspaceIntent
    data object OnConfirmDismissDialog : WorkspaceIntent
    data object OnResultDismissDialog : WorkspaceIntent
    data object OnClearError : WorkspaceIntent
    data class OnSearchQueryChange(val query: String) : WorkspaceIntent
    data class OnResourceFilterChange(val filter: ResourceFilter) : WorkspaceIntent
    data class OnResourceClicked(val resource: UnusedResourceItem) : WorkspaceIntent
    data class OnProjectHighlight(val projectName: String) : WorkspaceIntent
    data object OnClearHighlights : WorkspaceIntent
    data object OnToggleSelectionPanel : WorkspaceIntent
    data object OnSelectRecommended : WorkspaceIntent

    // Analytics event trackers
    data object TrackFeedbackOpened : WorkspaceIntent
    data object TrackReviewPromptShown : WorkspaceIntent
    data object TrackFeedbackCancelled : WorkspaceIntent
    data object TrackWorkspaceAnalyzerOpened : WorkspaceIntent

    // Dependency Analyzer tab intents
    data class OnWorkspaceTabChange(val tab: WorkspaceTab) : WorkspaceIntent
    data class OnDependencyTabChange(val tab: DependencyTypeTab) : WorkspaceIntent
    data class OnDependencySearchChange(val query: String) : WorkspaceIntent
    data class OnToggleConflictsOnly(val showOnly: Boolean) : WorkspaceIntent
}
