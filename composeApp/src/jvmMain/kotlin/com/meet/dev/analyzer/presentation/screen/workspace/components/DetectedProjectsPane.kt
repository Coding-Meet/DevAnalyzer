package com.meet.dev.analyzer.presentation.screen.workspace.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.presentation.components.CustomOutlinedTextField
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceIntent
import com.meet.dev.analyzer.presentation.screen.workspace.WorkspaceUiState
import java.awt.Cursor

@Composable
fun DetectedProjectsPane(
    uiState: WorkspaceUiState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var projectSortOrder by remember { mutableStateOf(ProjectSortOrder.NAME_ASC) }
    val projectListState = rememberLazyListState()

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

    Column(
        modifier = modifier.fillMaxHeight()
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
}

enum class ProjectSortOrder {
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC
}
