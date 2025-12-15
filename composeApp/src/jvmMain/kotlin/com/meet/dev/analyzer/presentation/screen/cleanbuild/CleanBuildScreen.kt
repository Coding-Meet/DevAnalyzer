package com.meet.dev.analyzer.presentation.screen.cleanbuild

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.meet.dev.analyzer.core.utility.Utils.formatSize
import com.meet.dev.analyzer.core.utility.Utils.openFile
import com.meet.dev.analyzer.data.models.cleanbuild.ModuleBuild
import com.meet.dev.analyzer.data.models.cleanbuild.ProjectBuildInfo
import com.meet.dev.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.dev.analyzer.presentation.components.TopAppBar
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.presentation.screen.cleanbuild.components.ProjectsSelectionSection
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor
import java.io.File


@Composable
fun CleanBuildScreen(
    parentEntry: NavBackStackEntry
) {
    val viewModel = koinViewModel<CleanBuildViewModel>(
        viewModelStoreOwner = parentEntry
    )
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Directory picker launcher
    val directoryPickerLauncher = rememberDirectoryPickerLauncher(
        directory = PlatformFile(File(System.getProperty("user.home"), "AndroidStudioProjects"))
    ) { directory ->
        if (directory != null) {
            coroutineScope.launch {
                val path = directory.path
                viewModel.handleIntent(CleanBuildIntent.OnPathSelected(path))
            }
        }
    }

    CleanBuildScreenContent(
        uiState = uiState,
        onBrowseClick = {
            directoryPickerLauncher.launch()
        },
        onEvent = viewModel::handleIntent
    )
}

@Composable
fun CleanBuildScreenContent(
    uiState: CleanBuildUiState,
    onBrowseClick: () -> Unit,
    onEvent: (CleanBuildIntent) -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }

    val scrollState = rememberLazyListState()

    val totalSelectedCount = uiState.projectBuildInfoList.sumOf { it.selectedModules.size }
    val totalSelectedSize = uiState.projectBuildInfoList.sumOf { it.selectedSize }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Clean Build",
                icon = Icons.Default.Delete,
                actions = {
                    if (uiState.projectBuildInfoList.isNotEmpty()) {
                        // un-expend/expend project selection visible or gone
                        IconButton(
                            modifier = Modifier.pointerHoverIcon(
                                PointerIcon(
                                    Cursor.getPredefinedCursor(
                                        Cursor.HAND_CURSOR
                                    )
                                )
                            ),
                            onClick = {
                                isExpanded = !isExpanded
                            },
                        ) {
                            Icon(
                                imageVector = if (isExpanded)
                                    Icons.Default.KeyboardArrowUp
                                else
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded)
                                    "Collapse"
                                else
                                    "Expand"
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.projectBuildInfoList.isNotEmpty() && totalSelectedCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + scaleIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + scaleOut()
            ) {
                FloatingActionButton(
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                            )
                        )
                    ),
                    onClick = { onEvent(CleanBuildIntent.OnDeleteClicked) },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                        Column {
                            Text(
                                "Delete ($totalSelectedCount)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatSize(totalSelectedSize),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Project Selection Section
            ProjectsSelectionSection(
                isExpanded = isExpanded,
                uiState = uiState,
                onClearResults = {
                    onEvent(CleanBuildIntent.OnPathSelected(""))
                },
                onBrowseClick = onBrowseClick,
                onAnalyzeClick = {
                    onEvent(CleanBuildIntent.OnAnalyzeProjects)
                },
                onClearError = {
                    onEvent(CleanBuildIntent.OnClearError)
                }
            )

            // Projects list
            if (uiState.projectBuildInfoList.isNotEmpty()) {

                ActionsCard(
                    uiState = uiState,
                    onEvent = onEvent,
                )

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            bottom = 70.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            uiState.projectBuildInfoList,
                            key = { _, project -> project.uniqueId }
                        ) { index, project ->
                            ProjectGroupItem(
                                project = project,
                                isExpanded = uiState.expandedProjects.contains(project.uniqueId),
                                onExpandChange = { expanded ->
                                    onEvent(
                                        CleanBuildIntent.OnExpandChange(
                                            project.uniqueId,
                                            expanded
                                        )
                                    )
                                },
                                onModuleSelectionChange = { moduleIndex, selected ->
                                    onEvent(
                                        CleanBuildIntent.OnModuleSelectionChange(
                                            project.uniqueId,
                                            moduleIndex,
                                            selected
                                        )
                                    )
                                },
                                onSelectAll = { selectAll ->
                                    onEvent(
                                        CleanBuildIntent.OnSelectAllInProject(
                                            project.uniqueId,
                                            selectAll
                                        )
                                    )
                                }
                            )
                        }
                    }

                    VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
                }
            } else {
                EmptyStateCardLayout(
                    title = "No Projects Selected",
                    description = "Browse and select Android Studio projects to clean",
                    icon = Icons.Default.FolderOpen,
                    modifier = Modifier.padding(10.dp).fillMaxSize()
                )
            }
        }
    }

    // Confirmation Dialog
    if (uiState.showConfirmDialog) {
        ConfirmationDialog(
            projects = uiState.projectBuildInfoList,
            totalCount = totalSelectedCount,
            totalSize = totalSelectedSize,
            onConfirm = {
                onEvent(CleanBuildIntent.OnConfirmDelete)
            },
            onDismiss = { onEvent(CleanBuildIntent.OnConfirmDismissDialog) }
        )
    }

    // Result Dialog
    if (uiState.showResultDialog) {
        ResultDialog(
            result = uiState.deletionResult,
            onDismiss = {
                onEvent(CleanBuildIntent.OnResultDismissDialog)
            }
        )
    }
}

