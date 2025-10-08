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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.Utils
import com.meet.project.analyzer.core.utility.Utils.openFile
import com.meet.project.analyzer.data.models.GradleCacheInfo
import com.meet.project.analyzer.presentation.components.EmptyStateCard
import com.meet.project.analyzer.presentation.components.SummaryStatItem
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.project.analyzer.presentation.screen.storage.StorageAnalyzerUiState
import java.awt.Cursor

@Composable
fun GradleCachesTabContent(
    uiState: StorageAnalyzerUiState,
    onLoadGradleCaches: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 350.dp),
            state = scrollState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.gradleCaches.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GradleCacheSummaryCard(uiState.gradleCaches)
                }
                items(uiState.gradleCaches) { cache ->
                    GradleCacheInfoCard(cache)
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCard(
                        title = "No Gradle Caches Found",
                        description = "No Gradle version caches found",
                        icon = Icons.Default.Build,
                        actionText = "Scan for Caches",
                        onAction = onLoadGradleCaches
                    )
                }
            }
        }
        VerticalScrollBarLayout(rememberScrollbarAdapter(scrollState))
    }
}

@Composable
fun GradleCacheSummaryCard(caches: List<GradleCacheInfo>) {
    val totalSize = caches.sumOf { it.sizeBytes }
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
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    "Gradle Caches",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem("Cache Versions", caches.size.toString())
                SummaryStatItem("Total Size", Utils.formatSize(totalSize))
            }
        }
    }
}

@Composable
fun GradleCacheInfoCard(cache: GradleCacheInfo) {
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
                    "Gradle ${cache.version}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    cache.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .clickable {
                            cache.path.openFile()
                        }
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    cache.sizeReadable,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}