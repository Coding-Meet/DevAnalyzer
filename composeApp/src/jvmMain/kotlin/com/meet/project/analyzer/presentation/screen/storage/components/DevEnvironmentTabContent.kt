package com.meet.project.analyzer.presentation.screen.storage.components

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.Utils.openFile
import com.meet.project.analyzer.data.models.DevEnvironmentInfo
import com.meet.project.analyzer.data.models.GradleWrapperInfo
import com.meet.project.analyzer.data.models.JdkInfo
import com.meet.project.analyzer.data.models.KonanInfo
import com.meet.project.analyzer.data.models.StorageInfo
import com.meet.project.analyzer.presentation.components.EmptyStateCard
import com.meet.project.analyzer.presentation.components.SummaryStatItem
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.project.analyzer.presentation.screen.storage.StorageAnalyzerUiState
import java.awt.Cursor

@Composable
fun DevEnvironmentTabContent(
    uiState: StorageAnalyzerUiState,
    onLoadDevEnv: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            state = scrollState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.devEnvironmentInfo?.let { devEnv ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DevEnvironmentSummaryCard(devEnv)
                }

                // Storage Info Cards
                item {
                    StorageInfoCard(devEnv.gradleCache, Icons.Default.Folder)
                }
                item {
                    StorageInfoCard(devEnv.ideaCache, Icons.Default.Code)
                }

                // JDKs
                if (devEnv.jdks.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SummaryStatItem("JDKs", devEnv.jdks.size.toString())
                    }
                    items(devEnv.jdks) { jdk ->
                        JdkInfoCard(jdk)
                    }
                }

                // Konan versions
                if (devEnv.konanInfos.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SummaryStatItem("Kotlin/Native", devEnv.konanInfos.size.toString())
                    }
                    items(devEnv.konanInfos) { konan ->
                        KonanInfoCard(konan)
                    }
                }

                // Gradle Wrappers
                if (devEnv.gradleWrapperInfos.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SummaryStatItem(
                            "Gradle Wrappers",
                            devEnv.gradleWrapperInfos.size.toString()
                        )
                    }
                    items(devEnv.gradleWrapperInfos) { wrapper ->
                        GradleWrapperInfoCard(wrapper)
                    }
                }
            } ?: run {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCard(
                        title = "No Dev Environment Data",
                        description = "Development environment information not loaded",
                        icon = Icons.Default.DeveloperMode,
                        actionText = "Load Dev Environment",
                        onAction = onLoadDevEnv
                    )
                }
            }

        }
        VerticalScrollBarLayout(rememberScrollbarAdapter(scrollState))
    }
}


@Composable
fun DevEnvironmentSummaryCard(devEnv: DevEnvironmentInfo) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DeveloperMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    "Development Environment",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem("JDKs", devEnv.jdks.size.toString())
                SummaryStatItem("Konan Versions", devEnv.konanInfos.size.toString())
                SummaryStatItem("Gradle Wrappers", devEnv.gradleWrapperInfos.size.toString())
            }
        }
    }
}

@Composable
fun StorageInfoCard(storage: StorageInfo, icon: ImageVector) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (storage.exists) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(end = 12.dp).size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    storage.path,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .clickable {
                            storage.path.openFile()
                        }
                )
                Text(
                    if (storage.exists) "Exists" else "Not Found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (storage.exists) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            if (storage.exists) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        storage.sizeReadable,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KonanInfoCard(konan: KonanInfo) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Memory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp).size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Kotlin/Native ${konan.version ?: "Unknown"}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    konan.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .clickable {
                            konan.path.openFile()
                        }
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    konan.sizeReadable,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun JdkInfoCard(jdk: JdkInfo) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Coffee,
                contentDescription = null,
                tint = Color(0xFFED8B00),
                modifier = Modifier.padding(end = 12.dp).size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "JDK ${jdk.version ?: "Unknown"}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    jdk.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .clickable {
                            jdk.path.openFile()
                        }
                )
            }
            Surface(
                color = Color(0xFFED8B00).copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    jdk.sizeReadable,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFED8B00),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun GradleWrapperInfoCard(wrapper: GradleWrapperInfo) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Build,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp).size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Gradle ${wrapper.version}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    wrapper.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .clickable {
                            wrapper.path.openFile()
                        }
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    wrapper.sizeReadable,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
