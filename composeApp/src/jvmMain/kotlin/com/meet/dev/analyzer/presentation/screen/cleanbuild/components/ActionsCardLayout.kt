package com.meet.dev.analyzer.presentation.screen.cleanbuild.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.presentation.screen.cleanbuild.CleanBuildIntent
import com.meet.dev.analyzer.presentation.screen.cleanbuild.CleanBuildUiState
import java.awt.Cursor

@Composable
fun ActionsCardLayout(
    uiState: CleanBuildUiState,
    onEvent: (CleanBuildIntent) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Summary info
            Column {
                Text(
                    "Found ${uiState.projectBuildInfoList.size} project(s) with ${uiState.totalModule} build folder(s)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Total: ${uiState.totalSizeFormatted}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Right side - Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand/Collapse All button
                OutlinedButton(
                    onClick = {
                        if (uiState.expandedProjects.size == uiState.projectBuildInfoList.size) {
                            onEvent(CleanBuildIntent.OnCollapseAll)
                        } else {
                            onEvent(CleanBuildIntent.OnExpandAll)
                        }
                    },
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                            )
                        )
                    ),
                ) {
                    Icon(
                        if (uiState.expandedProjects.size == uiState.projectBuildInfoList.size) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (uiState.expandedProjects.size == uiState.projectBuildInfoList.size) "Collapse All" else "Expand All")
                }

                // Select All button
                OutlinedButton(
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                            )
                        )
                    ),
                    onClick = {
                        if (uiState.allSelected) {
                            onEvent(CleanBuildIntent.OnDeselectAllProjects)
                        } else {
                            onEvent(CleanBuildIntent.OnSelectAllProjects)
                        }
                    }
                ) {
                    Icon(
                        if (uiState.allSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (uiState.allSelected) "Deselect All" else "Select All")
                }
            }
        }
    }
}

