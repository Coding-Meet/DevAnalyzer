package com.meet.dev.analyzer.presentation.screen.workspace.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.meet.dev.analyzer.data.models.workspace.ResourceCategory
import com.meet.dev.analyzer.data.models.workspace.UnusedResourceItem
import com.meet.dev.analyzer.presentation.components.CustomToolTip
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceIntent
import com.meet.dev.analyzer.presentation.theme.DevAnalyzerTheme
import com.meet.dev.analyzer.utility.platform.FolderFileUtils.formatSize
import java.awt.Cursor

@Composable
fun CategoryHeaderItem(
    category: ResourceCategory,
    items: List<UnusedResourceItem>,
    isCollapsed: Boolean,
    isReadOnly: Boolean = false,
    onToggleCollapse: () -> Unit,
    onIntent: (WorkspaceIntent) -> Unit = {}
) {
    val shape = RoundedCornerShape(
        topStart = 10.dp,
        topEnd = 10.dp,
        bottomStart = if (isCollapsed) 10.dp else 0.dp,
        bottomEnd = if (isCollapsed) 10.dp else 0.dp
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape)
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            .clickable { onToggleCollapse() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isReadOnly) {
                val allCategorySelected = remember(items) { items.all { it.isSelected } }
                val someCategorySelected = remember(items) { items.any { it.isSelected } && !allCategorySelected }

                val categorySelectState = remember(allCategorySelected, someCategorySelected) {
                    when {
                        allCategorySelected -> ToggleableState.On
                        someCategorySelected -> ToggleableState.Indeterminate
                        else -> ToggleableState.Off
                    }
                }

                TriStateCheckbox(
                    state = categorySelectState,
                    onClick = {
                        onIntent(WorkspaceIntent.OnCategorySelectionChange(category, !allCategorySelected))
                    },
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                )
            }
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            CustomToolTip(
                title = category.displayName,
                description = category.description
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val totalCatSize = formatSize(items.sumOf { it.sizeBytes })
            Text(
                text = "${items.size} items ($totalCatSize)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = if (isCollapsed) Icons.Default.ChevronRight else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isCollapsed) "Expand" else "Collapse",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

