package com.meet.project.analyzer.presentation.screen.scanner.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.ColumnWeight
import com.meet.project.analyzer.data.models.scanner.Plugin
import com.meet.project.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout
import kotlinx.coroutines.launch
import java.awt.Cursor

@Composable
fun PluginsTabContent(
    plugins: List<Plugin>,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var sortColumn by rememberSaveable { mutableStateOf("name") }
    var sortAscending by rememberSaveable { mutableStateOf(true) }

    val filteredPlugins by remember(plugins, searchQuery, sortColumn, sortAscending) {
        derivedStateOf {
            val filtered = if (searchQuery.isBlank()) {
                plugins
            } else {
                plugins.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.group.contains(searchQuery, ignoreCase = true) ||
                            it.id.contains(searchQuery, ignoreCase = true) ||
                            it.module.contains(searchQuery, ignoreCase = true) ||
                            it.configuration.contains(searchQuery, ignoreCase = true) ||
                            it.version?.contains(searchQuery, ignoreCase = true) == true
                }
            }

            val sorted = when (sortColumn) {
                "name" -> filtered.sortedBy { it.name }
                "id" -> filtered.sortedBy { it.id }
                "group" -> filtered.sortedBy { it.group }
                "version" -> filtered.sortedBy { it.version }
                "configuration" -> filtered.sortedBy { it.configuration }
                "availableVersions" -> filtered.sortedBy { it.availableVersions?.versions?.size }
                "module" -> filtered.sortedBy { it.module }
                else -> filtered
            }
            if (sortAscending) sorted else sorted.reversed()
        }
    }
    // Search and Filter Section
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        "Search plugins, groups, modules, or versions... " +
                                if (searchQuery.isBlank()) {
                                    "(${plugins.size} plugins)"
                                } else {
                                    "(${filteredPlugins.size} of ${plugins.size} plugins)"
                                },
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            modifier = Modifier.pointerHoverIcon(
                                PointerIcon(
                                    Cursor.getPredefinedCursor(
                                        Cursor.HAND_CURSOR
                                    )
                                )
                            ),
                            onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

//                // Results count
//                Text(
//                    text = if (searchQuery.isBlank()) {
//                        "${plugins.size} plugins"
//                    } else {
//                        "${filteredPlugins.size} of ${plugins.size} plugins"
//                    },
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                )
        }
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberLazyListState()

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (filteredPlugins.isNotEmpty()) {
                    stickyHeader {
                        // Table Header
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                TableHeaderCell(
                                    title = "Plugin",
                                    weight = ColumnWeight.NAME.value,
                                    sortColumn = sortColumn,
                                    currentSort = "name",
                                    sortAscending = sortAscending,
                                    onSort = {
                                        if (sortColumn == "name") {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = "name"
                                            sortAscending = true
                                        }
                                    }
                                )
                                TableHeaderCell(
                                    title = "Current",
                                    weight = ColumnWeight.CURRENT.value,
                                    sortColumn = sortColumn,
                                    currentSort = "version",
                                    sortAscending = sortAscending,
                                    onSort = {
                                        if (sortColumn == "version") {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = "version"
                                            sortAscending = true
                                        }
                                    }
                                )
                                TableHeaderCell(
                                    title = "Versions",
                                    weight = ColumnWeight.VERSIONS.value,
                                    sortColumn = sortColumn,
                                    currentSort = "availableVersions",
                                    sortAscending = sortAscending,
                                    onSort = {
                                        if (sortColumn == "availableVersions") {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = "availableVersions"
                                            sortAscending = true
                                        }
                                    }
                                )
                                TableHeaderCell(
                                    title = "Configuration",
                                    weight = ColumnWeight.CONFIGURATION.value,
                                    sortColumn = sortColumn,
                                    currentSort = "configuration",
                                    sortAscending = sortAscending,
                                    onSort = {
                                        if (sortColumn == "configuration") {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = "configuration"
                                            sortAscending = true
                                        }
                                    }
                                )
                                TableHeaderCell(
                                    title = "Module",
                                    weight = ColumnWeight.MODULE.value,
                                    sortColumn = sortColumn,
                                    currentSort = "module",
                                    sortAscending = sortAscending,
                                    onSort = {
                                        if (sortColumn == "module") {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = "module"
                                            sortAscending = true
                                        }
                                    }
                                )
                            }
                        }
                    }

                    itemsIndexed(
                        items = filteredPlugins,
                        key = { _, plugin -> plugin.uniqueId }
                    ) { index, plugin ->
                        PluginTableRow(
                            index = index,
                            plugin = plugin,
                            isEven = index % 2 == 0,
                            listState = scrollState
                        )
                    }
                } else {
                    item {
                        EmptyStateCardLayout(
                            message = if (searchQuery.isBlank()) "No plugins found"
                            else "No results for \"$searchQuery\"",
                            modifier = Modifier.fillMaxWidth().padding(10.dp)
                        )
                    }
                }
            }
            VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
        }
    }
}

@Composable
fun PluginTableRow(
    index: Int,
    plugin: Plugin,
    isEven: Boolean,
    listState: LazyListState,
) {
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val backgroundColor = if (isEven) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .animateContentSize(animationSpec = spring()),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // plugin name
                Column(
                    modifier = Modifier
                        .weight(ColumnWeight.NAME.value)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        plugin.group,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        plugin.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        plugin.id,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Current version
                Box(
                    modifier = Modifier
                        .weight(ColumnWeight.CURRENT.value)
                        .padding(horizontal = 6.dp)
                ) {
                    if (plugin.version != null) {
                        Surface(
                            color = if (plugin.isAvailable) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                plugin.version,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color =
                                    if (plugin.isAvailable) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text(
                            "No data",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // Available versions (expandable)
                Box(
                    modifier = Modifier
                        .weight(ColumnWeight.VERSIONS.value)
                        .padding(horizontal = 6.dp)
                ) {
                    if (plugin.availableVersions?.versions?.isNotEmpty() == true) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                            onClick = {
                                coroutineScope.launch {
                                    isExpanded = !isExpanded
                                    listState.animateScrollToItem(index)
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${plugin.availableVersions.versions.size} versions",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    } else {
                        Text(
                            "No data",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // Configuration
                Box(
                    modifier = Modifier
                        .weight(ColumnWeight.CONFIGURATION.value)
                        .padding(horizontal = 6.dp)
                ) {
                    Surface(
                        color = getDependencyTypeColor(plugin.configuration).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            plugin.configuration,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = getDependencyTypeColor(plugin.configuration),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Module
                Box(
                    modifier = Modifier
                        .weight(ColumnWeight.MODULE.value)
                        .padding(horizontal = 6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            plugin.module,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Expanded available versions
            AvailableVersionLayout(
                color = getDependencyTypeColor(plugin.configuration),
                isExpanded = isExpanded,
                availableVersions = plugin.availableVersions,
                version = plugin.version
            )
        }
    }
}


