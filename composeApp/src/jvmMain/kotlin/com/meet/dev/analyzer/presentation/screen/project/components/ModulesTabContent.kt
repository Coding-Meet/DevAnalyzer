package com.meet.dev.analyzer.presentation.screen.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.data.models.project.Dependency
import com.meet.dev.analyzer.data.models.project.ModuleBuildFileInfo
import com.meet.dev.analyzer.data.models.project.Plugin
import com.meet.dev.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import java.awt.Cursor


@Composable
fun ModulesTabContent(
    moduleBuildFileInfos: List<ModuleBuildFileInfo>,
    projectName: String,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()
        LazyVerticalGrid(
            state = scrollState,
            columns = GridCells.Adaptive(minSize = 360.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (moduleBuildFileInfos.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ModulesSummaryCard(moduleBuildFileInfos = moduleBuildFileInfos)
                }
            }

            items(
                items = moduleBuildFileInfos,
                key = { module -> module.uniqueId }
            ) { module ->
                DetailedModuleCard(
                    moduleName = module.moduleName,
                    sizeReadable = module.sizeReadable,
                    plugins = module.plugins,
                    fileName = module.type.fileName,
                    dependencies = module.dependencies,
                    projectName = projectName,
                    modulePath = module.modulePath,
                    isRootBuildFile = module.isRootBuildFile
                )
            }

            if (moduleBuildFileInfos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = "Module",
                        description = "No modules files found",
                        icon = Icons.Default.Extension
                    )
                }
            }
        }
        VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
    }
}

@Composable
private fun ModulesSummaryCard(moduleBuildFileInfos: List<ModuleBuildFileInfo>) {
    val totalSize = FolderFileUtils.formatSize(moduleBuildFileInfos.sumOf { it.sizeBytes })
    val pluginCount = moduleBuildFileInfos.sumOf { it.plugins.size }
    val dependencyCount = moduleBuildFileInfos.sumOf { it.dependencies.size }
    val configurationCount = moduleBuildFileInfos
        .flatMap { module -> module.plugins.map { it.configuration } + module.dependencies.map { it.configuration } }
        .distinct()
        .size

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Modules Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModuleSummaryTile(
                    label = "Modules",
                    value = moduleBuildFileInfos.size.toString(),
                    icon = Icons.Default.AccountTree,
                    modifier = Modifier.weight(1f)
                )
                ModuleSummaryTile(
                    label = "Total Size",
                    value = totalSize,
                    icon = Icons.Default.Folder,
                    modifier = Modifier.weight(1f)
                )
                ModuleSummaryTile(
                    label = "Plugins",
                    value = pluginCount.toString(),
                    icon = Icons.Default.Plumbing,
                    modifier = Modifier.weight(1f)
                )
                ModuleSummaryTile(
                    label = "Dependencies",
                    value = dependencyCount.toString(),
                    icon = Icons.Default.DataObject,
                    modifier = Modifier.weight(1f)
                )
                ModuleSummaryTile(
                    label = "Configurations",
                    value = configurationCount.toString(),
                    icon = Icons.Default.Extension,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModuleSummaryTile(
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
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Column {
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun DetailedModuleCard(
    moduleName: String,
    sizeReadable: String,
    fileName: String,
    plugins: List<Plugin>,
    dependencies: List<Dependency>,
    projectName: String,
    modulePath: String,
    isRootBuildFile: Boolean,
) {

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = getModuleIcon(moduleName = moduleName, projectName = projectName),
                    contentDescription = moduleName,
                    tint = getModuleColor(moduleName = moduleName, projectName = projectName),
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            getModuleColor(moduleName = moduleName, projectName = projectName)
                                .copy(alpha = 0.12f),
                            CircleShape
                        )
                        .padding(7.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = moduleName
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = modulePath.ifBlank { fileName }.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.pointerHoverIcon(
                            PointerIcon(
                                Cursor.getPredefinedCursor(
                                    Cursor.HAND_CURSOR
                                )
                            )
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = sizeReadable,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    if (isRootBuildFile) {
                        Text(
                            "Root",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            if (plugins.isNotEmpty() || dependencies.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (plugins.isNotEmpty()) {
                        ModuleStatChip(
                            "${plugins.size} Plugins",
                            Icons.Default.Plumbing
                        )
                    }
                    if (dependencies.isNotEmpty()) {
                        ModuleStatChip(
                            "${dependencies.size} Dependencies",
                            Icons.Default.AccountTree
                        )
                    }
                }
            }
            // Configuration breakdown section
            ConfigurationBreakdownSection(
                plugins = plugins,
                dependencies = dependencies
            )

            // Plugins section
//            PluginsSection(plugins = plugins)

            // Dependencies section
//            DependenciesSection(dependencies = dependencies)
        }
    }
}

@Composable
private fun DependenciesSection(dependencies: List<Dependency>) {
    if (dependencies.isNotEmpty()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        var isExpanded by rememberSaveable { mutableStateOf(false) }

        val dependencyList =
            if (isExpanded) dependencies else dependencies.take(3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Dependencies (${dependencies.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            if (dependencies.size > 3) {
                TextButton(
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                            )
                        )
                    ),
                    onClick = { isExpanded = !isExpanded }
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

        dependencyList.forEach { dependency ->
            ModuleRow(
                name = dependency.name,
                type = dependency.configuration,
                version = dependency.version,
                group = dependency.group,
                versionName = dependency.versionName
            )
        }
    }
}

@Composable
private fun PluginsSection(plugins: List<Plugin>) {
    if (plugins.isNotEmpty()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Plugins (${plugins.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        plugins.forEach { plugin ->
            ModuleRow(
                name = plugin.name,
                type = "plugin",
                version = plugin.version,
                group = plugin.group,
                versionName = plugin.id
            )
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
fun ConfigurationBreakdownSection(
    plugins: List<Plugin>,
    dependencies: List<Dependency>
) {
    val configGroupsWithPlugins = plugins.groupBy { it.configuration }
    val configGroupsWithDepList = dependencies.groupBy { it.configuration }

    if (configGroupsWithDepList.isNotEmpty() || configGroupsWithPlugins.isNotEmpty()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Configuration (${configGroupsWithDepList.size + configGroupsWithPlugins.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            configGroupsWithPlugins.forEach { (config, plugins) ->
                ConfigurationEachLayout(config = config, size = plugins.size.toString())
            }
            configGroupsWithDepList.forEach { (config, dependencyList) ->
                ConfigurationEachLayout(config = config, size = dependencyList.size.toString())
            }
        }
    }
}

@Composable
private fun ConfigurationEachLayout(
    config: String,
    size: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                config,
                style = MaterialTheme.typography.bodyMedium,
                color = getDependencyTypeColor(config),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                size,
                style = MaterialTheme.typography.bodyMedium,
                color = getDependencyTypeColor(config),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ModuleRow(
    name: String,
    type: String,
    version: String?,
    group: String,
    versionName: String,
) {
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
                    getDependencyTypeColor(type),
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {

            Text(
                group,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                versionName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            color = getDependencyTypeColor(type).copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Text(
                type,
                style = MaterialTheme.typography.labelSmall,
                color = getDependencyTypeColor(type),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            version ?: "N/A",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
