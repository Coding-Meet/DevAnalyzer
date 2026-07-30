package com.meet.dev.analyzer.presentation.screen.workspace

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.meet.dev.analyzer.data.models.workspace.UnusedResourceItem
import com.meet.dev.analyzer.data.models.workspace.WorkspaceProjectInfo
import com.meet.dev.analyzer.utility.platform.FolderFileUtils.formatSize

data class WorkspaceUiState(
    val selectedPaths: List<String> = emptyList(),
    val projects: List<WorkspaceProjectInfo> = emptyList(),
    val unusedResources: List<UnusedResourceItem> = emptyList(),

    val searchQuery: String = "",
    val activeResources: List<UnusedResourceItem> = emptyList(),
    val resourceFilter: ResourceFilter = ResourceFilter.ALL,
    val highlightedProjectName: String? = null,

    val isAnalyzing: Boolean = false,
    val scanProgress: Float = 0f,
    val scanStatus: String = "",
    val scanElapsedTime: String = "00:00",

    val showConfirmDialog: Boolean = false,
    val showDeletionProgressDialog: Boolean = false,
    val deletionProgressList: List<WorkspaceDeletionProgress> = emptyList(),
    val isDeletionComplete: Boolean = false,
    val deletionResult: String = "",

    val error: String? = null
) {
    val allSelected: Boolean
        get() = unusedResources.isNotEmpty() && unusedResources.all { it.isSelected }

    val someSelected: Boolean
        get() = unusedResources.any { it.isSelected } && !allSelected

    val totalUnusedSizeBytes: Long
        get() = unusedResources.sumOf { it.sizeBytes }

    val totalUnusedSizeReadable: String
        get() = formatSize(totalUnusedSizeBytes)

    val selectedResources: List<UnusedResourceItem>
        get() = unusedResources.filter { it.isSelected } + activeResources.filter { it.isSelected }

    val selectedActiveCount: Int
        get() = activeResources.count { it.isSelected }

    val totalSelectedCount: Int
        get() = selectedResources.size

    val totalSelectedSizeBytes: Long
        get() = selectedResources.sumOf { it.sizeBytes }

    val totalSelectedSizeReadable: String
        get() = formatSize(totalSelectedSizeBytes)

    val deletionSuccessCount: Int
        get() = deletionProgressList.count { it.status == WorkspaceDeletionStatus.SUCCESS }

    val deletionFailedCount: Int
        get() = deletionProgressList.count { it.status == WorkspaceDeletionStatus.FAILED }

    val deletedSizeBytes: Long
        get() = deletionProgressList
            .filter { it.status == WorkspaceDeletionStatus.SUCCESS }
            .sumOf { it.resourceItem.sizeBytes }

    val deletedSizeReadable: String
        get() = formatSize(deletedSizeBytes)
}

data class WorkspaceDeletionProgress(
    val resourceItem: UnusedResourceItem,
    val status: WorkspaceDeletionStatus,
    val error: String? = null
)

enum class WorkspaceDeletionStatus(
    val statusText: String,
    val containerColor: @Composable () -> Color,
    val icon: ImageVector?,
    val iconTint: @Composable () -> Color
) {
    DELETING(
        statusText = "Deleting...",
        containerColor = { MaterialTheme.colorScheme.primaryContainer },
        icon = null,
        iconTint = { Color.Unspecified }
    ),
    SUCCESS(
        statusText = "Deleted",
        containerColor = { MaterialTheme.colorScheme.surfaceVariant },
        icon = Icons.Default.CheckCircle,
        iconTint = { MaterialTheme.colorScheme.primary }
    ),
    FAILED(
        statusText = "Failed",
        containerColor = { MaterialTheme.colorScheme.errorContainer },
        icon = Icons.Default.Error,
        iconTint = { MaterialTheme.colorScheme.error }
    )
}

enum class ResourceFilter(val label: String) {
    ALL("All"),
    UNUSED("Unused"),
    ACTIVE("Active / Protected")
}
