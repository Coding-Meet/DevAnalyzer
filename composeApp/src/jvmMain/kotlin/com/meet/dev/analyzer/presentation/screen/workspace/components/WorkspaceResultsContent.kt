package com.meet.dev.analyzer.presentation.screen.workspace.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
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
import com.meet.dev.analyzer.data.models.workspace.ResourceGroup
import com.meet.dev.analyzer.data.models.workspace.group
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.presentation.screen.workspace.ResourceFilter
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceIntent
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceUiState
import java.awt.Cursor

@Composable
fun WorkspaceResultsContent(
    uiState: WorkspaceUiState,
    onIntent: (WorkspaceIntent) -> Unit,
    onProjectHighlight: (String) -> Unit
) {
    // Combined resource list based on active filter
    val combinedResources =
        remember(uiState.resourceFilter, uiState.unusedResources, uiState.activeResources) {
            when (uiState.resourceFilter) {
                ResourceFilter.ALL -> uiState.unusedResources + uiState.activeResources
                ResourceFilter.UNUSED -> uiState.unusedResources
                ResourceFilter.ACTIVE -> uiState.activeResources
            }
        }

    // Single collapse state map shared across all categories
    val collapsedCategories = remember { mutableStateMapOf<ResourceGroup, Boolean>() }

    // Warning visibility — computed based on visible selected items to prevent confusion across filter chips
    val hasLibraryCacheSelected = remember(combinedResources) {
        combinedResources.any { it.category == ResourceCategory.GRADLE_DEPENDENCY_CACHE && it.isSelected }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resource Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        ResourceFilter.entries.forEach { filter ->
                            val count = when (filter) {
                                ResourceFilter.ALL -> uiState.unusedResources.size + uiState.activeResources.size
                                ResourceFilter.UNUSED -> uiState.unusedResources.size
                                ResourceFilter.ACTIVE -> uiState.activeResources.size
                            }
                            val isSelected = uiState.resourceFilter == filter
                            val chipShape = RoundedCornerShape(6.dp)

                            Box(
                                modifier = Modifier
                                    .clip(chipShape)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                        shape = chipShape
                                    )
                                    .clickable { onIntent(WorkspaceIntent.OnResourceFilterChange(filter)) }
                                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${filter.label} ($count)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (uiState.resourceFilter != ResourceFilter.ACTIVE) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    WorkspaceBulkActionBar(uiState = uiState, onIntent = onIntent)
                }
            }
        }

        // Warning banners — only library cache selected warning remains
        WorkspaceWarningBanner(visible = hasLibraryCacheSelected)

        Spacer(modifier = Modifier.height(4.dp))

        val groupedResources = remember(combinedResources) {
            combinedResources.groupBy { it.category.group }
        }

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                groupedResources.forEach { (group, items) ->
                    val isCollapsed = collapsedCategories[group] == true
                    item(key = group.name) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.05f
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            val isReadOnly = uiState.resourceFilter == ResourceFilter.ACTIVE

                            val allGroupSelected = remember(items) { items.all { it.isSelected } }
                            val someGroupSelected = remember(items) { items.any { it.isSelected } && !allGroupSelected }
                            val groupSelectState = remember(allGroupSelected, someGroupSelected) {
                                when {
                                    allGroupSelected -> ToggleableState.On
                                    someGroupSelected -> ToggleableState.Indeterminate
                                    else -> ToggleableState.Off
                                }
                            }

                            CategoryHeaderItem(
                                title = group.displayName,
                                description = group.description,
                                items = items,
                                isCollapsed = isCollapsed,
                                isReadOnly = isReadOnly,
                                onToggleCollapse = {
                                    collapsedCategories[group] = !isCollapsed
                                },
                                onCheckboxClick = if (isReadOnly) null else {
                                    {
                                        items.forEach { resource ->
                                            onIntent(
                                                WorkspaceIntent.OnResourceSelectionChange(
                                                    resource.uniqueId,
                                                    !allGroupSelected
                                                )
                                            )
                                        }
                                    }
                                },
                                checkboxState = groupSelectState
                            )

                            if (!isCollapsed) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                    thickness = 1.dp
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items.forEach { resource ->
                                        val isActive =
                                            uiState.activeResources.any { it.uniqueId == resource.uniqueId }
                                        ResourceItemCard(
                                            resource = resource,
                                            isActive = isActive,
                                            onIntent = onIntent,
                                            onProjectHighlight = onProjectHighlight,
                                            onCheckedChange = { isChecked ->
                                                onIntent(
                                                    WorkspaceIntent.OnResourceSelectionChange(
                                                        resource.uniqueId,
                                                        isChecked
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(listState))
        }
    }
}
