package com.meet.dev.analyzer.presentation.screen.project.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.data.models.project.Dependency
import com.meet.dev.analyzer.data.models.project.ModuleBuildFileInfo
import com.meet.dev.analyzer.data.models.project.Plugin
import com.meet.dev.analyzer.data.models.project.ProjectInfo
import com.meet.dev.analyzer.data.models.project.ProjectOverviewInfo
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProjectOverviewCard(
                    projectOverviewInfo = projectInfo.projectOverviewInfo,
                    dependencies = projectInfo.dependencies,
                    plugins = projectInfo.plugins,
                    moduleBuildFileInfos = projectInfo.moduleBuildFileInfos,
                    projectFilesSize = projectInfo.projectFiles.size
                )
            }
            item {
                ProjectConfigurationCard(projectOverviewInfo = projectInfo.projectOverviewInfo)
            }
            item {
                ProjectPlatformsCard(projectOverviewInfo = projectInfo.projectOverviewInfo)
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
    projectFilesSize: Int
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                columns = GridCells.Fixed(5),
                modifier = Modifier.height(90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ProjectStatItem(
                        label = "Size",
                        value = projectOverviewInfo.sizeReadable,
                        icon = Icons.Default.Storage
                    )
                }
                item {
                    ProjectStatItem(
                        label = "Files",
                        value = projectFilesSize.toString(),
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
        }
    }
}

@Composable
private fun ProjectConfigurationCard(projectOverviewInfo: ProjectOverviewInfo) {
    val configurationItems = listOf(
        ProjectDetail("Gradle", projectOverviewInfo.gradleVersion),
        ProjectDetail("Kotlin", projectOverviewInfo.kotlinVersion),
        ProjectDetail("AGP", projectOverviewInfo.androidGradlePluginVersion),
        ProjectDetail("Min SDK", projectOverviewInfo.minSdkVersion),
        ProjectDetail("Compile SDK", projectOverviewInfo.compileSdkVersion),
        ProjectDetail("Target SDK", projectOverviewInfo.targetSdkVersion),
        ProjectDetail("Build Tools", projectOverviewInfo.buildToolsSdk),
        ProjectDetail("Multi-Module", if (projectOverviewInfo.isMultiModule) "Yes" else "No"),
        ProjectDetail("NDK", projectOverviewInfo.ndkVersion),
        ProjectDetail("CMake", projectOverviewInfo.cmakeVersion)
    )

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle(
                title = "Build Configuration",
                icon = Icons.Default.Code
            )
            DetailGrid(
                items = configurationItems,
                columnCount = 5
            )
        }

    }
}

@Composable
private fun ProjectPlatformsCard(
    projectOverviewInfo: ProjectOverviewInfo
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle(
                title = "Platforms",
                icon = Icons.Default.Android
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (projectOverviewInfo.platformList.isEmpty()) {
                    ProjectPill(text = "No platform detected")
                } else {
                    projectOverviewInfo.platformList.forEach { platform ->
                        ProjectPill(text = platform)
                    }
                }
            }
        }
    }
}


@Composable
private fun DetailGrid(
    items: List<ProjectDetail>,
    columnCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(columnCount).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    DetailTile(
                        label = item.label,
                        value = item.value.orEmpty().ifBlank { "Not found" },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columnCount - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
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
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
private fun DetailTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(72.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProjectPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1
        )
    }
}

private data class ProjectDetail(
    val label: String,
    val value: String?
)
