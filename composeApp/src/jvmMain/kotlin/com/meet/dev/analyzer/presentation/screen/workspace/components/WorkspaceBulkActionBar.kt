package com.meet.dev.analyzer.presentation.screen.workspace.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.presentation.components.CustomToolTip
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceIntent
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceUiState
import java.awt.Cursor

@Composable
fun WorkspaceBulkActionBar(
    uiState: WorkspaceUiState,
    onIntent: (WorkspaceIntent) -> Unit
) {
    // Simplified, low-profile row instead of a heavy Card container
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        if (uiState.allSelected) {
                            onIntent(WorkspaceIntent.OnDeselectAll)
                        } else {
                            onIntent(WorkspaceIntent.OnSelectAll)
                        }
                    }
                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val selectState = remember(uiState.allSelected, uiState.someSelected) {
                    when {
                        uiState.allSelected -> ToggleableState.On
                        uiState.someSelected -> ToggleableState.Indeterminate
                        else -> ToggleableState.Off
                    }
                }
                TriStateCheckbox(
                    state = selectState,
                    onClick = null // Row handles clicking
                )
                Text(
                    text = if (uiState.allSelected) "Deselect All" else "Select All",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Select Safe Resources Row (Pill + Legend Tooltip)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .clickable { onIntent(WorkspaceIntent.OnSelectRecommended) }
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select Safe Resources",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                CustomToolTip(
                    title = "Safety Tier Legend",
                    description = "• Recommended: Zero impact. Temporary files and Gradle daemon logs can be safely removed.\n" +
                            "• Safe: Unused caches and development resources can be removed. Required files will be re-downloaded or rebuilt when needed.\n" +
                            "• Caution: Removes resources that may affect your development environment, such as Gradle JDKs, Android system images, and AVDs."
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Safety Tier Info",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Potential Savings: ${uiState.totalUnusedSizeReadable}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uiState.totalSelectedCount > 0) {
                Text(
                    text = "Selected: ${uiState.totalSelectedSizeReadable} (${uiState.totalSelectedCount} items)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
