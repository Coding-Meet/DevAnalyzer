package com.meet.dev.analyzer.presentation.screen.workspace.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.data.models.workspace.UnusedResourceItem
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceIntent
import com.meet.dev.analyzer.utility.platform.FolderFileUtils.openFile
import java.awt.Cursor

@Composable
fun ResourceItemCard(
    resource: UnusedResourceItem,
    isActive: Boolean = false,
    onIntent: (WorkspaceIntent) -> Unit = {},
    onProjectHighlight: (String) -> Unit = {},
    onCheckedChange: (Boolean) -> Unit = {}
) {
    val isSelectedActive = resource.isSelected && isActive
    val isSelectedUnused = resource.isSelected && !isActive
    val cardShape = RoundedCornerShape(10.dp)

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        onClick = { onIntent(WorkspaceIntent.OnResourceClicked(resource)) },
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .border(
                width = if (resource.isSelected) 1.5.dp else if (isHovered) 1.dp else 1.dp,
                color = when {
                    isSelectedActive -> MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                    isSelectedUnused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    isHovered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                },
                shape = cardShape
            )
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelectedActive -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
                isSelectedUnused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 2.dp else 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = resource.isSelected,
                onCheckedChange = onCheckedChange,
                colors = if (isActive) {
                    CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.error,
                        checkmarkColor = MaterialTheme.colorScheme.onError
                    )
                } else {
                    CheckboxDefaults.colors()
                },
                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            )

            // Main content column
            Column(modifier = Modifier.weight(1f)) {
                // Row: name + lock icon  |  sizeFormatted (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = resource.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isActive) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Active / Protected",
                                tint = if (isSelectedActive) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = resource.sizeFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelectedActive) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End
                    )
                }

                // Path (clickable, clipped)
                Text(
                    text = resource.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .clickable {
                            try {
                                resource.path.openFile()
                            } catch (_: Exception) {
                            }
                        }
                )

                // "Used By" tags for active resources
                if (isActive && resource.usedByProjects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Used By:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        resource.usedByProjects.forEach { projectName ->
                            val tagShape = RoundedCornerShape(4.dp)
                            Box(
                                modifier = Modifier
                                    .clip(tagShape)
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                        tagShape
                                    )
                                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                                    .clickable { onProjectHighlight(projectName) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = projectName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
