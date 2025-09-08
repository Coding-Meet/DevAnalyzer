package com.meet.project.analyzer.presentation.screen.scanner

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.meet.project.analyzer.core.utility.AppLogger
import com.meet.project.analyzer.core.utility.StorageUtils
import com.meet.project.analyzer.data.models.scanner.BuildFileInfo
import com.meet.project.analyzer.data.models.scanner.BuildFileType
import com.meet.project.analyzer.data.models.scanner.DependencyInfo
import com.meet.project.analyzer.data.models.scanner.DependencyType
import com.meet.project.analyzer.data.models.scanner.FileType
import com.meet.project.analyzer.data.models.scanner.LibraryInfo
import com.meet.project.analyzer.data.models.scanner.ModuleInfo
import com.meet.project.analyzer.data.models.scanner.ModuleType
import com.meet.project.analyzer.data.models.scanner.ProjectFileInfo
import com.meet.project.analyzer.data.models.scanner.ProjectInfo
import com.meet.project.analyzer.data.models.scanner.ProjectType
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.io.File

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
                AppLogger.i("ProjectScanner") { "Directory selected: $path" }
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
        onToggleDependencyExpansion = { dependencyName ->
            viewModel.handleIntent(ProjectScannerIntent.ToggleDependencyExpansion(dependencyName))
        }
    )

}

@Composable
fun ProjectScannerContent(
    uiState: ProjectScannerUiState,
    onBrowseClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onClearResults: () -> Unit,
    onClearError: () -> Unit,
    onToggleDependencyExpansion: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs =
        listOf("Overview", "Modules", "Dependencies", "Libraries", "Build Files", "Project Files")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Single Row Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Project Scanner",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "Analyze project structure, dependencies and build configuration",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Project Selection Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Path input and buttons in one row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.selectedPath,
                        onValueChange = { },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("No project selected") },
                        readOnly = true,
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = "Project folder"
                            )
                        },
                        trailingIcon = if (uiState.selectedPath.isNotEmpty()) {
                            {
                                IconButton(onClick = onClearResults) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear selection"
                                    )
                                }
                            }
                        } else null
                    )

                    Button(
                        onClick = onBrowseClick,
                        enabled = !uiState.isScanning,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse")
                    }

                    Button(
                        onClick = onAnalyzeClick,
                        enabled = uiState.selectedPath.isNotEmpty() && !uiState.isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.height(56.dp)
                    ) {
                        if (uiState.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uiState.isScanning) "Analyzing..." else "Analyze")
                    }
                }

                // Progress and status
                if (uiState.isScanning) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { uiState.scanProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            uiState.scanStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Error display
                uiState.error?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onClearError) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Results Content with Tabs and Scrollbar
        uiState.scanResult?.let { project ->
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Row
                TabRow(
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // Tab Content with Scrollbar
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (selectedTab) {
                        0 -> OverviewTabContent(project)
                        1 -> ModulesTabContent(
                            project.modules,
                            uiState.expandedDependencies,
                            onToggleDependencyExpansion
                        )

                        2 -> DependenciesTabContent(
                            project.dependencies,
                            uiState.expandedDependencies,
                            onToggleDependencyExpansion
                        )

                        3 -> LibrariesTabContent(project.allLibraries)
                        4 -> BuildFilesTabContent(project.buildFiles)
                        5 -> ProjectFilesTabContent(project.projectFiles)
                    }
                }
            }
        }
    }
}

// ===========================================
// TAB CONTENT COMPONENTS WITH GRIDS
// ===========================================

