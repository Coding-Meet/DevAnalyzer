package com.meet.project.analyzer

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.data.models.scanner.Dependency
import com.meet.project.analyzer.data.models.scanner.Plugin
import com.meet.project.analyzer.data.models.scanner.ProjectAnalysis

// Dummy data
object DummyData {
    val dependencies = listOf(
        Dependency(
            group = "org.jetbrains.kotlin",
            name = "kotlin-stdlib",
            currentVersion = "1.9.20",
            latestVersion = "1.9.22",
            availableVersions = listOf(
                "1.9.22",
                "1.9.21",
                "1.9.20",
                "1.9.10",
                "1.9.0",
                "1.8.22",
                "1.8.21",
                "1.8.20"
            ),
            configuration = "implementation",
            module = "root",
            fileType = "build.gradle.kts"
        ),
        Dependency(
            group = "androidx.compose.ui",
            name = "ui",
            currentVersion = "1.5.10",
            latestVersion = "1.6.0",
            availableVersions = listOf(
                "1.6.0",
                "1.5.15",
                "1.5.14",
                "1.5.13",
                "1.5.12",
                "1.5.11",
                "1.5.10",
                "1.5.9"
            ),
            configuration = "implementation",
            module = "app",
            fileType = "build.gradle.kts"
        ),
        Dependency(
            group = "androidx.compose.ui",
            name = "ui-tooling-preview",
            currentVersion = "1.5.10",
            latestVersion = "1.6.0",
            availableVersions = listOf(
                "1.6.0",
                "1.5.15",
                "1.5.14",
                "1.5.13",
                "1.5.12",
                "1.5.11",
                "1.5.10"
            ),
            configuration = "implementation",
            module = "app",
            fileType = "build.gradle.kts"
        ),
        Dependency(
            group = "androidx.compose.material3",
            name = "material3",
            currentVersion = "1.1.2",
            latestVersion = "1.2.0",
            availableVersions = listOf(
                "1.2.0",
                "1.1.5",
                "1.1.4",
                "1.1.3",
                "1.1.2",
                "1.1.1",
                "1.1.0",
                "1.0.1"
            ),
            configuration = "implementation",
            module = "app",
            fileType = "build.gradle.kts"
        ),
        Dependency(
            group = "org.jetbrains.kotlinx",
            name = "kotlinx-coroutines-core",
            currentVersion = "1.7.3",
            latestVersion = "1.8.0",
            availableVersions = listOf(
                "1.8.0",
                "1.7.3",
                "1.7.2",
                "1.7.1",
                "1.7.0",
                "1.6.4",
                "1.6.3"
            ),
            configuration = "implementation",
            module = "core",
            fileType = "build.gradle"
        ),
        Dependency(
            group = "com.squareup.retrofit2",
            name = "retrofit",
            currentVersion = "2.9.0",
            latestVersion = "2.10.0",
            availableVersions = listOf(
                "2.10.0",
                "2.9.0",
                "2.8.2",
                "2.8.1",
                "2.8.0",
                "2.7.2",
                "2.7.1",
                "2.7.0",
                "2.6.4"
            ),
            configuration = "implementation",
            module = "network",
            fileType = "build.gradle"
        ),
        Dependency(
            group = "io.ktor",
            name = "ktor-client-core",
            currentVersion = "2.3.5",
            latestVersion = "2.3.7",
            availableVersions = listOf(
                "2.3.7",
                "2.3.6",
                "2.3.5",
                "2.3.4",
                "2.3.3",
                "2.3.2",
                "2.3.1"
            ),
            configuration = "implementation",
            module = "network",
            fileType = "build.gradle.kts"
        ),
        Dependency(
            group = "junit",
            name = "junit",
            currentVersion = "4.13.2",
            latestVersion = "4.13.2",
            availableVersions = listOf("4.13.2", "4.13.1", "4.13", "4.12"),
            configuration = "testImplementation",
            module = "app",
            fileType = "build.gradle.kts"
        ),
        Dependency(
            group = "androidx.test.ext",
            name = "junit",
            currentVersion = "1.1.5",
            latestVersion = "1.1.5",
            availableVersions = listOf("1.1.5", "1.1.4", "1.1.3", "1.1.2", "1.1.1"),
            configuration = "androidTestImplementation",
            module = "app",
            fileType = "build.gradle.kts"
        ),
        Dependency(
            group = "com.google.dagger",
            name = "hilt-android",
            currentVersion = "2.48",
            latestVersion = "2.50",
            availableVersions = listOf("2.50", "2.49", "2.48", "2.47", "2.46", "2.45", "2.44"),
            configuration = "implementation",
            module = "app",
            fileType = "build.gradle.kts"
        ),
        Dependency(
            group = "androidx.lifecycle",
            name = "lifecycle-viewmodel-ktx",
            currentVersion = "2.7.0",
            latestVersion = "2.7.0",
            availableVersions = listOf("2.7.0", "2.6.2", "2.6.1", "2.6.0", "2.5.1", "2.5.0"),
            configuration = "implementation",
            module = "app",
            fileType = "build.gradle.kts"
        ),
        Dependency(
            group = "androidx.navigation",
            name = "navigation-compose",
            currentVersion = "2.7.5",
            latestVersion = "2.7.6",
            availableVersions = listOf(
                "2.7.6",
                "2.7.5",
                "2.7.4",
                "2.7.3",
                "2.7.2",
                "2.7.1",
                "2.7.0"
            ),
            configuration = "implementation",
            module = "app",
            fileType = "build.gradle.kts"
        ),
    )

