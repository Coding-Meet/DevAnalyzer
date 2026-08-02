package com.meet.dev.analyzer.presentation.screen.workspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.meet.dev.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.dev.analyzer.presentation.components.ReviewDialog
import com.meet.dev.analyzer.presentation.components.TopAppBar
import com.meet.dev.analyzer.presentation.screen.cleanbuild.components.DeleteFloatingActionButton
import com.meet.dev.analyzer.presentation.screen.workspace.components.WorkspaceConfirmationDialog
import com.meet.dev.analyzer.presentation.screen.workspace.components.WorkspaceDeletionProgressDialog
import com.meet.dev.analyzer.presentation.screen.workspace.components.WorkspaceResultsContent
import com.meet.dev.analyzer.presentation.screen.workspace.components.WorkspaceSelectionSection
import com.meet.dev.analyzer.utility.crash_report.CustomProperties
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor
import java.io.File

@Composable
fun WorkspaceScreen(
    parentEntry: NavBackStackEntry
) {
    val viewModel = koinViewModel<WorkspaceViewModel>(
        viewModelStoreOwner = parentEntry
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val lastSubmittedVersion by uiState.lastSubmittedReviewVersion.collectAsStateWithLifecycle(initialValue = "")
    val desktopConfig = remember {
        CustomProperties.createAppConfig(CustomProperties.loadProperties())
    }
    val currentVersion = desktopConfig.version
    val showFeedbackButton = lastSubmittedVersion != currentVersion
    var showReviewDialog by remember { mutableStateOf(false) }

    var isSelectionPanelExpanded by remember { mutableStateOf(true) }

    val directoryPickerLauncher = rememberDirectoryPickerLauncher(
        directory = PlatformFile(File(System.getProperty("user.home"), "AndroidStudioProjects"))
    ) { directory ->
        if (directory != null) {
            coroutineScope.launch {
                viewModel.handleIntent(WorkspaceIntent.OnAddPath(directory.path))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Workspace Analyzer",
                icon = Icons.Default.Workspaces,
                actions = {
                    if (uiState.projects.isNotEmpty() || uiState.unusedResources.isNotEmpty() || uiState.activeResources.isNotEmpty()) {
                        IconButton(
                            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                            onClick = { isSelectionPanelExpanded = !isSelectionPanelExpanded }
                        ) {
                            Icon(
                                imageVector = if (isSelectionPanelExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isSelectionPanelExpanded) "Collapse" else "Expand"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val visibleSelectedCount by remember(uiState.resourceFilter, uiState.unusedResources, uiState.activeResources) {
                derivedStateOf {
                    val combined = when (uiState.resourceFilter) {
                        ResourceFilter.ALL -> uiState.unusedResources + uiState.activeResources
                        ResourceFilter.UNUSED -> uiState.unusedResources
                        ResourceFilter.ACTIVE -> uiState.activeResources
                    }
                    combined.count { it.isSelected }
                }
            }
            val visibleSelectedSizeReadable by remember(uiState.resourceFilter, uiState.unusedResources, uiState.activeResources) {
                derivedStateOf {
                    val combined = when (uiState.resourceFilter) {
                        ResourceFilter.ALL -> uiState.unusedResources + uiState.activeResources
                        ResourceFilter.UNUSED -> uiState.unusedResources
                        ResourceFilter.ACTIVE -> uiState.activeResources
                    }
                    val size = combined.filter { it.isSelected }.sumOf { it.sizeBytes }
                    FolderFileUtils.formatSize(size)
                }
            }
            val isFabVisible by remember(visibleSelectedCount) {
                derivedStateOf { visibleSelectedCount > 0 }
            }

            DeleteFloatingActionButton(
                visible = isFabVisible,
                selectedCount = visibleSelectedCount,
                totalSelectedSizeReadable = visibleSelectedSizeReadable,
                onClick = { viewModel.handleIntent(WorkspaceIntent.OnDeleteClicked) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            WorkspaceSelectionSection(
                isExpanded = isSelectionPanelExpanded,
                selectedPaths = uiState.selectedPaths,
                isAnalyzing = uiState.isAnalyzing,
                scanProgress = uiState.scanProgress,
                scanStatus = uiState.scanStatus,
                scanElapsedTime = uiState.scanElapsedTime,
                error = uiState.error,
                onClearPaths = { viewModel.handleIntent(WorkspaceIntent.OnClearPaths) },
                onBrowseClick = { directoryPickerLauncher.launch() },
                onAnalyzeClick = { viewModel.handleIntent(WorkspaceIntent.OnAnalyzeWorkspace) },
                onClearError = { viewModel.handleIntent(WorkspaceIntent.OnClearError) },
                onRemovePath = { path -> viewModel.handleIntent(WorkspaceIntent.OnRemovePath(path)) }
            )

            if (uiState.projects.isEmpty() && uiState.unusedResources.isEmpty() && uiState.activeResources.isEmpty() && !uiState.isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCardLayout(
                        title = "Analyze Development Workspace",
                        description = "Select your projects root folder (e.g. AndroidStudioProjects) to discover unused SDK platforms, old build tools, Gradle wrapper caches, dependency libraries, and Kotlin Native prebuilts.",
                        actionText = "Select Folder",
                        onAction = { directoryPickerLauncher.launch() },
                        icon = Icons.Default.Workspaces
                    )
                }
            } else if (!uiState.isAnalyzing) {
                WorkspaceResultsContent(
                    uiState = uiState,
                    onIntent = viewModel::handleIntent
                )
            }
        }
    }

    if (uiState.showConfirmDialog) {
        WorkspaceConfirmationDialog(
            selectedResources = uiState.selectedResources,
            totalCount = uiState.totalSelectedCount,
            totalSelectedSizeReadable = uiState.totalSelectedSizeReadable,
            onConfirm = { viewModel.handleIntent(WorkspaceIntent.OnConfirmDelete) },
            onDismiss = { viewModel.handleIntent(WorkspaceIntent.OnConfirmDismissDialog) }
        )
    }

    if (uiState.showDeletionProgressDialog) {
        WorkspaceDeletionProgressDialog(
            deletionProgressList = uiState.deletionProgressList,
            isDeletionComplete = uiState.isDeletionComplete,
            successCount = uiState.deletionSuccessCount,
            failedCount = uiState.deletionFailedCount,
            deletedSizeReadable = uiState.deletedSizeReadable,
            totalSelectedCount = uiState.totalSelectedCount,
            totalSelectedSizeReadable = uiState.totalSelectedSizeReadable,
            deletionResult = uiState.deletionResult,
            showFeedbackButton = showFeedbackButton,
            onShareFeedbackClick = {
                showReviewDialog = true
                viewModel.handleIntent(WorkspaceIntent.TrackFeedbackOpened)
            },
            onDismiss = { viewModel.handleIntent(WorkspaceIntent.OnResultDismissDialog) }
        )
    }

    if (showReviewDialog) {
        LaunchedEffect(showReviewDialog) {
            if (showReviewDialog) viewModel.handleIntent(WorkspaceIntent.TrackReviewPromptShown)
        }

        ReviewDialog(
            appVersion = currentVersion,
            onDismiss = {
                showReviewDialog = false
                viewModel.handleIntent(WorkspaceIntent.TrackFeedbackCancelled)
            },
            onReviewSubmitted = {
                coroutineScope.launch {
                    viewModel.saveReviewVersion(currentVersion)
                }
            }
        )
    }
}
