package com.meet.project.analyzer.presentation.screen.storage.components

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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.Utils
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.project.analyzer.presentation.screen.storage.StorageAnalyzerUiState

@Composable
fun OverviewTabContent(uiState: StorageAnalyzerUiState) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            state = scrollState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Storage Card
            item(span = { GridItemSpan(maxLineSpan) }) {
                OverviewContent(uiState)
            }
        }
        VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
    }
}

@Composable
fun OverviewContent(uiState: StorageAnalyzerUiState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Total Storage Card
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 12.dp)
                    )
                    Text(
                        "Total Storage Used",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    uiState.totalStorageUsed,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Quick Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickStatCard(
                title = "AVDs",
                value = uiState.avds.size.toString(),
                icon = Icons.Default.PhoneAndroid,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                title = "JDKs",
                value = (uiState.devEnvironmentInfo?.jdks?.size ?: 0).toString(),
                icon = Icons.Default.Coffee,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                title = "Libraries",
                value = (uiState.gradleModulesInfo?.libraries?.size ?: 0).toString(),
                icon = Icons.AutoMirrored.Filled.LibraryBooks,
                modifier = Modifier.weight(1f)
            )
        }

        // Storage Breakdown
        uiState.devEnvironmentInfo?.let { devEnv ->
            Text(
                "Storage Breakdown",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            StorageBreakdownCard(
                items = listOf(
                    BreakdownItem(
                        name = "Gradle Cache",
                        sizeByte = devEnv.gradleCache.sizeBytes,
                        sizeReadable = devEnv.gradleCache.sizeReadable,
                        icon = Icons.Default.Folder
                    ),
                    BreakdownItem(
                        name = "IDE Cache",
                        sizeByte = devEnv.ideaCache.sizeBytes,
                        sizeReadable = devEnv.ideaCache.sizeReadable,
                        icon = Icons.Default.Code
                    ),
                    BreakdownItem(
                        name = "Konan Cache",
                        sizeByte = devEnv.konanInfo.sizeBytes,
                        sizeReadable = devEnv.konanInfo.sizeReadable,
                        icon = Icons.Default.Memory
                    ),
                    BreakdownItem(
                        name = "Skiko Cache",
                        sizeByte = devEnv.skikoInfo.sizeBytes,
                        sizeReadable = devEnv.skikoInfo.sizeReadable,
                        icon = Icons.Default.Brush
                    ),
                    BreakdownItem(
                        name = "SDK",
                        sizeByte = uiState.sdkInfo?.totalSizeBytes ?: 0L,
                        sizeReadable = uiState.sdkInfo?.totalSize ?: "0 B",
                        icon = Icons.Default.Android
                    ),
                    BreakdownItem(
                        name = "AVDs",
                        sizeByte = uiState.avds.sumOf { it.sizeBytes },
                        sizeReadable = Utils.formatSize(uiState.avds.sumOf { it.sizeBytes }),
                        icon = Icons.Default.PhoneAndroid
                    ),
                ).sortedByDescending {
                    it.sizeByte
                }
            )
        }
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class BreakdownItem(
    val name: String,
    val sizeByte: Long,
    val sizeReadable: String,
    val icon: ImageVector
)

@Composable
fun StorageBreakdownCard(items: List<BreakdownItem>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        item.name,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            item.sizeReadable,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}