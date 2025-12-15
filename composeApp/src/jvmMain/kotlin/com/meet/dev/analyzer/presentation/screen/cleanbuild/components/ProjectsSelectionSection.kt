package com.meet.dev.analyzer.presentation.screen.cleanbuild.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.presentation.components.CustomOutlinedTextField
import com.meet.dev.analyzer.presentation.components.ErrorLayout
import com.meet.dev.analyzer.presentation.components.ProgressStatusLayout
import com.meet.dev.analyzer.presentation.screen.cleanbuild.CleanBuildUiState
import java.awt.Cursor

@Composable
fun ProjectsSelectionSection(
    isExpanded: Boolean,
    uiState: CleanBuildUiState,
    onClearResults: () -> Unit,
    onBrowseClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onClearError: () -> Unit
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300)
        ),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 250)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 250)
        )
    ) {

        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Path input and buttons in one row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomOutlinedTextField(
                    value = uiState.selectedPath,
                    onValueChange = { },
                    onClear = onClearResults,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Folder,
                    labelText = "Enter AndroidStudioProjects Folder Path",
                    readOnly = true,
                    placeholder = { Text("Ex: /Users/meet/AndroidStudioProjects") },
                )

                Button(
                    onClick = onBrowseClick,
                    enabled = !uiState.isAnalyzing,
                    modifier = Modifier
                        .height(56.dp)
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse")
                }

                Button(
                    onClick = onAnalyzeClick,
                    enabled = uiState.selectedPath.isNotEmpty() && !uiState.isAnalyzing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                ) {
                    if (uiState.isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isAnalyzing) "Analyzing..." else "Analyze")
                }
            }

            // Progress and status
            ProgressStatusLayout(
                isScanning = uiState.isAnalyzing,
                scanProgress = uiState.scanProgress,
                scanStatus = uiState.scanStatus,
                scanElapsedTime = uiState.scanElapsedTime
            )

            // Error display
            ErrorLayout(error = uiState.error, onClearError = onClearError)

        }
    }
}
