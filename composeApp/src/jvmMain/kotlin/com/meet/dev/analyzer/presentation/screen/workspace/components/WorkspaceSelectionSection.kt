package com.meet.dev.analyzer.presentation.screen.workspace.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.presentation.components.CustomOutlinedTextField
import com.meet.dev.analyzer.presentation.components.ErrorLayout
import com.meet.dev.analyzer.presentation.components.ProgressStatusLayout
import java.awt.Cursor
import java.io.File

@Composable
fun WorkspaceSelectionSection(
    isExpanded: Boolean,
    selectedPaths: List<String>,
    isAnalyzing: Boolean,
    scanProgress: Float,
    scanStatus: String,
    scanElapsedTime: String,
    error: String?,
    onClearPaths: () -> Unit,
    onBrowseClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onClearError: () -> Unit,
    onRemovePath: (String) -> Unit
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(animationSpec = tween(durationMillis = 300)) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = shrinkVertically(animationSpec = tween(durationMillis = 250)) + fadeOut(animationSpec = tween(durationMillis = 250))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tip Banner explaining the need for combining parent workspace paths
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Tip: Add all your projects' parent folders (e.g. AndroidStudioProjects, IdeaProjects). Since Gradle cache, Kotlin native compilers, SDK platforms, SDK sources, NDKs, and CMake tools are shared globally, scanning all workspace folders together ensures safe and accurate cleanup without breaking other projects.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomOutlinedTextField(
                    value = if (selectedPaths.isEmpty()) "" else "Selected ${selectedPaths.size} workspace folder(s)",
                    onValueChange = { },
                    onClear = onClearPaths,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Folder,
                    labelText = "Workspace Folder(s) to Scan",
                    readOnly = true,
                    placeholder = { Text("Click 'Browse' to add one or more project folders...") },
                )

                Button(
                    onClick = onBrowseClick,
                    enabled = !isAnalyzing,
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
                    enabled = selectedPaths.isNotEmpty() && !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                ) {
                    if (isAnalyzing) {
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
                    Text(if (isAnalyzing) "Analyzing..." else "Analyze")
                }
            }

            // Scanned workspace paths list
            if (selectedPaths.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Active Folders:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedPaths.forEach { path ->
                            val chipShape = RoundedCornerShape(8.dp)
                            Row(
                                modifier = Modifier
                                    .clip(chipShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = chipShape
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = path.split(File.separatorChar).lastOrNull()?.takeIf { it.isNotEmpty() } ?: path,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove path",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                                        .clickable { onRemovePath(path) }
                                )
                            }
                        }
                    }
                }
            }

            ProgressStatusLayout(
                isScanning = isAnalyzing,
                scanProgress = scanProgress,
                scanStatus = scanStatus,
                scanElapsedTime = scanElapsedTime
            )

            ErrorLayout(error = error, onClearError = onClearError)
        }
    }
}
