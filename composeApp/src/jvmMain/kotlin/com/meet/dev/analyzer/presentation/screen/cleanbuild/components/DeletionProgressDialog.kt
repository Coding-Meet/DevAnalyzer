package com.meet.dev.analyzer.presentation.screen.cleanbuild.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.core.utility.Utils.openFile
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.presentation.screen.cleanbuild.DeletionProgress
import com.meet.dev.analyzer.presentation.screen.cleanbuild.DeletionStatus
import java.awt.Cursor

@Composable
fun DeletionProgressDialog(
    deletionProgressList: List<DeletionProgress>,
    isDeletionComplete: Boolean,
    successCount: Int,
    failedCount: Int,
    deletedSizeReadable: String,
    totalSelectedCount: Int,
    totalSelectedSizeReadable: String,
    deletionResult: String,
    onDismiss: () -> Unit
) {
    val scrollState = rememberLazyListState()

    LaunchedEffect(deletionProgressList.size) {
        if (deletionProgressList.isNotEmpty() && !isDeletionComplete) {
            scrollState.animateScrollToItem(0)
        }
    }
    AlertDialog(
        onDismissRequest = { if (isDeletionComplete) onDismiss() },
        modifier = Modifier.width(700.dp),
        icon = {
            if (isDeletionComplete) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (failedCount > 0)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        title = {
            Text(
                if (isDeletionComplete) "Deletion Complete" else "Deleting Build Folders...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Progress summary
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDeletionComplete && failedCount == 0)
                            MaterialTheme.colorScheme.primaryContainer
                        else if (isDeletionComplete && failedCount > 0)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Progress:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${successCount + failedCount} / $totalSelectedCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = {
                                if (deletionProgressList.isEmpty()) 0f
                                else (successCount + failedCount).toFloat() / totalSelectedCount
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Size progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Deleted:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "$deletedSizeReadable / $totalSelectedSizeReadable",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Success: $successCount",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (failedCount > 0) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Failed: $failedCount",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // Show result message when complete
                        if (isDeletionComplete && deletionResult.isNotEmpty()) {
                            HorizontalDivider()
                            Text(
                                deletionResult,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (failedCount > 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress list with scrollbar
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentPadding = PaddingValues(
                            end = 12.dp // scrollbar breathing space
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = deletionProgressList,
                            key = { _, item -> item.moduleBuild.uniqueId }
                        ) { _, progress ->
                            DeletionProgressItem(progress = progress)
                        }
                    }
                    VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
                }
            }
        },
        confirmButton = {
            if (isDeletionComplete) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                ) {
                    Text("Done", style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        dismissButton = null
    )
}

@Composable
private fun DeletionProgressItem(progress: DeletionProgress) {
    val status = progress.status

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = status.containerColor()
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            if (status.icon != null) {
                Icon(
                    imageVector = status.icon,
                    contentDescription = status.statusText,
                    tint = status.iconTint(),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }

            // Project and Module info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (progress.moduleBuild.projectName.isNullOrBlank()) "/ ${progress.moduleBuild.moduleName}" else
                        "${progress.moduleBuild.projectName} / ${progress.moduleBuild.moduleName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = progress.moduleBuild.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = if (progress.status == DeletionStatus.FAILED)
                        Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                            .clickable {
                                progress.moduleBuild.path.openFile()
                            }
                    else
                        Modifier
                )
            }

            // Status and size
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = status.statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = status.iconTint(),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = progress.moduleBuild.sizeFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}