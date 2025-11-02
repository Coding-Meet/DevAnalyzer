package com.meet.dev.analyzer.presentation.screen.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
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
import com.meet.dev.analyzer.core.utility.ProjectScreenTabs
import com.meet.dev.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.dev.analyzer.presentation.components.TabLayout
import com.meet.dev.analyzer.presentation.components.TabSlideAnimation
import com.meet.dev.analyzer.presentation.components.TopAppBar
import com.meet.dev.analyzer.presentation.screen.project.components.BuildFilesTabContent
import com.meet.dev.analyzer.presentation.screen.project.components.DependenciesTabContent
import com.meet.dev.analyzer.presentation.screen.project.components.ModulesTabContent
import com.meet.dev.analyzer.presentation.screen.project.components.PluginsTabContent
import com.meet.dev.analyzer.presentation.screen.project.components.ProjectFilesTabContent
import com.meet.dev.analyzer.presentation.screen.project.components.ProjectOverviewTabContent
import com.meet.dev.analyzer.presentation.screen.project.components.ProjectSelectionSection
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor

@Composable
fun ProjectAnalyzerScreen() {
    val viewModel = koinViewModel<ProjectAnalyzerViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Directory picker launcher
    val directoryPickerLauncher = rememberDirectoryPickerLauncher { directory ->
        if (directory != null) {
            coroutineScope.launch {
                val path = directory.path
                viewModel.handleIntent(ProjectAnalyzerIntent.SelectProject(path))
            }
        }
    }

    ProjectAnalyzerContent(
        uiState = uiState,
        onBrowseClick = {
            directoryPickerLauncher.launch()
        },
        onAnalyzeClick = {
            viewModel.handleIntent(ProjectAnalyzerIntent.AnalyzeProject)
        },
        onClearResults = {
            viewModel.handleIntent(ProjectAnalyzerIntent.ClearResults)
        },
        onClearError = {
            viewModel.handleIntent(ProjectAnalyzerIntent.ClearError)
        },
        onTabSelected = { previousTabIndex, currentTabIndex, projectScreenTabs ->
            viewModel.handleIntent(
                ProjectAnalyzerIntent.SelectTab(
                    previousTabIndex = previousTabIndex,
                    currentTabIndex = currentTabIndex,
                    projectScreenTabs = projectScreenTabs
                )
            )
        },
    )
}


@Composable
fun ProjectAnalyzerContent(
    uiState: ProjectAnalyzerUiState,
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
                title = "Development Project Analyzer",
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
                                moduleBuildFileInfos = uiState.projectInfo.moduleBuildFileInfos,
                                projectName = uiState.projectInfo.projectOverviewInfo.projectName,
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
                                projectName = uiState.projectInfo.projectOverviewInfo.projectName,
                                moduleBuildFileInfos = uiState.projectInfo.moduleBuildFileInfos,
                                settingsGradleFileInfo = uiState.projectInfo.settingsGradleFileInfo,
                                propertiesFileInfo = uiState.projectInfo.propertiesFileInfo,
                                gradleWrapperPropertiesFileInfo = uiState.projectInfo.gradleWrapperPropertiesFileInfo,
                                versionCatalogFileInfo = uiState.projectInfo.versionCatalogFileInfo
                            )
                        }

                        ProjectScreenTabs.ProjectFiles -> {
                            ProjectFilesTabContent(
                                projectFiles = uiState.projectInfo.projectFiles,
                                projectName = uiState.projectInfo.projectOverviewInfo.projectName
                            )
                        }

                    }
                }
            } else {
                EmptyStateCardLayout(
                    title = "No Project Selected",
                    description = "Browse and select an Android Studio project folder, then click Analyze to view modules, plugins, and dependencies.",
                    icon = Icons.Default.FolderOpen,
                    modifier = Modifier.padding(10.dp).fillMaxSize()
                )
            }
        }
    }
}