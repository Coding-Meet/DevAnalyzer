package com.meet.dev.analyzer.presentation.screen.cleanbuild

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.meet.dev.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.dev.analyzer.presentation.components.TopAppBar
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.presentation.screen.cleanbuild.components.ActionsCardLayout
import com.meet.dev.analyzer.presentation.screen.cleanbuild.components.ConfirmationDialog
import com.meet.dev.analyzer.presentation.screen.cleanbuild.components.DeleteFloatingActionButton
import com.meet.dev.analyzer.presentation.screen.cleanbuild.components.DeletionProgressDialog
import com.meet.dev.analyzer.presentation.screen.cleanbuild.components.ProjectGroupItemLayout
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
        onIntent = viewModel::handleIntent
    )
}

@Composable
fun CleanBuildScreenContent(
    uiState: CleanBuildUiState,
    onBrowseClick: () -> Unit,
    onIntent: (CleanBuildIntent) -> Unit,
) {
    val scrollState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Clean Build",
                icon = Icons.Default.CleaningServices,
                actions = {
                    if (uiState.projectBuildInfoList.isNotEmpty()) {
                        IconButton(
                            modifier = Modifier.pointerHoverIcon(
                                PointerIcon(
                                    Cursor.getPredefinedCursor(
                                        Cursor.HAND_CURSOR
                                    )
                                )
                            ),
                            onClick = {
                                onIntent(CleanBuildIntent.OnToggleProjectSelection)
                            }) {
                            Icon(
                                imageVector = if (uiState.isProjectSelectionExpanded)
                                    Icons.Default.KeyboardArrowUp
                                else
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = if (uiState.isProjectSelectionExpanded)
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
            val floatingVisible by remember(
                uiState.projectBuildInfoList,
                uiState.totalSelectedCount
            ) {
                derivedStateOf {
                    uiState.projectBuildInfoList.isNotEmpty() && uiState.totalSelectedCount > 0
                }
            }
            DeleteFloatingActionButton(
                visible = floatingVisible,
                selectedCount = uiState.totalSelectedCount,
                totalSelectedSizeReadable = uiState.totalSelectedSizeReadable,
                onClick = { onIntent(CleanBuildIntent.OnDeleteClicked) }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Project Selection Section
            ProjectsSelectionSection(
                isExpanded = uiState.isProjectSelectionExpanded,
                isAnalyzing = uiState.isAnalyzing,
                selectedPath = uiState.selectedPath,
                scanProgress = uiState.scanProgress,
                scanStatus = uiState.scanStatus,
                scanElapsedTime = uiState.scanElapsedTime,
                error = uiState.error,
                onClearResults = {
                    onIntent(CleanBuildIntent.OnClearError)
                },
                onBrowseClick = onBrowseClick,
                onAnalyzeClick = {
                    onIntent(CleanBuildIntent.OnAnalyzeProjects)
                },
                onClearError = {
                    onIntent(CleanBuildIntent.OnClearError)
                }
            )

            // Projects list
            if (uiState.projectBuildInfoList.isNotEmpty()) {

                ActionsCardLayout(
                    uiState = uiState,
                    onEvent = onIntent,
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
                        items(
                            uiState.projectBuildInfoList,
                            key = { project -> project.uniqueId }
                        ) { project ->
                            ProjectGroupItemLayout(
                                project = project,
                                isExpanded = uiState.expandedProjects.contains(project.uniqueId),
                                onExpandChange = { expanded ->
                                    onIntent(
                                        CleanBuildIntent.OnExpandChange(
                                            project.uniqueId,
                                            expanded
                                        )
                                    )
                                },
                                onModuleSelectionChange = { moduleIndex, selected ->
                                    onIntent(
                                        CleanBuildIntent.OnModuleSelectionChange(
                                            project.uniqueId,
                                            moduleIndex,
                                            selected
                                        )
                                    )
                                },
                                onSelectAll = { selectAll ->
                                    onIntent(
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
            selectedProjects = uiState.selectedModule,
            totalCount = uiState.totalSelectedCount,
            totalSelectedSizeReadable = uiState.totalSelectedSizeReadable,
            onConfirm = {
                onIntent(CleanBuildIntent.OnConfirmDelete)
            },
            onDismiss = { onIntent(CleanBuildIntent.OnConfirmDismissDialog) }
        )
    }
    if (uiState.showDeletionProgressDialog) {
        DeletionProgressDialog(
            deletionProgressList = uiState.deletionProgressList,
            isDeletionComplete = uiState.isDeletionComplete,
            successCount = uiState.deletionSuccessCount,
            failedCount = uiState.deletionFailedCount,
            deletedSizeReadable = uiState.deletedSizeReadable,
            totalSelectedCount = uiState.totalSelectedCount,
            totalSelectedSizeReadable = uiState.totalSelectedSizeReadable,
            deletionResult = uiState.deletionResult,
            onDismiss = {
                if (uiState.isDeletionComplete) {
                    onIntent(CleanBuildIntent.OnResultDismissDialog)
                }
            }
        )
    }
}