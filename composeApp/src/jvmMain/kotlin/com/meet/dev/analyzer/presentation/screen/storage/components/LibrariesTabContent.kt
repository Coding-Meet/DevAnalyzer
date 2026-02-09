package com.meet.dev.analyzer.presentation.screen.storage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.data.models.storage.GradleLibraryInfo
import com.meet.dev.analyzer.data.models.storage.GradleModulesInfo
import com.meet.dev.analyzer.presentation.components.CustomOutlinedTextField
import com.meet.dev.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.dev.analyzer.presentation.components.SummaryExpandableSectionLayout
import com.meet.dev.analyzer.presentation.components.SummaryStatItem
import com.meet.dev.analyzer.presentation.components.TableBodyCell
import com.meet.dev.analyzer.presentation.components.TableBodyCellChip
import com.meet.dev.analyzer.presentation.components.TableBodyCellColumn
import com.meet.dev.analyzer.presentation.components.TableBodyCellText
import com.meet.dev.analyzer.presentation.components.TableBodyLayout
import com.meet.dev.analyzer.presentation.components.TableHeaderCell
import com.meet.dev.analyzer.presentation.components.TableHeaderLayout
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.presentation.screen.project.components.AvailableGradleVersions
import com.meet.dev.analyzer.presentation.screen.project.components.getDependencyTypeColor
import com.meet.dev.analyzer.utility.platform.FolderFileUtils.openFile
import com.meet.dev.analyzer.utility.ui.GradleLibrary
import com.meet.dev.analyzer.utility.ui.StorageLibraryColumn
import kotlinx.coroutines.launch

@Composable
fun LibrariesTabContent(
    gradleModulesInfo: GradleModulesInfo?,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortByColumn by rememberSaveable { mutableStateOf(StorageLibraryColumn.NAME) }
    var sortAscending by rememberSaveable { mutableStateOf(true) }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val filteredDependencies by remember(
        gradleModulesInfo?.libraries,
        searchQuery,
        sortByColumn,
        sortAscending
    ) {
        derivedStateOf {
            val filtered = if (searchQuery.isBlank()) {
                gradleModulesInfo?.libraries
            } else {
                gradleModulesInfo?.libraries?.filter {
                    it.artifactId.contains(searchQuery, ignoreCase = true) ||
                            it.groupId.contains(searchQuery, ignoreCase = true) ||
                            it.versions.any { version ->
                                version.version.contains(searchQuery, ignoreCase = true)
                            }
                }
            } ?: emptyList()

            val sorted = when (sortByColumn) {
                StorageLibraryColumn.NAME -> filtered.sortedBy { it.groupId + ":" + it.artifactId }
                StorageLibraryColumn.GROUP -> filtered.sortedBy { it.groupId }
                StorageLibraryColumn.AVAILABLE_VERSIONS -> filtered.sortedBy { it.versions.size }
                StorageLibraryColumn.TOTAL_SIZE_OPEN_FILE -> filtered.sortedBy { it.totalSizeBytes }
            }
            if (sortAscending) sorted else sorted.reversed()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        if (gradleModulesInfo != null) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LibrariesSummaryCard(modules = gradleModulesInfo)
                // Search Field
                CustomOutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    onClear = { searchQuery = "" },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Default.Search,
                    labelText = "Search libraries, modules, or versions..." +
                            if (searchQuery.isBlank()) {
                                "(${gradleModulesInfo.libraries.size} libraries)"
                            } else {
                                "(${filteredDependencies.size} of ${gradleModulesInfo.libraries.size} libraries)"
                            }
                )
            }
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
        if (gradleModulesInfo != null) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (filteredDependencies.isNotEmpty()) {
                        itemsIndexed(
                            items = filteredDependencies,
                            key = { _, library -> library.uniqueId }
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
                            EmptyStateCardLayout(
                                title = "Dependencies",
                                description = if (searchQuery.isBlank()) "No dependencies found"
                                else "No results for \"$searchQuery\"",
                                icon = GradleLibrary.Libraries.icon,
                                modifier = Modifier.padding(10.dp).fillMaxWidth()
                            )
                        }
                    }
                }
                VerticalScrollBarLayout(rememberScrollbarAdapter(scrollState))

            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                EmptyStateCardLayout(
                    title = GradleLibrary.Libraries.messageTitle,
                    description = GradleLibrary.Libraries.messageDescription,
                    icon = GradleLibrary.Libraries.icon
                )
            }
        }
    }
}


@Composable
fun LibrariesSummaryCard(modules: GradleModulesInfo) {
    SummaryExpandableSectionLayout(
        expandableSection = GradleLibrary.Libraries,
    ) {
        SummaryStatItem(
            label = "Total Group",
            value = modules.groupList.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = it.totalLabel,
            value = modules.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Libraries",
            value = modules.libraries.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
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