@Composable
private fun ActionsCard(
    uiState: CleanBuildUiState,
    onEvent: (CleanBuildIntent) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Summary info
            Column {
                Text(
                    "Found ${uiState.projectBuildInfoList.size} project(s) with ${uiState.projectBuildInfoList.sumOf { it.modules.size }} build folder(s)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Total: ${formatSize(uiState.projectBuildInfoList.sumOf { it.totalSize })}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Right side - Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand/Collapse All button
                OutlinedButton(
                    onClick = {
                        if (uiState.expandedProjects.size == uiState.projectBuildInfoList.size) {
                            onEvent(CleanBuildIntent.OnCollapseAll)
                        } else {
                            onEvent(CleanBuildIntent.OnExpandAll)
                        }
                    },
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                            )
                        )
                    ),
                ) {
                    Icon(
                        if (uiState.expandedProjects.size == uiState.projectBuildInfoList.size) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (uiState.expandedProjects.size == uiState.projectBuildInfoList.size) "Collapse All" else "Expand All")
                }

                // Select All button
                OutlinedButton(
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                            )
                        )
                    ),
                    onClick = {
                        val allSelected = uiState.projectBuildInfoList.all { project ->
                            project.modules.all { it.isSelected }
                        }
                        if (allSelected) {
                            onEvent(CleanBuildIntent.OnDeselectAllProjects)
                        } else {
                            onEvent(CleanBuildIntent.OnSelectAllProjects)
                        }
                    }
                ) {
                    val allSelected = uiState.projectBuildInfoList.all { project ->
                        project.modules.all { it.isSelected }
                    }
                    Icon(
                        if (allSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (allSelected) "Deselect All" else "Select All")
                }
            }
        }
    }
}

@Composable
fun ProjectGroupItem(
    project: ProjectBuildInfo,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onModuleSelectionChange: (Int, Boolean) -> Unit,
    onSelectAll: (Boolean) -> Unit
) {
    val allSelected = project.modules.all { it.isSelected }
    val someSelected = project.modules.any { it.isSelected } && !allSelected

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Project Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                onClick = { onExpandChange(!isExpanded) }) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand/Collapse"
                )
            }

            TriStateCheckbox(
                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                state = when {
                    allSelected -> ToggleableState.On
                    someSelected -> ToggleableState.Indeterminate
                    else -> ToggleableState.Off
                },
                onClick = { onSelectAll(!allSelected) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = project.projectPath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .clickable {
                            project.projectPath.openFile()
                        }
                )
                Text(
                    text = "${project.modules.size} module(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = project.sizeFormatted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Modules List

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 250)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 250)
            )
        ) {
            Column {
                HorizontalDivider()
                project.modules.forEachIndexed { index, module ->
                    ModuleItem(
                        module = module,
                        onSelectionChange = { selected ->
                            onModuleSelectionChange(index, selected)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ModuleItem(
    module: ModuleBuild,
    onSelectionChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { },
            enabled = false
        ) {
        }
        Checkbox(
            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
            checked = module.isSelected,
            onCheckedChange = onSelectionChange
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = module.moduleName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = module.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                    .clickable {
                        module.path.openFile()
                    }
            )
        }

        Text(
            text = module.sizeFormatted,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ConfirmationDialog(
    projects: List<ProjectBuildInfo>,
    totalCount: Int,
    totalSize: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val selectedProjects = projects.filter { it.selectedModules.isNotEmpty() }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.width(800.dp),
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "Confirm Deletion",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Summary section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Build folders to delete:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "$totalCount folders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total space to free:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatSize(totalSize),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Selected projects:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))
                BoxWithConstraints {
                    val scrollState = rememberLazyGridState()

                    LazyVerticalGrid(
                        state = scrollState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.heightIn(max = 400.dp),
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            bottom = 16.dp,
                            start = 4.dp,
                            end = 12.dp // scrollbar breathing space
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(selectedProjects) { project ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Project header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            project.projectName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    HorizontalDivider()

                                    // Total size
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Total Size:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            formatSize(project.selectedSize),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Module list
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            "Modules (${project.selectedModules.size}):",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        project.selectedModules.forEach { module ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    "• ${module.moduleName}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    module.sizeFormatted,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))

                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete All", style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            ) {
                Text("Cancel", style = MaterialTheme.typography.bodyLarge)
            }
        }
    )
}

@Composable
fun ResultDialog(
    result: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.width(500.dp),
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        },
        title = {
            Text(
                "Deletion Complete",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        result,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Build folders have been successfully removed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            ) {
                Text("Done", style = MaterialTheme.typography.bodyLarge)
            }
        }
    )
}
