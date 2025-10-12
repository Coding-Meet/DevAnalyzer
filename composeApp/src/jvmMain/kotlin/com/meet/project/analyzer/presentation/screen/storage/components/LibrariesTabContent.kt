package com.meet.project.analyzer.presentation.screen.storage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.StorageLibraryColumn
import com.meet.project.analyzer.core.utility.Utils.openFile
import com.meet.project.analyzer.data.models.storage.GradleLibraryInfo
import com.meet.project.analyzer.data.models.storage.GradleModulesInfo
import com.meet.project.analyzer.presentation.components.CustomOutlinedTextField
import com.meet.project.analyzer.presentation.components.EmptyStateCard
import com.meet.project.analyzer.presentation.components.SummaryStatItem
import com.meet.project.analyzer.presentation.components.TableBodyCell
import com.meet.project.analyzer.presentation.components.TableBodyCellChip
import com.meet.project.analyzer.presentation.components.TableBodyCellColumn
import com.meet.project.analyzer.presentation.components.TableBodyCellText
import com.meet.project.analyzer.presentation.components.TableBodyLayout
import com.meet.project.analyzer.presentation.components.TableHeaderCell
import com.meet.project.analyzer.presentation.components.TableHeaderLayout
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.project.analyzer.presentation.screen.scanner.components.AvailableGradleVersions
import com.meet.project.analyzer.presentation.screen.scanner.components.getDependencyTypeColor
import com.meet.project.analyzer.presentation.screen.storage.StorageAnalyzerUiState
import kotlinx.coroutines.launch

@Composable
fun LibrariesTabContent(
    uiState: StorageAnalyzerUiState,
    onLoadGradleModules: () -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortByColumn by rememberSaveable { mutableStateOf(StorageLibraryColumn.NAME) }
    var sortAscending by rememberSaveable { mutableStateOf(true) }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val filteredDependencies by remember(
        uiState.gradleModulesInfo?.libraries,
        searchQuery,
        sortByColumn,
        sortAscending
    ) {
        derivedStateOf {
            val filtered = if (searchQuery.isBlank()) {
                uiState.gradleModulesInfo?.libraries ?: emptyList()
            } else {
                uiState.gradleModulesInfo?.libraries?.filter {
                    it.artifactId.contains(searchQuery, ignoreCase = true) ||
                            it.groupId.contains(searchQuery, ignoreCase = true) ||
                            it.versions.any { version ->
                                version.version.contains(searchQuery, ignoreCase = true)
                            }
                } ?: emptyList()
            }

            val sorted = when (sortByColumn) {
                StorageLibraryColumn.NAME -> filtered.sortedBy { it.groupId + ":" + it.artifactId }
                StorageLibraryColumn.GROUP -> filtered.sortedBy { it.groupId }
                StorageLibraryColumn.AVAILABLE_VERSIONS -> filtered.sortedBy { it.versions.size }
                StorageLibraryColumn.TOTAL_SIZE_OPEN_FILE -> filtered.sortedBy { it.totalSizeBytes }
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
            if (uiState.gradleModulesInfo != null) {
                LibrariesSummaryCard(uiState.gradleModulesInfo)
            }
            // Search Field
            CustomOutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onClear = { searchQuery = "" },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Search,
                labelText = "Search libraries, modules, or versions..." +
                        if (searchQuery.isBlank()) {
                            "(${uiState.gradleModulesInfo?.libraries?.size ?: 0} libraries)"
                        } else {
                            "(${filteredDependencies.size} of ${uiState.gradleModulesInfo?.libraries?.size ?: 0} libraries)"
                        }
            )
        }
        if (filteredDependencies.isNotEmpty()) {
            LibraryTableHeaderRow(
                sortByColumn = sortByColumn,
                sortAscending = sortAscending,
                onSort = {
                    if (sortByColumn == it) {
                        sortAscending = !sortAscending
                    } else {
                        sortByColumn = it
                        sortAscending = true
                    }
                    coroutineScope.launch {
                        scrollState.animateScrollToItem(0)
                    }
                }
            )
        }
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (filteredDependencies.isNotEmpty()) {
                    itemsIndexed(
                        items = filteredDependencies,
//                        key = { _, library -> library }
                    ) { index, library ->
                        LibraryTableBodyRow(
                            index = index,
                            dependency = library,
                            isEven = index % 2 == 0,
                            listState = scrollState
                        )
                    }
                } else {
                    item {
                        EmptyStateCard(
                            title = "No Libraries Found",
                            description = "No Gradle modules information available",
                            icon = Icons.AutoMirrored.Filled.LibraryBooks,
                            actionText = "Load Libraries",
                            onAction = onLoadGradleModules
                        )
                    }
                }
            }
            VerticalScrollBarLayout(rememberScrollbarAdapter(scrollState))

        }
    }
}


