package com.meet.dev.analyzer.presentation.screen.cleanbuild

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.meet.dev.analyzer.data.models.cleanbuild.ModuleBuild
import com.meet.dev.analyzer.data.models.cleanbuild.ProjectBuildInfo
import com.meet.dev.analyzer.utility.platform.FolderFileUtils.formatSize

data class CleanBuildUiState(
    val selectedPath: String = "",
    val projectBuildInfoList: List<ProjectBuildInfo> = emptyList(),

    val isAnalyzing: Boolean = false,
    val scanProgress: Float = 0f,
    val scanStatus: String = "",
    val scanElapsedTime: String = "00:00",

    val expandedProjects: Set<String> = emptySet(),
    val isProjectSelectionExpanded: Boolean = true,

    val showConfirmDialog: Boolean = false,
    val showDeletionProgressDialog: Boolean = false,
    val deletionProgressList: List<DeletionProgress> = emptyList(),
    val isDeletionComplete: Boolean = false,
    val deletionResult: String = "",

    val error: String? = null,
) {

    val allSelected = projectBuildInfoList.all { project ->
        project.modules.all { it.isSelected }
    }

    val totalModule = projectBuildInfoList.sumOf {
        it.modules.size
    }
    val selectedModule = projectBuildInfoList.filter {
        it.selectedModules.isNotEmpty()
    }

    private val totalSizeByte = projectBuildInfoList.sumOf {
        it.sizeBytes
    }
    val totalSizeFormatted = formatSize(totalSizeByte)

    val totalSelectedCount = projectBuildInfoList.sumOf { project ->
        project.selectedModules.size
    }
    private val totalSelectedSize = projectBuildInfoList.sumOf { project ->
        project.selectedSize
    }
    val totalSelectedSizeReadable = formatSize(totalSelectedSize)

    val deletionSuccessCount = deletionProgressList.count {
        it.status == DeletionStatus.SUCCESS
    }
    val deletionFailedCount = deletionProgressList.count {
        it.status == DeletionStatus.FAILED
    }

    private val deletedSize = deletionProgressList
        .filter { it.status == DeletionStatus.SUCCESS }
        .sumOf { it.moduleBuild.sizeBytes }
    val deletedSizeReadable = formatSize(deletedSize)

}

data class DeletionProgress(
    val moduleBuild: ModuleBuild,
    val status: DeletionStatus,
    val error: String? = null,
)


enum class DeletionStatus(
    val statusText: String,
    val containerColor: @Composable () -> Color,
    val icon: ImageVector?,
    val iconTint: @Composable () -> Color
) {
    DELETING(
        statusText = "Deleting...",
        containerColor = { MaterialTheme.colorScheme.primaryContainer },
        icon = null, // Using CircularProgressIndicator instead
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