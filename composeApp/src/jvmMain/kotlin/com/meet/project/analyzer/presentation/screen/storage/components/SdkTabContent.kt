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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.Utils.openFile
import com.meet.project.analyzer.data.models.SdkInfo
import com.meet.project.analyzer.data.models.SdkItem
import com.meet.project.analyzer.presentation.components.EmptyStateCard
import com.meet.project.analyzer.presentation.components.SummaryStatItem
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.project.analyzer.presentation.screen.storage.StorageAnalyzerUiState
import java.awt.Cursor

@Composable
fun SdkTabContent(
    uiState: StorageAnalyzerUiState,
    onLoadSdk: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.sdkInfo?.let { sdk ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SdkSummaryCard(sdk)
                }

                // Platforms section
                if (sdk.platforms.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SummaryStatItem("Platforms", sdk.platforms.size.toString())
                    }
                    items(sdk.platforms) { platform ->
                        SdkItemCard(platform, Icons.Default.Android)
                    }
                }

                // Build Tools section
                if (sdk.buildTools.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SummaryStatItem("Build Tools", sdk.buildTools.size.toString())
                    }
                    items(sdk.buildTools) { buildTool ->
                        SdkItemCard(buildTool, Icons.Default.Build)
                    }
                }

                // System Images section
                if (sdk.systemImages.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SummaryStatItem("System Images", sdk.systemImages.size.toString())
                    }
                    items(sdk.systemImages) { image ->
                        SdkItemCard(image, Icons.Default.Image)
                    }
                }

                // Extras section
                if (sdk.extras.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SummaryStatItem("Extras", sdk.extras.size.toString())
                    }
                    items(sdk.extras) { extra ->
                        SdkItemCard(extra, Icons.Default.Extension)
                    }
                }
            } ?: run {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCard(
                        title = "No SDK Found",
                        description = "Android SDK not found on this system",
                        icon = Icons.Default.Android,
                        actionText = "Scan for SDK",
                        onAction = onLoadSdk
                    )
                }
            }
        }
        VerticalScrollBarLayout(rememberScrollbarAdapter(scrollState))
    }
}

@Composable
fun SdkSummaryCard(sdk: SdkInfo) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Android,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    "Android SDK",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Path: ${sdk.sdkPath}", style = MaterialTheme.typography.bodyMedium)
                Text("Total Size: ${sdk.totalSize}", style = MaterialTheme.typography.bodyMedium)
                Text("Free Space: ${sdk.freeSpace}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Platforms: ${sdk.platforms.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Build Tools: ${sdk.buildTools.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SdkItemCard(item: SdkItem, icon: ImageVector) {
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp).size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    item.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .clickable {
                            item.path.openFile()
                        }
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    item.size,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}