@Composable
fun LibrariesSummaryCard(modules: GradleModulesInfo) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.LibraryBooks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    "Gradle Libraries",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    "Total Group",
                    modules.libraries.distinctBy { it.groupId }.size.toString()
                )
                SummaryStatItem("Cache Size", modules.sizeReadable)
                SummaryStatItem("Total Libraries", modules.libraries.size.toString())
            }
        }
    }
}

@Composable
fun LibraryTableHeaderRow(
    sortByColumn: StorageLibraryColumn,
    sortAscending: Boolean,
    onSort: (StorageLibraryColumn) -> Unit
) {
    TableHeaderLayout {
        StorageLibraryColumn.entries.forEach { dependencyColumn ->
            TableHeaderCell(
                title = dependencyColumn.title,
                description = dependencyColumn.description,
                weight = dependencyColumn.weight,
                isSelected = sortByColumn == dependencyColumn,
                sortAscending = sortAscending,
                onSort = {
                    onSort(dependencyColumn)
                }
            )
        }
    }
}


@Composable
fun LibraryTableBodyRow(
    index: Int,
    dependency: GradleLibraryInfo,
    isEven: Boolean,
    listState: LazyListState,
) {
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    TableBodyLayout(
        isEven = isEven,
        innerContent = {
            // Dependency name, group, and id
            TableBodyCell(weight = StorageLibraryColumn.NAME.weight) {
                TableBodyCellColumn(
                    primaryText = dependency.artifactId,
                    secondaryText = dependency.groupId,
                    tertiaryText = dependency.groupId + ":" + dependency.artifactId,
                )
            }

            // Group
            TableBodyCell(weight = StorageLibraryColumn.GROUP.weight) {
                TableBodyCellChip(
                    text = dependency.groupId,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }


            // Available versions (expandable)
            TableBodyCell(weight = StorageLibraryColumn.AVAILABLE_VERSIONS.weight) {
                if (dependency.versions.isNotEmpty()) {
                    TableBodyCellChip(
                        text = "${dependency.versions.size} versions",
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = {
                            coroutineScope.launch {
                                isExpanded = !isExpanded
                                listState.animateScrollToItem(index)
                            }
                        },
                        trailingContent = {
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    )
                } else {
                    TableBodyCellText(text = "No data")
                }
            }

            // Total Size
            TableBodyCell(weight = StorageLibraryColumn.TOTAL_SIZE_OPEN_FILE.weight) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TableBodyCellChip(
                        text = dependency.sizeReadable,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TableBodyCellChip(
                        text = "Open",
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = {
                            dependency.path.openFile()
                        },
                        trailingContent = {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = "Open",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    )
                }
            }

        }, outerContent = {

            // Expanded available gradle versions
            AvailableGradleVersions(
                color = getDependencyTypeColor("implementation"),
                isExpanded = isExpanded,
                gradleLibraryInfo = dependency,
                version = ""
            )
        }
    )
}

@Composable
fun LibraryInfoCard(library: GradleLibraryInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // 📚 Icon
                Icon(
                    Icons.AutoMirrored.Filled.LibraryBooks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 12.dp, top = 2.dp)
                        .size(22.dp)
                )

                // Library Info (artifact + group)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = library.artifactId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = library.groupId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 👉 Total size (moved to right side)
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = library.sizeReadable,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Small spacing
            if (library.versions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(10.dp))

                // Versions row
                Text(
                    text = "Versions (${library.versions.size}):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(library.versions.take(5)) { version ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.extraSmall,
                            tonalElevation = 1.dp
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = version.version,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = version.sizeReadable,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        }
                    }

                    // +more versions indicator
                    if (library.versions.size > 5) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "+${library.versions.size - 5} more",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}