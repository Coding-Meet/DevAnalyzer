package com.meet.project.analyzer.presentation.screen.scanner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.ProjectScreenTabs
import com.meet.project.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.project.analyzer.presentation.components.TabLayout
import com.meet.project.analyzer.presentation.components.TabSlideAnimation
import com.meet.project.analyzer.presentation.components.TopAppBar
import com.meet.project.analyzer.presentation.screen.scanner.components.BuildFilesTabContent
import com.meet.project.analyzer.presentation.screen.scanner.components.DependenciesTabContent
import com.meet.project.analyzer.presentation.screen.scanner.components.ModulesTabContent
import com.meet.project.analyzer.presentation.screen.scanner.components.PluginsTabContent
import com.meet.project.analyzer.presentation.screen.scanner.components.ProjectFilesTabContent
import com.meet.project.analyzer.presentation.screen.scanner.components.ProjectOverviewTabContent
import com.meet.project.analyzer.presentation.screen.scanner.components.ProjectSelectionSection
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor

@Composable
fun ProjectScannerScreen() {
    val viewModel = koinViewModel<ProjectScannerViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Directory picker launcher
    val directoryPickerLauncher = rememberDirectoryPickerLauncher { directory ->
        if (directory != null) {
            coroutineScope.launch {
                val path = directory.path
                viewModel.handleIntent(ProjectScannerIntent.SelectProject(path))
            }
        }
    }

    ProjectScannerContent(
        uiState = uiState,
        onBrowseClick = {
            directoryPickerLauncher.launch()
        },
        onAnalyzeClick = {
            viewModel.handleIntent(ProjectScannerIntent.AnalyzeProject)
        },
        onClearResults = {
            viewModel.handleIntent(ProjectScannerIntent.ClearResults)
        },
        onClearError = {
            viewModel.handleIntent(ProjectScannerIntent.ClearError)
        },
        onTabSelected = { previousTabIndex, currentTabIndex, projectScreenTabs ->
            viewModel.handleIntent(
                ProjectScannerIntent.SelectTab(
                    previousTabIndex = previousTabIndex,
                    currentTabIndex = currentTabIndex,
                    projectScreenTabs = projectScreenTabs
                )
            )
        },
    )
}


@Composable
fun ProjectScannerContent(
    uiState: ProjectScannerUiState,
    onBrowseClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onClearResults: () -> Unit,
    onClearError: () -> Unit,
    onTabSelected: (previousTabIndex: Int, currentTabIndex: Int, projectScreenTabs: ProjectScreenTabs) -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "Project Scanner",
                icon = Icons.Default.Folder,
                actions = {
                    if (uiState.projectInfo != null) {
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
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Project Selection Section
            ProjectSelectionSection(
                isExpanded = isExpanded,
                uiState = uiState,
                onClearResults = onClearResults,
                onBrowseClick = onBrowseClick,
                onAnalyzeClick = onAnalyzeClick,
                onClearError = onClearError
            )
            // Results Content with Tabs and Scrollbar
            if (uiState.projectInfo != null) {
                // Tab Row
                TabLayout(
                    selectedTabIndex = uiState.selectedTabIndex,
                    tabList = ProjectScreenTabs.entries,
                    onClick = onTabSelected
                )
                // Tab Content with Scrollbar
                TabSlideAnimation(
                    selectedTabIndex = uiState.selectedTabIndex,
                    previousTabIndex = uiState.previousTabIndex,
                    targetState = uiState.selectedTab
                ) { selectedTab ->

                    when (selectedTab) {
                        ProjectScreenTabs.Overview -> {
                            ProjectOverviewTabContent(projectInfo = uiState.projectInfo)
                        }

                        ProjectScreenTabs.Modules -> {
                            ModulesTabContent(
                                rootModuleInfo = uiState.projectInfo.rootModuleBuildFileInfo,
                                subModuleList = uiState.projectInfo.subModuleBuildFileInfos,
                            )
                        }

                        ProjectScreenTabs.Plugins -> {
                            PluginsTabContent(
                                plugins = uiState.projectInfo.plugins
                            )
                        }

                        ProjectScreenTabs.Dependencies -> {
                            DependenciesTabContent(
                                dependencies = uiState.projectInfo.dependencies,
                            )
                        }

                        ProjectScreenTabs.BuildFiles -> {
                            BuildFilesTabContent(
                                rootModuleInfo = uiState.projectInfo.rootModuleBuildFileInfo,
                                subModuleList = uiState.projectInfo.subModuleBuildFileInfos,
                                settingsGradleFileInfo = uiState.projectInfo.settingsGradleFileInfo,
                                propertiesFileInfo = uiState.projectInfo.propertiesFileInfo,
                                gradleWrapperPropertiesFileInfo = uiState.projectInfo.gradleWrapperPropertiesFileInfo,
                                versionCatalogFileInfo = uiState.projectInfo.versionCatalogFileInfo
                            )
                        }

                        ProjectScreenTabs.ProjectFiles -> {
                            ProjectFilesTabContent(
                                projectFiles = uiState.projectInfo.projectFiles
                            )
                        }

                    }
                }
            } else {
                EmptyStateCardLayout(
                    message = "No project selected",
                    icon = Icons.Default.Folder,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }
        }
    }
}