@Composable
fun OverviewTabContent(project: ProjectInfo) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProjectOverviewCard(project)
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState),
            style = defaultScrollbarStyle().copy(
                hoverColor = MaterialTheme.colorScheme.outline,
                unhoverColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun ModulesTabContent(
    modules: List<ModuleInfo>,
    expandedDependencies: Set<String>,
    onToggleDependencyExpansion: (String) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 400.dp),
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(modules) { module ->
                DetailedModuleCard(module, expandedDependencies, onToggleDependencyExpansion)
            }

            if (modules.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCard("No modules found")
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState),
            style = defaultScrollbarStyle().copy(
                hoverColor = MaterialTheme.colorScheme.outline,
                unhoverColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun DependenciesTabContent(
    dependencies: List<DependencyInfo>,
    expandedDependencies: Set<String>,
    onToggleDependencyExpansion: (String) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 400.dp),
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            val groupedDeps = dependencies.groupBy { it.type }

            groupedDeps.forEach { (type, deps) ->
                item {
                    DependencyTypeCard(
                        type,
                        deps,
                        expandedDependencies,
                        onToggleDependencyExpansion
                    )
                }
            }

            if (dependencies.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCard("No dependencies found")
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState),
            style = defaultScrollbarStyle().copy(
                hoverColor = MaterialTheme.colorScheme.outline,
                unhoverColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

    }
}

@Composable
fun LibrariesTabContent(libraries: List<LibraryInfo>) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(350.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(libraries) { library ->
                LibraryDetailCard(library)
            }

            if (libraries.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptyStateCard("No libraries found")
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState),
            style = defaultScrollbarStyle().copy(
                hoverColor = MaterialTheme.colorScheme.outline,
                unhoverColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun BuildFilesTabContent(buildFiles: List<BuildFileInfo>) {

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 400.dp),
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            items(buildFiles) { buildFile ->
                DetailedBuildFileCard(buildFile)
            }

            if (buildFiles.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCard("No build files found")
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState),
            style = defaultScrollbarStyle().copy(
                hoverColor = MaterialTheme.colorScheme.outline,
                unhoverColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun ProjectFilesTabContent(projectFiles: List<ProjectFileInfo>) {
    var selectedFile by remember { mutableStateOf<ProjectFileInfo?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left panel - File tree
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            val scrollState = rememberLazyListState()


            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    item {
                        Text(
                            "Project Files (${projectFiles.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    // Build file tree structure
                    val fileTree = buildFileTree(projectFiles)
                    fileTree.forEach { (folder, files) ->
                        item {
                            FolderHeader(folder, files.size)
                        }
                        items(files) { file ->
                            FileTreeItem(
                                file = file,
                                isSelected = selectedFile == file,
                                onClick = { selectedFile = file }
                            )
                        }
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState),
                    style = defaultScrollbarStyle().copy(
                        hoverColor = MaterialTheme.colorScheme.outline,
                        unhoverColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // Divider
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        // Right panel - File content
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {
            if (selectedFile != null) {
                FileContentViewer(selectedFile!!)
            } else {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Select a file to view its content",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


fun buildFileTree(projectFiles: List<ProjectFileInfo>): Map<String, List<ProjectFileInfo>> {
    return projectFiles.groupBy { file ->
        val pathParts = file.relativePath.split("/", "\\")
        if (pathParts.size > 1) {
            pathParts.dropLast(1).joinToString("/")
        } else {
            "Root"
        }
    }.toSortedMap()
}

@Composable
fun FolderHeader(folderPath: String, fileCount: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                folderPath,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Text(
                fileCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FileTreeItem(
    file: ProjectFileInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            Color.Transparent,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(12.dp)) // Indent for files
            Icon(
                getFileTypeIcon(file.type),
                contentDescription = null,
                tint = getFileTypeColor(file.type),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                file.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text(
                file.size,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FileContentViewer(file: ProjectFileInfo) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // File header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    getFileTypeIcon(file.type),
                    contentDescription = null,
                    tint = getFileTypeColor(file.type),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        file.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        file.relativePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        file.size,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // File content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (file.type == FileType.IMAGE) {
                // Show image preview
                AsyncImage(
                    model = File(file.path),
                    contentDescription = file.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            } else if (file.isReadable && file.content != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            file.content,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState),
                    style = defaultScrollbarStyle().copy(
                        hoverColor = MaterialTheme.colorScheme.outline,
                        unhoverColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            } else {
                // Non-readable file
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (file.content == null) "Binary file - cannot display content" else "File too large to display",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "File type: ${file.type.name.replace("_", " ").lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


// ===========================================
// DETAILED UI COMPONENTS
// ===========================================

@Composable
fun ProjectOverviewCard(project: ProjectInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Project header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    getProjectTypeIcon(project.projectType),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        project.projectName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        project.projectType.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Stats grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ProjectStatItem(
                        label = "Size",
                        value = project.totalSize,
                        icon = Icons.Default.Storage
                    )
                }
                item {
                    ProjectStatItem(
                        label = "Modules",
                        value = project.modules.size.toString(),
                        icon = Icons.Default.AccountTree
                    )
                }
                item {
                    ProjectStatItem(
                        label = "Dependencies",
                        value = project.dependencies.size.toString(),
                        icon = Icons.AutoMirrored.Filled.LibraryBooks
                    )
                }
                item {
                    ProjectStatItem(
                        label = "Libraries",
                        value = project.allLibraries.size.toString(),
                        icon = Icons.Default.Inventory
                    )
                }
                item {
                    ProjectStatItem(
                        label = "Build Files",
                        value = project.buildFiles.size.toString(),
                        icon = Icons.Default.Build
                    )
                }
                item {
                    ProjectStatItem(
                        label = "Project Files",
                        value = project.projectFiles.size.toString(),
                        icon = Icons.AutoMirrored.Filled.InsertDriveFile
                    )
                }
            }

            // Version information if available
            val versions = buildList {
                project.gradleVersion?.let { add("Gradle $it") }
                project.kotlinVersion?.let { add("Kotlin $it") }
                project.androidGradlePluginVersion?.let { add("AGP $it") }
                project.targetSdkVersion?.let { add("Target SDK $it") }
                project.minSdkVersion?.let { add("Min SDK $it") }
            }

            if (versions.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(versions) { version ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                version,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (project.isMultiModule) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    "Multi-Module",
                                    style = MaterialTheme.typography.labelMedium,
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

@Composable
fun DetailedModuleCard(
    module: ModuleInfo,
    expandedDependencies: Set<String>,
    onToggleDependencyExpansion: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Module header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    getModuleTypeIcon(module.type),
                    contentDescription = null,
                    tint = getModuleTypeColor(module.type),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        module.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        module.type.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = getModuleTypeColor(module.type)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        module.size,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Module stats in grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ModuleStatChip("${module.sourceFiles} Sources", Icons.Default.Code)
                }
                item {
                    ModuleStatChip(
                        "${module.resourceFiles} Resources",
                        Icons.AutoMirrored.Filled.InsertDriveFile
                    )
                }
                item {
                    ModuleStatChip(
                        "${module.dependencies.size} Dependencies",
                        Icons.AutoMirrored.Filled.LibraryBooks
                    )
                }
                module.buildFile?.let {
                    item {
                        ModuleStatChip(it, Icons.Default.Build)
                    }
                }
            }

            // Dependencies section
            if (module.dependencies.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                val isExpanded = expandedDependencies.contains(module.name)
                val displayDeps =
                    if (isExpanded) module.dependencies else module.dependencies.take(3)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Dependencies (${module.dependencies.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (module.dependencies.size > 3) {
                        TextButton(
                            onClick = { onToggleDependencyExpansion(module.name) }
                        ) {
                            Text(
                                if (isExpanded) "Show Less" else "Show All",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                displayDeps.forEach { dependency ->
                    ModuleDependencyRow(dependency)
                }
            }
        }
    }
}

@Composable
fun DependencyTypeCard(
    type: DependencyType,
    dependencies: List<DependencyInfo>,
    expandedDependencies: Set<String>,
    onToggleDependencyExpansion: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with expand/collapse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = getDependencyTypeColor(type).copy(alpha = 0.2f),
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.LibraryBooks,
                            contentDescription = null,
                            tint = getDependencyTypeColor(type),
                            modifier = Modifier
                                .size(32.dp)
                                .padding(6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            type.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = getDependencyTypeColor(type)
                        )
                        Text(
                            "${dependencies.size} dependencies",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (dependencies.size > 5) {
                    val isExpanded = expandedDependencies.contains(type.name)
                    TextButton(
                        onClick = { onToggleDependencyExpansion(type.name) }
                    ) {
                        Text(
                            if (isExpanded) "Show Less" else "Show All",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Dependencies list
            val isExpanded = expandedDependencies.contains(type.name)
            val displayDeps = if (isExpanded) dependencies else dependencies.take(5)

            displayDeps.forEach { dependency ->
                DependencyDetailRow(dependency)
            }
        }
    }
}

@Composable
fun LibraryDetailCard(library: LibraryInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (library.isUsed)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Library header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (library.isUsed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (library.isUsed)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        library.artifact,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        library.group,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Version conflict indicator
                if (library.hasVersionConflict) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Conflict",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Status and type
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = if (library.isUsed)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        if (library.isUsed) "Used" else "Unused",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (library.isUsed)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                library.dependencyType?.let { type ->
                    Surface(
                        color = getDependencyTypeColor(type).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            type.name.lowercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = getDependencyTypeColor(type),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Versions
            if (library.allVersions.isNotEmpty()) {
                Text(
                    "Versions (${library.allVersions.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(library.allVersions) { version ->
                        Surface(
                            color = if (version == library.latestVersion)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                version,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (version == library.latestVersion)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Used in modules
            if (library.usedInModules.isNotEmpty()) {
                Text(
                    "Used in Modules",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(library.usedInModules) { module ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                module,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectFilesSummaryCard(projectFiles: List<ProjectFileInfo>) {
    val fileTypeGroups = projectFiles.groupBy { it.type }
    val totalSize = projectFiles.sumOf { it.sizeBytes }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Project Files Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${projectFiles.size} files • ${StorageUtils.formatSize(totalSize)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // File type statistics
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                modifier = Modifier.height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(fileTypeGroups.entries.toList().take(8)) { (type, files) ->
                    FileTypeStatChip(type, files.size)
                }
            }
        }
    }
}

@Composable
fun FileTypeStatChip(type: FileType, count: Int) {
    Surface(
        color = getFileTypeColor(type).copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                getFileTypeIcon(type),
                contentDescription = null,
                tint = getFileTypeColor(type),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "$count ${type.name.replace("_", " ").lowercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = getFileTypeColor(type)
            )
        }
    }
}

@Composable
fun FileTypeSectionHeader(type: FileType, count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = getFileTypeColor(type).copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                getFileTypeIcon(type),
                contentDescription = null,
                tint = getFileTypeColor(type),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "${
                    type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                } ($count)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = getFileTypeColor(type)
            )
        }
    }
}

@Composable
fun ProjectFileCard(file: ProjectFileInfo) {
    var showContent by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // File header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    getFileTypeIcon(file.type),
                    contentDescription = null,
                    tint = getFileTypeColor(file.type),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        file.relativePath,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    file.size,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Show content button for readable files
            if (file.isReadable && file.content != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showContent = !showContent },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (showContent) "Hide Content" else "Show Content",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        if (showContent) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Content preview
                if (showContent) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            file.content,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            maxLines = 20
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedBuildFileCard(buildFile: BuildFileInfo) {
    var showContent by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Build file header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    getBuildFileIcon(buildFile.type),
                    contentDescription = null,
                    tint = getBuildFileColor(buildFile.type),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        buildFile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        buildFile.type.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = getBuildFileColor(buildFile.type)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        buildFile.size,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // File path
            Text(
                buildFile.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Content preview
            buildFile.content?.let { content ->
                if (content.isNotEmpty()) {
                    TextButton(
                        onClick = { showContent = !showContent },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (showContent) "Hide Content" else "Show Content",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            if (showContent) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (showContent) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                content,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleStatChip(text: String, icon: ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModuleDependencyRow(dependency: DependencyInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    getDependencyTypeColor(dependency.type),
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                dependency.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        Surface(
            color = getDependencyTypeColor(dependency.type).copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Text(
                dependency.scope,
                style = MaterialTheme.typography.labelSmall,
                color = getDependencyTypeColor(dependency.type),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            dependency.version,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DependencyDetailRow(dependency: DependencyInfo) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dependency.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "v${dependency.version}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("•", style = MaterialTheme.typography.labelMedium)
                    Text(
                        dependency.module,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ===========================================
// UTILITY FUNCTIONS FOR ICONS AND COLORS
// ===========================================

fun getProjectTypeIcon(type: ProjectType): ImageVector {
    return when (type) {
        ProjectType.ANDROID -> Icons.Default.Android
        ProjectType.KOTLIN_MULTIPLATFORM -> Icons.Default.Language
        ProjectType.COMPOSE_MULTIPLATFORM -> Icons.Default.Brush
        ProjectType.JAVA -> Icons.Default.Coffee
        ProjectType.SPRING_BOOT -> Icons.Default.Settings
        ProjectType.UNKNOWN -> Icons.Default.QuestionMark
    }
}

fun getModuleTypeIcon(type: ModuleType): ImageVector {
    return when (type) {
        ModuleType.APP -> Icons.Default.Smartphone
        ModuleType.LIBRARY -> Icons.AutoMirrored.Filled.LibraryBooks
        ModuleType.FEATURE -> Icons.Default.Extension
        ModuleType.CORE -> Icons.Default.Settings
        ModuleType.DATA -> Icons.Default.Storage
        ModuleType.DOMAIN -> Icons.Default.Business
        ModuleType.PRESENTATION -> Icons.Default.Visibility
        ModuleType.UNKNOWN -> Icons.AutoMirrored.Filled.Help
    }
}

fun getModuleTypeColor(type: ModuleType): Color {
    return when (type) {
        ModuleType.APP -> Color(0xFF2196F3)
        ModuleType.LIBRARY -> Color(0xFF4CAF50)
        ModuleType.FEATURE -> Color(0xFF9C27B0)
        ModuleType.CORE -> Color(0xFF607D8B)
        ModuleType.DATA -> Color(0xFF795548)
        ModuleType.DOMAIN -> Color(0xFFFF9800)
        ModuleType.PRESENTATION -> Color(0xFFE91E63)
        ModuleType.UNKNOWN -> Color(0xFF9E9E9E)
    }
}

fun getBuildFileIcon(type: BuildFileType): ImageVector {
    return when (type) {
        BuildFileType.BUILD_GRADLE_KTS, BuildFileType.BUILD_GRADLE -> Icons.Default.Build
        BuildFileType.SETTINGS_GRADLE_KTS, BuildFileType.SETTINGS_GRADLE -> Icons.Default.Settings
        BuildFileType.VERSION_CATALOG -> Icons.Default.BookmarkBorder
        BuildFileType.GRADLE_PROPERTIES, BuildFileType.LOCAL_PROPERTIES -> Icons.Default.Settings
    }
}

fun getBuildFileColor(type: BuildFileType): Color {
    return when (type) {
        BuildFileType.BUILD_GRADLE_KTS, BuildFileType.BUILD_GRADLE -> Color(0xFF2196F3)
        BuildFileType.SETTINGS_GRADLE_KTS, BuildFileType.SETTINGS_GRADLE -> Color(0xFF607D8B)
        BuildFileType.VERSION_CATALOG -> Color(0xFF7C4DFF)
        BuildFileType.GRADLE_PROPERTIES, BuildFileType.LOCAL_PROPERTIES -> Color(0xFF795548)
    }
}

fun getDependencyTypeColor(type: DependencyType): Color {
    return when (type) {
        DependencyType.IMPLEMENTATION -> Color(0xFF4CAF50)
        DependencyType.API -> Color(0xFF2196F3)
        DependencyType.COMPILE_ONLY -> Color(0xFFFF9800)
        DependencyType.RUNTIME_ONLY -> Color(0xFF9C27B0)
        DependencyType.TEST_IMPLEMENTATION -> Color(0xFFE91E63)
        DependencyType.ANDROID_TEST_IMPLEMENTATION -> Color(0xFF3F51B5)
        DependencyType.KAPT -> Color(0xFF795548)
        DependencyType.KSP -> Color(0xFF607D8B)
        DependencyType.PLUGIN -> Color(0xFF00BCD4)
    }
}

fun getFileTypeIcon(type: FileType): ImageVector {
    return when (type) {
        FileType.SOURCE_KOTLIN -> Icons.Default.Code
        FileType.SOURCE_JAVA -> Icons.Default.Coffee
        FileType.BUILD_SCRIPT -> Icons.Default.Build
        FileType.CONFIGURATION -> Icons.Default.Settings
        FileType.RESOURCE -> Icons.Default.Image
        FileType.MANIFEST -> Icons.Default.Description
        FileType.LAYOUT -> Icons.AutoMirrored.Filled.ViewQuilt
        FileType.DRAWABLE -> Icons.Default.Image
        FileType.VALUES -> Icons.AutoMirrored.Filled.List
        FileType.ASSETS -> Icons.Default.Folder
        FileType.PROPERTIES -> Icons.Default.Settings
        FileType.JSON -> Icons.Default.DataObject
        FileType.XML -> Icons.Default.Code
        FileType.TEXT -> Icons.Default.TextFields
        FileType.MARKDOWN -> Icons.AutoMirrored.Filled.Article
        FileType.IMAGE -> Icons.Default.Image
        FileType.OTHER -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

fun getFileTypeColor(type: FileType): Color {
    return when (type) {
        FileType.SOURCE_KOTLIN -> Color(0xFF7F52FF)
        FileType.SOURCE_JAVA -> Color(0xFFED8B00)
        FileType.BUILD_SCRIPT -> Color(0xFF02303A)
        FileType.CONFIGURATION -> Color(0xFF607D8B)
        FileType.RESOURCE -> Color(0xFF4CAF50)
        FileType.MANIFEST -> Color(0xFF3DDC84)
        FileType.LAYOUT -> Color(0xFF2196F3)
        FileType.DRAWABLE -> Color(0xFF9C27B0)
        FileType.VALUES -> Color(0xFFFF9800)
        FileType.ASSETS -> Color(0xFF795548)
        FileType.PROPERTIES -> Color(0xFF607D8B)
        FileType.JSON -> Color(0xFF4CAF50)
        FileType.XML -> Color(0xFFFF5722)
        FileType.TEXT -> Color(0xFF9E9E9E)
        FileType.MARKDOWN -> Color(0xFF673AB7)
        FileType.IMAGE -> Color(0xFF2196F3)
        FileType.OTHER -> Color(0xFF9E9E9E)
    }
}