package com.meet.project.analyzer.presentation.screen.scanner.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.DependencyColumn
import com.meet.project.analyzer.data.models.scanner.Dependency
import com.meet.project.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.project.analyzer.presentation.components.TableBodyCell
import com.meet.project.analyzer.presentation.components.TableBodyCellChip
import com.meet.project.analyzer.presentation.components.TableBodyCellColumn
import com.meet.project.analyzer.presentation.components.TableBodyCellText
import com.meet.project.analyzer.presentation.components.TableBodyLayout
import com.meet.project.analyzer.presentation.components.TableHeaderCell
import com.meet.project.analyzer.presentation.components.TableHeaderLayout
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout
import kotlinx.coroutines.launch
import java.awt.Cursor


@Composable
fun DependenciesTabContent(
    dependencies: List<Dependency>,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortByColumn by rememberSaveable { mutableStateOf(DependencyColumn.NAME) }
    var sortAscending by rememberSaveable { mutableStateOf(true) }

    val filteredDependencies by remember(dependencies, searchQuery, sortByColumn, sortAscending) {
        derivedStateOf {
            val filtered = if (searchQuery.isBlank()) {
                dependencies
            } else {
                dependencies.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.group.contains(searchQuery, ignoreCase = true) ||
                            it.id.contains(searchQuery, ignoreCase = true) ||
                            it.module.contains(searchQuery, ignoreCase = true) ||
                            it.configuration.contains(searchQuery, ignoreCase = true) ||
                            it.version?.contains(searchQuery, ignoreCase = true) == true
                }
            }

            val sorted = when (sortByColumn) {
                DependencyColumn.NAME -> filtered.sortedBy { it.name }
                DependencyColumn.CURRENT -> filtered.sortedBy { it.version }
                DependencyColumn.VERSIONS -> filtered.sortedBy { it.availableVersions?.versions?.size }
                DependencyColumn.CONFIGURATION -> filtered.sortedBy { it.configuration }
                DependencyColumn.MODULE -> filtered.sortedBy { it.module }
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
                        "Search dependencies, modules, or versions... " +
                                if (searchQuery.isBlank()) {
                                    "(${dependencies.size} dependencies)"
                                } else {
                                    "(${filteredDependencies.size} of ${dependencies.size} dependencies)"
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
        }
        if (filteredDependencies.isNotEmpty()) {
            DependencyTableHeaderRow(
                sortByColumn = sortByColumn,
                sortAscending = sortAscending,
                onSort = {
                    if (sortByColumn == it) {
                        sortAscending = !sortAscending
                    } else {
                        sortByColumn = it
                        sortAscending = true
                    }
                }
            )
        }

        // Dependencies Table
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberLazyListState()

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (filteredDependencies.isNotEmpty()) {
                    itemsIndexed(
                        items = filteredDependencies,
                        key = { _, dependency -> dependency.uniqueId }
                    ) { index, dependency ->
                        DependencyTableBodyRow(
                            index = index,
                            dependency = dependency,
                            isEven = index % 2 == 0,
                            listState = scrollState
                        )
                    }
                } else {
                    item {
                        EmptyStateCardLayout(
                            message = if (searchQuery.isBlank()) "No dependencies found"
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
fun DependencyTableHeaderRow(
    sortByColumn: DependencyColumn,
    sortAscending: Boolean,
    onSort: (DependencyColumn) -> Unit
) {
    TableHeaderLayout {
        DependencyColumn.entries.forEach { dependencyColumn ->
            TableHeaderCell(
                title = dependencyColumn.title,
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
fun DependencyTableBodyRow(
    index: Int,
    dependency: Dependency,
    isEven: Boolean,
    listState: LazyListState,
) {
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    TableBodyLayout(
        isEven = isEven,
        innerContent = {
            // Dependency name
            TableBodyCell(weight = DependencyColumn.NAME.weight) {
                TableBodyCellColumn(
                    primaryText = dependency.name,
                    secondaryText = dependency.group,
                    tertiaryText = dependency.versionName
                )
            }

            // Current version
            TableBodyCell(weight = DependencyColumn.CURRENT.weight) {
                if (dependency.version != null) {
                    TableBodyCellChip(
                        text = dependency.version,
                        backgroundColor = if (dependency.isAvailable) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (dependency.isAvailable) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                } else {
                    TableBodyCellText(text = "No data")
                }
            }

            // Available versions (expandable)
            TableBodyCell(weight = DependencyColumn.VERSIONS.weight) {
                if (dependency.availableVersions?.versions?.isNotEmpty() == true) {
                    TableBodyCellChip(
                        text = "${dependency.availableVersions.versions.size} versions",
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

            // Configuration
            TableBodyCell(weight = DependencyColumn.CONFIGURATION.weight) {
                TableBodyCellChip(
                    text = dependency.configuration,
                    backgroundColor = getDependencyTypeColor(dependency.configuration).copy(
                        alpha = 0.15f
                    ),
                    contentColor = getDependencyTypeColor(dependency.configuration)
                )
            }

            // Module
            TableBodyCell(weight = DependencyColumn.MODULE.weight) {
                TableBodyCellChip(
                    text = dependency.module,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }, outerContent = {

            // Expanded available versions
            AvailableVersionLayout(
                color = getDependencyTypeColor(dependency.configuration),
                isExpanded = isExpanded,
                availableVersions = dependency.availableVersions,
                version = dependency.version
            )
        }
    )
}