    val plugins = listOf(
        Plugin(
            id = "org.jetbrains.kotlin.jvm",
            currentVersion = "1.9.20",
            latestVersion = "1.9.22",
            availableVersions = listOf("1.9.22", "1.9.21", "1.9.20", "1.9.10", "1.9.0"),
            module = "root",
            fileType = "build.gradle.kts"
        ),
        Plugin(
            id = "org.jetbrains.compose",
            currentVersion = "1.5.10",
            latestVersion = "1.6.0",
            availableVersions = listOf(
                "1.6.0",
                "1.5.15",
                "1.5.14",
                "1.5.13",
                "1.5.12",
                "1.5.11",
                "1.5.10"
            ),
            module = "app",
            fileType = "build.gradle.kts"
        ),
        Plugin(
            id = "com.android.application",
            currentVersion = "8.1.2",
            latestVersion = "8.2.1",
            availableVersions = listOf(
                "8.2.1",
                "8.2.0",
                "8.1.4",
                "8.1.3",
                "8.1.2",
                "8.1.1",
                "8.1.0"
            ),
            module = "app",
            fileType = "build.gradle"
        ),
        Plugin(
            id = "org.jetbrains.kotlin.plugin.compose",
            currentVersion = "1.9.20",
            latestVersion = "1.9.22",
            availableVersions = listOf("1.9.22", "1.9.21", "1.9.20", "1.9.10"),
            module = "app",
            fileType = "build.gradle.kts"
        ),
        Plugin(
            id = "dagger.hilt.android.plugin",
            currentVersion = "2.48",
            latestVersion = "2.50",
            availableVersions = listOf("2.50", "2.49", "2.48", "2.47", "2.46"),
            module = "app",
            fileType = "build.gradle"
        ),
        Plugin(
            id = "kotlin-kapt",
            currentVersion = null,
            latestVersion = null,
            availableVersions = emptyList(),
            module = "app",
            fileType = "build.gradle.kts"
        ),
    )

    val modules = listOf("root", "app", "core", "network", "data", "ui-common")

    val analysis = ProjectAnalysis(dependencies, plugins, modules)
}

enum class AnalysisTab(val title: String, val icon: ImageVector) {
    Overview("Overview", Icons.Outlined.Dashboard),
    Dependencies("Dependencies", Icons.Outlined.AccountTree),
    Plugins("Plugins", Icons.Outlined.Extension),
    Modules("Modules", Icons.Outlined.Folder)
}

