package com.meet.dev.analyzer.presentation.screen.setting

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class PathUiState(
    val sdkPath: String = "",
    val gradleHomePath: String = "",
    val avdLocationPath: String = "",
    val androidFolderPath: String = "",
    val konanFolderPath: String = "",

    val sdkPathStatus: PathStatus = PathStatus.UNCHECKED,
    val gradleHomePathStatus: PathStatus = PathStatus.UNCHECKED,
    val avdLocationPathStatus: PathStatus = PathStatus.UNCHECKED,
    val androidFolderPathStatus: PathStatus = PathStatus.UNCHECKED,
    val konanFolderPathStatus: PathStatus = PathStatus.UNCHECKED,

    val jdkPath1: String = "",
    val jdkPath2: String = "",
    val jdkPath3: String = "",

    val ideJetBrains1: String = "",
    val ideJetBrains2: String = "",
    val ideJetBrains3: String = "",

    val ideGoogle1: String = "",
    val ideGoogle2: String = "",
    val ideGoogle3: String = "",

    val jdkPath1Status: PathStatus = PathStatus.UNCHECKED,
    val jdkPath2Status: PathStatus = PathStatus.UNCHECKED,
    val jdkPath3Status: PathStatus = PathStatus.UNCHECKED,

    val ideJetBrains1Status: PathStatus = PathStatus.UNCHECKED,
    val ideJetBrains2Status: PathStatus = PathStatus.UNCHECKED,
    val ideJetBrains3Status: PathStatus = PathStatus.UNCHECKED,

    val ideGoogle1Status: PathStatus = PathStatus.UNCHECKED,
    val ideGoogle2Status: PathStatus = PathStatus.UNCHECKED,
    val ideGoogle3Status: PathStatus = PathStatus.UNCHECKED,
)

enum class PathStatus(
    val imageVector: ImageVector,
    val message: String,
    val tint: @Composable () -> Color,
    val containerColor: @Composable () -> Color
) {
    UNCHECKED(
        imageVector = Icons.AutoMirrored.Filled.Help,
        message = "Path has not been checked",
        tint = { MaterialTheme.colorScheme.onSurfaceVariant },
        containerColor = { MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) }
    ),
    VALID(
        imageVector = Icons.Default.CheckCircle,
        message = "Path is valid and accessible",
        tint = { MaterialTheme.colorScheme.primary },
        containerColor = { MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) }
    ),
    INVALID(
        imageVector = Icons.Default.Error,
        message = "Path does not exist or is not accessible",
        tint = { MaterialTheme.colorScheme.error },
        containerColor = { MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) }
    ),
}

