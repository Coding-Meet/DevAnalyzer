package com.meet.dev.analyzer.presentation.screen.storage.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.core.utility.StorageSummarySection
import com.meet.dev.analyzer.data.models.storage.StorageAnalyzerInfo
import com.meet.dev.analyzer.data.models.storage.StorageBreakdown
import com.meet.dev.analyzer.data.models.storage.StorageBreakdownItem
import com.meet.dev.analyzer.presentation.components.SummaryExpandableSectionLayout
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.presentation.screen.storage.StorageAnalyzerUiState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OverviewTabContent(uiState: StorageAnalyzerUiState) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Total Storage Card
            item {
                TotalStorageCard(storageAnalyzerInfo = uiState.storageAnalyzerInfo)
            }
            // Quick Stats
            item {
                QuickStatsCard(storageAnalyzerInfo = uiState.storageAnalyzerInfo)
            }

            // StorageBreakdown Card
            if (uiState.storageAnalyzerInfo != null) {
                item {
                    StorageBreakdownCard(
                        storageBreakdown = uiState.storageAnalyzerInfo.storageBreakdown
                    )
                }
            }
        }
        VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
    }
}

@Composable
private fun QuickStatsCard(storageAnalyzerInfo: StorageAnalyzerInfo?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            title = "AVDs",
            value = (storageAnalyzerInfo?.androidAvdInfo?.avdItemList?.size
                ?: 0).toString(),
            icon = Icons.Default.PhoneAndroid,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "JDKs",
            value = (storageAnalyzerInfo?.gradleInfo?.jdkInfo?.jdkItems?.size ?: 0).toString(),
            icon = Icons.Default.Coffee,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "Libraries",
            value = (storageAnalyzerInfo?.gradleInfo?.gradleModulesInfo?.libraries?.size
                ?: 0).toString(),
            icon = Icons.AutoMirrored.Filled.LibraryBooks,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TotalStorageCard(storageAnalyzerInfo: StorageAnalyzerInfo?) {
    SummaryExpandableSectionLayout(
        expandableSection = StorageSummarySection.TotalSummary,
    ) {
        Text(
            text = storageAnalyzerInfo?.totalStorageUsed ?: "0 B",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.displayMedium,
            color = it.valueColor(),
            fontWeight = FontWeight.Bold
        )
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


@Composable
fun StorageBreakdownCard(storageBreakdown: StorageBreakdown) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .padding(6.dp)
                )
                // Storage Breakdown
                Text(
                    "Storage Breakdown",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(IntrinsicSize.Max)
            ) {
                // Pie Chart
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .wrapContentHeight()
                ) {
                    PieChart(
                        storageBreakdown = storageBreakdown,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    storageBreakdown.storageBreakdownItemList.forEach { storageBreakdownItem ->
                        ChartCard(storageBreakdownItem = storageBreakdownItem)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    storageBreakdownItem: StorageBreakdownItem,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            storageBreakdownItem.storageBreakdownItemColor.icon,
            contentDescription = null,
            tint = storageBreakdownItem.storageBreakdownItemColor.color,
            modifier = Modifier.size(20.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                storageBreakdownItem.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                storageBreakdownItem.percentageReadable,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                storageBreakdownItem.sizeReadable,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PieChart(
    storageBreakdown: StorageBreakdown,
    modifier: Modifier = Modifier
) {
    val total = storageBreakdown.totalSizeByte.toFloat()
    if (total <= 0f) return

    Canvas(modifier = modifier.padding(16.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2.5f

        var startAngle = -90f

        storageBreakdown.storageBreakdownItemList.forEach { item ->
            val sweepAngle = (item.sizeByte / total) * 360f

            drawArc(
                color = item.storageBreakdownItemColor.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Draw white separator lines
            val separatorAngle = Math.toRadians((startAngle + sweepAngle).toDouble())
            val endX = center.x + radius * cos(separatorAngle).toFloat()
            val endY = center.y + radius * sin(separatorAngle).toFloat()

            drawLine(
                color = Color.White,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )

            startAngle += sweepAngle
        }

        // Center circle for donut effect
        drawCircle(
            color = Color.White,
            radius = radius * 0.4f,
            center = center
        )

        drawCircle(
            color = Color(0xFFF5F5F5),
            radius = radius * 0.35f,
            center = center
        )
    }
}