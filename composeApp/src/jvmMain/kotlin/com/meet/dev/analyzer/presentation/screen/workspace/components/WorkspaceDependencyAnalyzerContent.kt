package com.meet.dev.analyzer.presentation.screen.workspace.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.data.models.workspace.WorkspaceDependencyInfo
import com.meet.dev.analyzer.data.models.workspace.WorkspacePluginInfo
import com.meet.dev.analyzer.presentation.components.CustomOutlinedTextField
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.presentation.screen.workspace.DependencyTypeTab
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceIntent
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceUiState
import java.awt.Cursor

@Composable
fun WorkspaceDependencyAnalyzerContent(
    uiState: WorkspaceUiState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter and search Libraries
    val filteredDeps = remember(uiState.workspaceDependencies, uiState.dependencySearchQuery, uiState.showConflictsOnly) {
        uiState.workspaceDependencies.filter { dep ->
            val matchesQuery = dep.id.contains(uiState.dependencySearchQuery, ignoreCase = true) ||
                    dep.group.contains(uiState.dependencySearchQuery, ignoreCase = true) ||
                    dep.artifact.contains(uiState.dependencySearchQuery, ignoreCase = true)
            val matchesConflict = !uiState.showConflictsOnly || dep.versionsInUse.size > 1
            matchesQuery && matchesConflict
        }
    }

    // Filter and search Plugins
    val filteredPlugins = remember(uiState.workspacePlugins, uiState.dependencySearchQuery, uiState.showConflictsOnly) {
        uiState.workspacePlugins.filter { plugin ->
            val matchesQuery = plugin.id.contains(uiState.dependencySearchQuery, ignoreCase = true)
            val matchesConflict = !uiState.showConflictsOnly || plugin.versionsInUse.size > 1
            matchesQuery && matchesConflict
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Conflicts Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomOutlinedTextField(
                value = uiState.dependencySearchQuery,
                onValueChange = { onIntent(WorkspaceIntent.OnDependencySearchChange(it)) },
                onClear = { onIntent(WorkspaceIntent.OnDependencySearchChange("")) },
                modifier = Modifier.weight(1f),
                leadingIcon = Icons.Default.Search,
                labelText = "Search Dependencies & Plugins",
                placeholder = { Text("Search by name, ID, or group...") }
            )

            // Conflicts Only Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                    .clickable { onIntent(WorkspaceIntent.OnToggleConflictsOnly(!uiState.showConflictsOnly)) }
                    .padding(8.dp)
            ) {
                Checkbox(
                    checked = uiState.showConflictsOnly,
                    onCheckedChange = { onIntent(WorkspaceIntent.OnToggleConflictsOnly(it)) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Show Conflicts Only",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Tabs switcher for Libraries / Plugins
        SecondaryTabRow(
            selectedTabIndex = if (uiState.dependencyTab == DependencyTypeTab.LIBRARIES) 0 else 1,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = uiState.dependencyTab == DependencyTypeTab.LIBRARIES,
                onClick = { onIntent(WorkspaceIntent.OnDependencyTabChange(DependencyTypeTab.LIBRARIES)) },
                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                text = {
                    Text(
                        "Libraries (${filteredDeps.size})",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Tab(
                selected = uiState.dependencyTab == DependencyTypeTab.PLUGINS,
                onClick = { onIntent(WorkspaceIntent.OnDependencyTabChange(DependencyTypeTab.PLUGINS)) },
                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                text = {
                    Text(
                        "Plugins (${filteredPlugins.size})",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }

        // Results Pane with Scrollbar
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val scrollState = rememberLazyListState()

            if (uiState.dependencyTab == DependencyTypeTab.LIBRARIES) {
                if (filteredDeps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (uiState.showConflictsOnly) "No version conflicts detected in libraries!" else "No libraries found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize().padding(end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredDeps, key = { it.id }) { dep ->
                            LibraryItemCard(dep = dep)
                        }
                    }
                }
            } else {
                if (filteredPlugins.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (uiState.showConflictsOnly) "No version conflicts detected in plugins!" else "No plugins found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize().padding(end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPlugins, key = { it.id }) { plugin ->
                            PluginItemCard(plugin = plugin)
                        }
                    }
                }
            }

            VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
        }
    }
}

@Composable
fun LibraryItemCard(dep: WorkspaceDependencyInfo) {
    var expanded by remember { mutableStateOf(false) }
    val hasConflict = dep.versionsInUse.size > 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (hasConflict) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (hasConflict) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outlineVariant
            )
        )
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dep.artifact,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (hasConflict) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dep.id,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasConflict) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Conflict",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Conflict: ${dep.versionsInUse.size} versions",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(dep.versionsInUse.keys.firstOrNull() ?: "") }
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = "Expand details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    dep.versionsInUse.forEach { (version, projects) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Text(
                                    text = version,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Used by ${projects.size} project${if (projects.size > 1) "s" else ""}:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    projects.forEach { prjName ->
                                        Card(
                                            colors = CardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                disabledContainerColor = Color.Transparent,
                                                disabledContentColor = Color.Transparent
                                            )
                                        ) {
                                            Text(
                                                text = prjName,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PluginItemCard(plugin: WorkspacePluginInfo) {
    var expanded by remember { mutableStateOf(false) }
    val hasConflict = plugin.versionsInUse.size > 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (hasConflict) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (hasConflict) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outlineVariant
            )
        )
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (hasConflict) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasConflict) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Conflict",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Conflict: ${plugin.versionsInUse.size} versions",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(plugin.versionsInUse.keys.firstOrNull() ?: "") }
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = "Expand details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    plugin.versionsInUse.forEach { (version, projects) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Text(
                                    text = version,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Used by ${projects.size} project${if (projects.size > 1) "s" else ""}:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    projects.forEach { prjName ->
                                        Card(
                                            colors = CardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                disabledContainerColor = Color.Transparent,
                                                disabledContentColor = Color.Transparent
                                            )
                                        ) {
                                            Text(
                                                text = prjName,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
