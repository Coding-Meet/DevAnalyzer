package com.meet.dev.analyzer.presentation.screen.workspace.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.data.models.workspace.ResourceCategory
import com.meet.dev.analyzer.presentation.components.CustomOutlinedTextField
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.presentation.screen.workspace.ResourceFilter
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceIntent
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceUiState
import kotlinx.coroutines.launch
import java.awt.Cursor

@Composable
fun WorkspaceResultsContent(
    uiState: WorkspaceUiState,
    onIntent: (WorkspaceIntent) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val projectListState = rememberLazyListState()

    var projectSortOrder by remember { mutableStateOf(ProjectSortOrder.NAME_ASC) }

    val filteredProjects = remember(uiState.projects, uiState.searchQuery, projectSortOrder) {
        val filtered = uiState.projects.filter {
            it.projectName.contains(uiState.searchQuery, ignoreCase = true)
        }
        when (projectSortOrder) {
            ProjectSortOrder.NAME_ASC -> filtered.sortedBy { it.projectName.lowercase() }
            ProjectSortOrder.NAME_DESC -> filtered.sortedByDescending { it.projectName.lowercase() }
            ProjectSortOrder.SIZE_DESC -> filtered.sortedByDescending { it.totalSizeBytes }
            ProjectSortOrder.SIZE_ASC -> filtered.sortedBy { it.totalSizeBytes }
        }
    }

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
    val collapsedCategories = remember { mutableStateMapOf<ResourceCategory, Boolean>() }

    // Warning visibility — computed based on visible selected items to prevent confusion across filter chips
    val hasLibraryCacheSelected = remember(combinedResources) {
        combinedResources.any { it.category == ResourceCategory.GRADLE_DEPENDENCY_CACHE && it.isSelected }
    }
    val hasActiveSelected = remember(combinedResources, uiState.activeResources) {
        val activeIds = uiState.activeResources.map { it.uniqueId }.toSet()
        combinedResources.any { it.isSelected && it.uniqueId in activeIds }
    }

    // Direct scroll: called from project tag click (no LaunchedEffect needed)
    val onProjectHighlight: (String) -> Unit = { projectName ->
        onIntent(WorkspaceIntent.OnProjectHighlight(projectName))
        val idx = filteredProjects.indexOfFirst { it.projectName == projectName }
        if (idx != -1) {
            coroutineScope.launch { projectListState.animateScrollToItem(idx) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Left Pane: Detected Projects (40%) ──────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            ) {
                Text(
                    "Detected Projects (${uiState.projects.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                var showSortMenu by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomOutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { onIntent(WorkspaceIntent.OnSearchQueryChange(it)) },
                        onClear = { onIntent(WorkspaceIntent.OnSearchQueryChange("")) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Search,
                        labelText = "Search Projects",
                        placeholder = { Text("Search projects by name...") }
                    )

                    Box {
                        IconButton(
                            modifier = Modifier.pointerHoverIcon(
                                PointerIcon(
                                    Cursor.getPredefinedCursor(
                                        Cursor.HAND_CURSOR
                                    )
                                )
                            ),
                            onClick = { showSortMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort Projects",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Name (A-Z)") },
                                onClick = {
                                    projectSortOrder = ProjectSortOrder.NAME_ASC
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (projectSortOrder == ProjectSortOrder.NAME_ASC) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Name (Z-A)") },
                                onClick = {
                                    projectSortOrder = ProjectSortOrder.NAME_DESC
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (projectSortOrder == ProjectSortOrder.NAME_DESC) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Size (Largest)") },
                                onClick = {
                                    projectSortOrder = ProjectSortOrder.SIZE_DESC
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (projectSortOrder == ProjectSortOrder.SIZE_DESC) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Size (Smallest)") },
                                onClick = {
                                    projectSortOrder = ProjectSortOrder.SIZE_ASC
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (projectSortOrder == ProjectSortOrder.SIZE_ASC) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }

                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = projectListState,
                        modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProjects) { project ->
                            val isHighlighted =
                                project.projectName == uiState.highlightedProjectName
                            ProjectInfoCard(
                                project = project,
                                isHighlighted = isHighlighted
                            )
                        }
                    }
                    VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(projectListState))
                }
            }

            // ── Right Pane: Unified resource list with filter chips (60%) ───
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            ) {
                // Filter chip row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resources:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ResourceFilter.entries.forEach { filter ->
                        val count = when (filter) {
                            ResourceFilter.ALL -> uiState.unusedResources.size + uiState.activeResources.size
                            ResourceFilter.UNUSED -> uiState.unusedResources.size
                            ResourceFilter.ACTIVE -> uiState.activeResources.size
                        }
                        FilterChip(
                            selected = uiState.resourceFilter == filter,
                            onClick = { onIntent(WorkspaceIntent.OnResourceFilterChange(filter)) },
                            label = {
                                Text(
                                    text = "${filter.label} ($count)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (uiState.resourceFilter == filter) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            elevation = FilterChipDefaults.filterChipElevation(
                                hoveredElevation = 0.dp
                            ),
                            modifier = Modifier.pointerHoverIcon(
                                PointerIcon(
                                    Cursor.getPredefinedCursor(
                                        Cursor.HAND_CURSOR
                                    )
                                )
                            )
                        )
                    }
                }

                // Warning banners — always rendered, visibility controlled by flag
                WorkspaceWarningBanner(visible = hasLibraryCacheSelected)
                WorkspaceWarningBanner(
                    visible = hasActiveSelected,
                    title = "⚠️ Danger: Active Resources Selected",
                    message = "You have selected resources that are actively used by your projects. Deleting these may break your builds, cause Gradle sync failures, or require re-downloading SDK components. Proceed only if you fully understand the impact."
                )

                // Bulk action bar (hidden when only active resources visible)
                if (uiState.resourceFilter != ResourceFilter.ACTIVE) {
                    WorkspaceBulkActionBar(uiState = uiState, onIntent = onIntent)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val groupedResources = remember(combinedResources) {
                    combinedResources.groupBy { it.category }
                }

                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        groupedResources.forEach { (category, items) ->
                            val isCollapsed = collapsedCategories[category] == true
                            item(key = category.name) {
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
                                    CategoryHeaderItem(
                                        category = category,
                                        items = items,
                                        isCollapsed = isCollapsed,
                                        isReadOnly = false,
                                        onToggleCollapse = {
                                            collapsedCategories[category] = !isCollapsed
                                        },
                                        onIntent = onIntent
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
    }
}

enum class ProjectSortOrder {
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC
}
