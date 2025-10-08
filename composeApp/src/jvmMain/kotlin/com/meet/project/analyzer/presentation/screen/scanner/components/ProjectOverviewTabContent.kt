package com.meet.project.analyzer.presentation.screen.scanner.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.data.models.scanner.Dependency
import com.meet.project.analyzer.data.models.scanner.ModuleBuildFileInfo
import com.meet.project.analyzer.data.models.scanner.Plugin
import com.meet.project.analyzer.data.models.scanner.ProjectInfo
import com.meet.project.analyzer.data.models.scanner.ProjectOverviewInfo
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout

@Composable
fun ProjectOverviewTabContent(
    projectInfo: ProjectInfo
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProjectOverviewCard(
                    projectOverviewInfo = projectInfo.projectOverviewInfo,
                    dependencies = projectInfo.dependencies,
                    plugins = projectInfo.plugins,
                    moduleBuildFileInfos = projectInfo.moduleBuildFileInfos,
                )
            }
        }

        VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))

    }
}

@Composable
fun ProjectOverviewCard(
    projectOverviewInfo: ProjectOverviewInfo,
    dependencies: List<Dependency>,
    plugins: List<Plugin>,
    moduleBuildFileInfos: List<ModuleBuildFileInfo>,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Project header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Android,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    projectOverviewInfo.projectName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Stats grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ProjectStatItem(
                        label = "Size",
                        value = projectOverviewInfo.totalSize,
                        icon = Icons.Default.Storage
                    )
                }
                item {
                    ProjectStatItem(
                        label = "Modules",
                        value = moduleBuildFileInfos.size.toString(),
                        icon = Icons.Default.AccountTree
                    )
                }
                item {
                    ProjectStatItem(
                        label = "Plugins",
                        value = plugins.size.toString(),
                        icon = Icons.Default.Extension
                    )
                }
                item {
                    ProjectStatItem(
                        label = "Dependencies",
                        value = dependencies.size.toString(),
                        icon = Icons.AutoMirrored.Filled.LibraryBooks
                    )
                }
            }

            // Version information if available
            val versions =
                remember {
                    buildList {
                        projectOverviewInfo.gradleVersion?.let { add("Gradle $it") }
                        projectOverviewInfo.kotlinVersion?.let { add("Kotlin $it") }
                        projectOverviewInfo.androidGradlePluginVersion?.let { add("AGP $it") }
                        projectOverviewInfo.minSdkVersion?.let { add("Min SDK $it") }
                        projectOverviewInfo.compileSdkVersion?.let { add("Compile SDK $it") }
                        projectOverviewInfo.targetSdkVersion?.let { add("Target SDK $it") }
                        if (projectOverviewInfo.isMultiModule) {
                            add("Multi-Module")
                        }
                    }
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