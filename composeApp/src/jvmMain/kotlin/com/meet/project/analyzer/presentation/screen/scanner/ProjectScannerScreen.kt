package com.meet.project.analyzer.presentation.screen.scanner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.ProjectScreenTabs
import com.meet.project.analyzer.presentation.components.EmptyStateCard
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
        onTabSelected = { index, projectScreenTabs ->
            viewModel.handleIntent(ProjectScannerIntent.SelectTab(index, projectScreenTabs))
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
    onTabSelected: (Int, ProjectScreenTabs) -> Unit,
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "Project Scanner",
                icon = Icons.Default.Folder
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it)
        ) {
            // Project Selection Section
            ProjectSelectionSection(
                uiState = uiState,
                onClearResults = onClearResults,
                onBrowseClick = onBrowseClick,
                onAnalyzeClick = onAnalyzeClick,
                onClearError = onClearError
            )
            // Results Content with Tabs and Scrollbar
            uiState.projectInfo?.let { project ->
                Column(modifier = Modifier.fillMaxSize()) {
                    // Tab Row
                    TabRow(
                        modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                        selectedTabIndex = uiState.selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        ProjectScreenTabs.entries.forEachIndexed { index, projectScreenTabs ->
                            Tab(
                                selected = uiState.selectedTabIndex == index,
                                modifier = Modifier.pointerHoverIcon(
                                    PointerIcon(
                                        Cursor.getPredefinedCursor(
                                            Cursor.HAND_CURSOR
                                        )
                                    )
                                ),
                                onClick = { onTabSelected(index, projectScreenTabs) },
                                text = {
                                    Text(
                                        projectScreenTabs.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }

                    // Tab Content with Scrollbar
                    when (uiState.selectedTab) {
                        ProjectScreenTabs.Overview -> {
                            ProjectOverviewTabContent(projectInfo = project)
                        }

                        ProjectScreenTabs.Modules -> {
                            ModulesTabContent(
                                rootModuleInfo = project.rootModuleBuildFileInfo,
                                subModuleList = project.subModuleBuildFileInfos,
                            )
                        }

                        ProjectScreenTabs.Plugins -> {
                            PluginsTabContent(
                                plugins = project.plugins
                            )
                        }

                        ProjectScreenTabs.Dependencies -> {
                            DependenciesTabContent(
                                dependencies = project.dependencies,
                            )
                        }

                        ProjectScreenTabs.BuildFiles -> {
                            BuildFilesTabContent(
                                rootModuleInfo = project.rootModuleBuildFileInfo,
                                subModuleList = project.subModuleBuildFileInfos,
                                settingsGradleFileInfo = project.settingsGradleFileInfo,
                                propertiesFileInfo = project.propertiesFileInfo,
                                gradleWrapperPropertiesFileInfo = project.gradleWrapperPropertiesFileInfo,
                                versionCatalogFileInfo = project.versionCatalogFileInfo
                            )
                        }

                        ProjectScreenTabs.ProjectFiles -> {
                            ProjectFilesTabContent(
                                projectFiles = project.projectFiles
                            )
                        }

                    }

                }
            } ?: EmptyStateCard(
                message = "No project selected",
                icon = Icons.Default.Folder,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )
        }
    }
}