@Composable
fun App() {
    var projectPath by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(AnalysisTab.Overview) }
    var isLoading by remember { mutableStateOf(false) }
    val analysisResult = remember { DummyData.analysis }

    MaterialTheme(
        colorScheme = dynamicLightColorScheme()
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
        ) {
            val isWideScreen = maxWidth > 840.dp

            if (isWideScreen) {
                // Wide screen layout - Navigation rail + content
                Row(modifier = Modifier.fillMaxSize()) {
                    // Navigation Rail
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Spacer(Modifier.height(24.dp))

                        // App Icon
                        Card(
                            modifier = Modifier.size(56.dp).padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Analytics,
                                    contentDescription = "App Icon",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // Navigation items
                        AnalysisTab.entries.forEach { tab ->
                            NavigationRailItem(
                                icon = {
                                    Icon(
                                        tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        tab.title,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab }
                            )
                        }

                        Spacer(Modifier.weight(1f))
                    }

                    // Main content
                    MainContent(
                        modifier = Modifier.fillMaxSize(),
                        projectPath = projectPath,
                        onProjectPathChange = { projectPath = it },
                        selectedTab = selectedTab,
                        analysisResult = analysisResult,
                        isLoading = isLoading,
                        onAnalyze = { isLoading = !isLoading }
                    )
                }
            } else {
                // Compact screen layout - Bottom navigation + content
                Column(modifier = Modifier.fillMaxSize()) {
                    // Main content
                    MainContent(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        projectPath = projectPath,
                        onProjectPathChange = { projectPath = it },
                        selectedTab = selectedTab,
                        analysisResult = analysisResult,
                        isLoading = isLoading,
                        onAnalyze = { isLoading = !isLoading },
                        showHeader = true
                    )

                    // Bottom navigation
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        AnalysisTab.entries.forEach { tab ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        tab.title,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HeaderSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Analytics,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Gradle Project Analyzer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Analyze dependencies, plugins, and project structure",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ProjectInputSection(
    projectPath: String,
    onProjectPathChange: (String) -> Unit,
    isLoading: Boolean,
    onAnalyze: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Project Path",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedTextField(
                value = projectPath,
                onValueChange = onProjectPathChange,
                placeholder = {
                    Text("Enter your project path (e.g., /path/to/your/project)")
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = { /* Browse folder */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Browse")
                }

                Button(
                    onClick = onAnalyze,
                    enabled = projectPath.isNotBlank() && !isLoading,
                    modifier = Modifier.weight(2f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Analyzing...")
                    } else {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Analyze Project")
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewContent(analysis: ProjectAnalysis) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Statistics cards
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                title = "Modules",
                value = analysis.modules.size.toString(),
                icon = Icons.Outlined.Folder,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Dependencies",
                value = analysis.dependencies.size.toString(),
                icon = Icons.Outlined.AccountTree,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Plugins",
                value = analysis.plugins.size.toString(),
                icon = Icons.Outlined.Extension,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        // Configuration breakdown
        ConfigurationBreakdown(analysis.dependencies)

        // Recent dependencies
        RecentItems(
            title = "Recent Dependencies",
            items = analysis.dependencies.take(6)
        ) { dependency ->
            CompactDependencyCard(dependency)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = color
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ConfigurationBreakdown(dependencies: List<Dependency>) {
    val configGroups = dependencies.groupBy { it.configuration }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Configuration Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            configGroups.forEach { (config, deps) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(getConfigColor(config))
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            config,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        deps.size.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun getConfigColor(config: String): Color {
    return when (config.lowercase()) {
        "implementation" -> MaterialTheme.colorScheme.primary
        "testimplementation" -> MaterialTheme.colorScheme.secondary
        "runtimeonly" -> MaterialTheme.colorScheme.tertiary
        "androidtestimplementation" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
}

@Composable
fun <T> RecentItems(
    title: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            items.forEach { item ->
                itemContent(item)
                if (item != items.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                count.toString(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    projectPath: String,
    onProjectPathChange: (String) -> Unit,
    selectedTab: AnalysisTab,
    analysisResult: ProjectAnalysis,
    isLoading: Boolean,
    onAnalyze: () -> Unit,
    showHeader: Boolean = false
) {
    val scrollState = rememberLazyListState()

    BoxWithConstraints(
        modifier = modifier.padding(24.dp)
    ) {
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize().padding(end = 12.dp)
        ) {
            // Header (only on compact screens)
            if (showHeader) {
                item {
                    HeaderSection()
                }
            }

            // Project input section
            item {
                ProjectInputSection(
                    projectPath = projectPath,
                    onProjectPathChange = onProjectPathChange,
                    isLoading = isLoading,
                    onAnalyze = onAnalyze
                )
            }

            // Tab content
            when (selectedTab) {
                AnalysisTab.Overview -> {
                    item {
                        OverviewContent(analysisResult)
                    }
                }

                AnalysisTab.Dependencies -> {
                    item {
                        SectionHeader("Dependencies", analysisResult.dependencies.size)
                    }
                    item {
                        DependenciesTable(analysisResult.dependencies)
                    }
                }

                AnalysisTab.Plugins -> {
                    item {
                        SectionHeader("Plugins", analysisResult.plugins.size)
                    }
                    item {
                        PluginsTable(analysisResult.plugins)
                    }
                }

                AnalysisTab.Modules -> {
                    item {
                        SectionHeader("Modules", analysisResult.modules.size)
                    }
                    items(analysisResult.modules) { module ->
                        ModuleCard(
                            module = module,
                            dependencyCount = analysisResult.dependencies.count { it.module == module },
                            pluginCount = analysisResult.plugins.count { it.module == module }
                        )
                    }
                }
            }
        }

        // Custom scrollbar
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
fun DependenciesTable(dependencies: List<Dependency>) {
    var sortColumn by remember { mutableStateOf("name") }
    var sortAscending by remember { mutableStateOf(true) }
    var expandedVersions by remember { mutableStateOf<Set<String>>(emptySet()) }

    val sortedDependencies = remember(dependencies, sortColumn, sortAscending) {
        val sorted = when (sortColumn) {
            "name" -> dependencies.sortedBy { "${it.group}:${it.name}" }
            "currentVersion" -> dependencies.sortedBy { it.currentVersion }
            "latestVersion" -> dependencies.sortedBy { it.latestVersion ?: "" }
            "configuration" -> dependencies.sortedBy { it.configuration }
            "module" -> dependencies.sortedBy { it.module }
            else -> dependencies
        }
        if (sortAscending) sorted else sorted.reversed()
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell(
                    title = "Dependency",
                    weight = 0.25f,
                    sortColumn = sortColumn,
                    currentSort = "name",
                    sortAscending = sortAscending,
                    onSort = {
                        if (sortColumn == "name") {
                            sortAscending = !sortAscending
                        } else {
                            sortColumn = "name"
                            sortAscending = true
                        }
                    }
                )
                TableHeaderCell(
                    title = "Current",
                    weight = 0.12f,
                    sortColumn = sortColumn,
                    currentSort = "currentVersion",
                    sortAscending = sortAscending,
                    onSort = {
                        if (sortColumn == "currentVersion") {
                            sortAscending = !sortAscending
                        } else {
                            sortColumn = "currentVersion"
                            sortAscending = true
                        }
                    }
                )
                TableHeaderCell(
                    title = "Latest",
                    weight = 0.12f,
                    sortColumn = sortColumn,
                    currentSort = "latestVersion",
                    sortAscending = sortAscending,
                    onSort = {
                        if (sortColumn == "latestVersion") {
                            sortAscending = !sortAscending
                        } else {
                            sortColumn = "latestVersion"
                            sortAscending = true
                        }
                    }
                )
                TableHeaderCell(
                    title = "Available Versions",
                    weight = 0.18f,
                    sortColumn = "",
                    currentSort = "",
                    sortAscending = true,
                    onSort = { }
                )
                TableHeaderCell(
                    title = "Config",
                    weight = 0.12f,
                    sortColumn = sortColumn,
                    currentSort = "configuration",
                    sortAscending = sortAscending,
                    onSort = {
                        if (sortColumn == "configuration") {
                            sortAscending = !sortAscending
                        } else {
                            sortColumn = "configuration"
                            sortAscending = true
                        }
                    }
                )
                TableHeaderCell(
                    title = "Module",
                    weight = 0.1f,
                    sortColumn = sortColumn,
                    currentSort = "module",
                    sortAscending = sortAscending,
                    onSort = {
                        if (sortColumn == "module") {
                            sortAscending = !sortAscending
                        } else {
                            sortColumn = "module"
                            sortAscending = true
                        }
                    }
                )
                Box(modifier = Modifier.weight(0.11f)) // Actions column
            }

            Spacer(Modifier.height(8.dp))

            // Table Rows
            sortedDependencies.forEachIndexed { index, dependency ->
                val dependencyKey = "${dependency.group}:${dependency.name}"
                DependencyTableRow(
                    dependency = dependency,
                    isEven = index % 2 == 0,
                    isExpanded = dependencyKey in expandedVersions,
                    onToggleExpanded = {
                        expandedVersions = if (dependencyKey in expandedVersions) {
                            expandedVersions - dependencyKey
                        } else {
                            expandedVersions + dependencyKey
                        }
                    }
                )
                if (index < sortedDependencies.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun PluginsTable(plugins: List<Plugin>) {
    var sortColumn by remember { mutableStateOf("id") }
    var sortAscending by remember { mutableStateOf(true) }
    var expandedVersions by remember { mutableStateOf<Set<String>>(emptySet()) }

    val sortedPlugins = remember(plugins, sortColumn, sortAscending) {
        val sorted = when (sortColumn) {
            "id" -> plugins.sortedBy { it.id }
            "currentVersion" -> plugins.sortedBy { it.currentVersion ?: "" }
            "latestVersion" -> plugins.sortedBy { it.latestVersion ?: "" }
            "module" -> plugins.sortedBy { it.module }
            else -> plugins
        }
        if (sortAscending) sorted else sorted.reversed()
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell(
                    title = "Plugin ID",
                    weight = 0.3f,
                    sortColumn = sortColumn,
                    currentSort = "id",
                    sortAscending = sortAscending,
                    onSort = {
                        if (sortColumn == "id") {
                            sortAscending = !sortAscending
                        } else {
                            sortColumn = "id"
                            sortAscending = true
                        }
                    }
                )
                TableHeaderCell(
                    title = "Current",
                    weight = 0.15f,
                    sortColumn = sortColumn,
                    currentSort = "currentVersion",
                    sortAscending = sortAscending,
                    onSort = {
                        if (sortColumn == "currentVersion") {
                            sortAscending = !sortAscending
                        } else {
                            sortColumn = "currentVersion"
                            sortAscending = true
                        }
                    }
                )
                TableHeaderCell(
                    title = "Latest",
                    weight = 0.15f,
                    sortColumn = sortColumn,
                    currentSort = "latestVersion",
                    sortAscending = sortAscending,
                    onSort = {
                        if (sortColumn == "latestVersion") {
                            sortAscending = !sortAscending
                        } else {
                            sortColumn = "latestVersion"
                            sortAscending = true
                        }
                    }
                )
                TableHeaderCell(
                    title = "Available Versions",
                    weight = 0.2f,
                    sortColumn = "",
                    currentSort = "",
                    sortAscending = true,
                    onSort = { }
                )
                TableHeaderCell(
                    title = "Module",
                    weight = 0.15f,
                    sortColumn = sortColumn,
                    currentSort = "module",
                    sortAscending = sortAscending,
                    onSort = {
                        if (sortColumn == "module") {
                            sortAscending = !sortAscending
                        } else {
                            sortColumn = "module"
                            sortAscending = true
                        }
                    }
                )
                Box(modifier = Modifier.weight(0.05f)) // Icon space
            }

            Spacer(Modifier.height(8.dp))

            // Table Rows
            sortedPlugins.forEachIndexed { index, plugin ->
                PluginTableRow(
                    plugin = plugin,
                    isEven = index % 2 == 0,
                    isExpanded = plugin.id in expandedVersions,
                    onToggleExpanded = {
                        expandedVersions = if (plugin.id in expandedVersions) {
                            expandedVersions - plugin.id
                        } else {
                            expandedVersions + plugin.id
                        }
                    }
                )
                if (index < sortedPlugins.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.TableHeaderCell(
    title: String,
    weight: Float,
    sortColumn: String,
    currentSort: String,
    sortAscending: Boolean,
    onSort: () -> Unit
) {
    Row(
        modifier = Modifier
            .weight(weight)
            .clickable { onSort() }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (sortColumn == currentSort) {
            Icon(
                if (sortAscending) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = "Sort direction",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DependencyTableRow(
    dependency: Dependency,
    isEven: Boolean,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val backgroundColor = if (isEven) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dependency name
            Column(
                modifier = Modifier.weight(0.25f).padding(end = 8.dp)
            ) {
                Text(
                    dependency.group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    dependency.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Current version
            Box(
                modifier = Modifier.weight(0.12f).padding(horizontal = 4.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        dependency.currentVersion,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Latest version
            Box(
                modifier = Modifier.weight(0.12f).padding(horizontal = 4.dp)
            ) {
                if (dependency.latestVersion != null) {
                    val isUpdateAvailable = dependency.currentVersion != dependency.latestVersion
                    Surface(
                        color = if (isUpdateAvailable) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isUpdateAvailable) {
                                Icon(
                                    Icons.Filled.Update,
                                    contentDescription = "Update available",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(
                                dependency.latestVersion,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isUpdateAvailable) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Text(
                        "Unknown",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Available versions (expandable)
            Box(
                modifier = Modifier.weight(0.18f).padding(horizontal = 4.dp)
            ) {
                if (dependency.availableVersions.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onToggleExpanded() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${dependency.availableVersions.size} versions",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                } else {
                    Text(
                        "No data",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Configuration
            Box(
                modifier = Modifier.weight(0.12f).padding(horizontal = 4.dp)
            ) {
                Surface(
                    color = getConfigColor(dependency.configuration).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        dependency.configuration,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = getConfigColor(dependency.configuration),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Module
            Box(
                modifier = Modifier.weight(0.1f).padding(horizontal = 4.dp)
            ) {
                Text(
                    dependency.module,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Actions
            Box(
                modifier = Modifier.weight(0.11f).padding(start = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row {
                    IconButton(
                        onClick = { /* Copy to clipboard */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    if (dependency.latestVersion != null && dependency.currentVersion != dependency.latestVersion) {
                        IconButton(
                            onClick = { /* Update action */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Update,
                                contentDescription = "Update available",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        // Expanded version list
        if (isExpanded && dependency.availableVersions.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Available Versions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))

                    // Version grid
                    val chunkedVersions =
                        dependency.availableVersions.chunked(6) // 6 versions per row
                    chunkedVersions.forEach { versionRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            versionRow.forEach { version ->
                                val isCurrent = version == dependency.currentVersion
                                val isLatest = version == dependency.latestVersion

                                Surface(
                                    color = when {
                                        isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                        isLatest -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .clickable { /* Select this version */ }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        when {
                                            isCurrent -> {
                                                Icon(
                                                    Icons.Filled.CheckCircle,
                                                    contentDescription = "Current version",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Spacer(Modifier.width(4.dp))
                                            }

                                            isLatest -> {
                                                Icon(
                                                    Icons.Filled.NewReleases,
                                                    contentDescription = "Latest version",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Spacer(Modifier.width(4.dp))
                                            }
                                        }

                                        Text(
                                            version,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = when {
                                                isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
                                                isLatest -> MaterialTheme.colorScheme.onSecondaryContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = if (isCurrent || isLatest) FontWeight.Medium else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            // Fill remaining space in the row
                            if (versionRow.size < 6) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    // Version info footer
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Current",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.NewReleases,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Latest",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PluginTableRow(
    plugin: Plugin,
    isEven: Boolean,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val backgroundColor = if (isEven) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plugin ID
            Box(
                modifier = Modifier.weight(0.3f).padding(end = 8.dp)
            ) {
                Text(
                    plugin.id,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Current version
            Box(
                modifier = Modifier.weight(0.15f).padding(horizontal = 4.dp)
            ) {
                if (plugin.currentVersion != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            plugin.currentVersion,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        "No version",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Latest version
            Box(
                modifier = Modifier.weight(0.15f).padding(horizontal = 4.dp)
            ) {
                if (plugin.latestVersion != null) {
                    val isUpdateAvailable = plugin.currentVersion != plugin.latestVersion
                    Surface(
                        color = if (isUpdateAvailable) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isUpdateAvailable) {
                                Icon(
                                    Icons.Filled.Update,
                                    contentDescription = "Update available",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(
                                plugin.latestVersion,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isUpdateAvailable) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Text(
                        "Unknown",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Available versions (expandable)
            Box(
                modifier = Modifier.weight(0.2f).padding(horizontal = 4.dp)
            ) {
                if (plugin.availableVersions.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onToggleExpanded() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${plugin.availableVersions.size} versions",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                } else {
                    Text(
                        "No data",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Module
            Box(
                modifier = Modifier.weight(0.15f).padding(horizontal = 4.dp)
            ) {
                Text(
                    plugin.module,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Plugin icon
            Box(
                modifier = Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Extension,
                    contentDescription = "Plugin",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                )
            }
        }

        // Expanded version list for plugins
        if (isExpanded && plugin.availableVersions.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Available Plugin Versions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))

                    // Version grid for plugins
                    val chunkedVersions =
                        plugin.availableVersions.chunked(5) // 5 versions per row for plugins
                    chunkedVersions.forEach { versionRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            versionRow.forEach { version ->
                                val isCurrent = version == plugin.currentVersion
                                val isLatest = version == plugin.latestVersion

                                Surface(
                                    color = when {
                                        isCurrent -> MaterialTheme.colorScheme.tertiaryContainer
                                        isLatest -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .clickable { /* Select this version */ }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        when {
                                            isCurrent -> {
                                                Icon(
                                                    Icons.Filled.CheckCircle,
                                                    contentDescription = "Current version",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                                Spacer(Modifier.width(4.dp))
                                            }

                                            isLatest -> {
                                                Icon(
                                                    Icons.Filled.NewReleases,
                                                    contentDescription = "Latest version",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Spacer(Modifier.width(4.dp))
                                            }
                                        }

                                        Text(
                                            version,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = when {
                                                isCurrent -> MaterialTheme.colorScheme.onTertiaryContainer
                                                isLatest -> MaterialTheme.colorScheme.onSecondaryContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = if (isCurrent || isLatest) FontWeight.Medium else FontWeight.Normal
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CompactDependencyCard(dependency: Dependency) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "${dependency.group}:${dependency.name}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                dependency.currentVersion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Surface(
            color = getConfigColor(dependency.configuration).copy(alpha = 0.1f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                dependency.configuration,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                color = getConfigColor(dependency.configuration)
            )
        }
    }
}

@Composable
fun ModuleCard(
    module: String,
    dependencyCount: Int,
    pluginCount: Int
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (module == "root") Icons.Filled.Home else Icons.Filled.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (module == "root") "Root Project" else module,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (module == "root") {
                            Text(
                                "Main project configuration",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.AccountTree,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$dependencyCount dependencies",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Extension,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$pluginCount plugins",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


@Composable
fun dynamicLightColorScheme(): ColorScheme {
    return lightColorScheme(
        primary = Color(0xFF6750A4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        tertiary = Color(0xFF7D5260),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFCAC4D0),
        surfaceContainer = Color(0xFFF3EDF7),
        surfaceContainerHigh = Color(0xFFECE6F0),
        surfaceContainerHighest = Color(0xFFE6E0E9)
    )
}

