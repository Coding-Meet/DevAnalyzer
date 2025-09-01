package com.meet.project.analyzer.presentation.screen.storage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meet.project.analyzer.core.utility.StorageUtils
import com.meet.project.analyzer.data.models.AvdInfo
import com.meet.project.analyzer.data.models.DevEnvironmentInfo
import com.meet.project.analyzer.data.models.GradleCacheInfo
import com.meet.project.analyzer.data.models.GradleLibraryInfo
import com.meet.project.analyzer.data.models.GradleModulesInfo
import com.meet.project.analyzer.data.models.GradleWrapperInfo
import com.meet.project.analyzer.data.models.JdkInfo
import com.meet.project.analyzer.data.models.KonanInfo
import com.meet.project.analyzer.data.models.SdkInfo
import com.meet.project.analyzer.data.models.SdkItem
import com.meet.project.analyzer.data.models.StorageInfo
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAnalyzerContent(
    uiState: StorageAnalyzerUiState,
    onRefresh: () -> Unit,
    onLoadAvds: () -> Unit,
    onLoadSdk: () -> Unit,
    onLoadDevEnv: () -> Unit,
    onLoadGradleCaches: () -> Unit,
    onLoadGradleModules: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs =
        listOf("Overview", "AVDs", "SDK", "Dev Environment", "Gradle Caches", "Libraries", "Charts")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Storage Analyzer",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            actions = {
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )

        // Loading indicator
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Tab Row
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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

        // Content based on selected tab
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Error display
            uiState.error?.let { error ->
                item {
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // Overview Tab
                    item {
                        OverviewContent(uiState)
                    }
                }

                1 -> {
                    // AVDs Tab
                    if (uiState.avds.isNotEmpty()) {
                        item {
                            AvdSummaryCard(uiState.avds)
                        }
                        items(uiState.avds) { avd ->
                            AvdDetailCard(avd)
                        }
                    } else {
                        item {
                            EmptyStateCard(
                                title = "No AVDs Found",
                                description = "No Android Virtual Devices found on this system",
                                icon = Icons.Default.PhoneAndroid,
                                actionText = "Scan for AVDs",
                                onAction = onLoadAvds
                            )
                        }
                    }
                }

                2 -> {
                    // SDK Tab
                    uiState.sdkInfo?.let { sdk ->
                        item {
                            SdkSummaryCard(sdk)
                        }
                        if (sdk.platforms.isNotEmpty()) {
                            item {
                                Text(
                                    "Platforms",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(sdk.platforms) { platform ->
                                SdkItemCard(platform, Icons.Default.Android)
                            }
                        }
                        if (sdk.buildTools.isNotEmpty()) {
                            item {
                                Text(
                                    "Build Tools",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(sdk.buildTools) { buildTool ->
                                SdkItemCard(buildTool, Icons.Default.Build)
                            }
                        }
                        if (sdk.systemImages.isNotEmpty()) {
                            item {
                                Text(
                                    "System Images",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(sdk.systemImages) { image ->
                                SdkItemCard(image, Icons.Default.Image)
                            }
                        }
                        if (sdk.extras.isNotEmpty()) {
                            item {
                                Text(
                                    "Extras",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(sdk.extras) { extra ->
                                SdkItemCard(extra, Icons.Default.Extension)
                            }
                        }
                    } ?: item {
                        EmptyStateCard(
                            title = "No SDK Found",
                            description = "Android SDK not found on this system",
                            icon = Icons.Default.Android,
                            actionText = "Scan for SDK",
                            onAction = onLoadSdk
                        )
                    }
                }

                3 -> {
                    // Dev Environment Tab
                    uiState.devEnvironmentInfo?.let { devEnv ->
                        item {
                            DevEnvironmentSummaryCard(devEnv)
                        }

                        // Gradle Cache
                        item {
                            Text(
                                "Gradle Cache",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        item {
                            StorageInfoCard(devEnv.gradleCache, Icons.Default.Folder)
                        }

                        // IDE Cache
                        item {
                            Text(
                                "IDE Cache",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        item {
                            StorageInfoCard(devEnv.ideaCache, Icons.Default.Code)
                        }

                        // Konan
                        if (devEnv.konanInfos.isNotEmpty()) {
                            item {
                                Text(
                                    "Kotlin/Native (Konan)",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(devEnv.konanInfos) { konan ->
                                KonanInfoCard(konan)
                            }
                        }

                        // JDKs
                        if (devEnv.jdks.isNotEmpty()) {
                            item {
                                Text(
                                    "JDKs (${devEnv.jdks.size})",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(devEnv.jdks) { jdk ->
                                JdkInfoCard(jdk)
                            }
                        }

                        // Gradle Wrappers
                        if (devEnv.gradleWrapperInfos.isNotEmpty()) {
                            item {
                                Text(
                                    "Gradle Wrappers",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(devEnv.gradleWrapperInfos) { wrapper ->
                                GradleWrapperInfoCard(wrapper)
                            }
                        }
                    } ?: item {
                        EmptyStateCard(
                            title = "No Dev Environment Data",
                            description = "Development environment information not loaded",
                            icon = Icons.Default.DeveloperMode,
                            actionText = "Load Dev Environment",
                            onAction = onLoadDevEnv
                        )
                    }
                }

                4 -> {
                    // Gradle Caches Tab
                    if (uiState.gradleCaches.isNotEmpty()) {
                        item {
                            GradleCacheSummaryCard(uiState.gradleCaches)
                        }
                        items(uiState.gradleCaches) { cache ->
                            GradleCacheInfoCard(cache)
                        }
                    } else {
                        item {
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

                5 -> {
                    // Libraries Tab
                    uiState.gradleModulesInfo?.let { modules ->
                        item {
                            LibrariesSummaryCard(modules)
                        }
                        if (modules.libraries.isNotEmpty()) {
                            items(modules.libraries) { library ->
                                LibraryInfoCard(library)
                            }
                        }
                    } ?: item {
                        EmptyStateCard(
                            title = "No Libraries Found",
                            description = "No Gradle modules information available",
                            icon = Icons.Default.LibraryBooks,
                            actionText = "Load Libraries",
                            onAction = onLoadGradleModules
                        )
                    }
                }

                6 -> {
                    // Charts Tab
                    // Storage Distribution Pie Chart
                    item() {
                        StorageDistributionChart(uiState)
                    }

                    // AVDs Size Comparison Bar Chart
                    if (uiState.avds.isNotEmpty()) {
                        item {
                            AvdSizeChart(uiState.avds)
                        }
                    }

                    // SDK Components Chart
                    uiState.sdkInfo?.let { sdk ->
                        item {
                            SdkComponentsChart(sdk)
                        }
                    }

                    // JDK Versions Distribution
                    uiState.devEnvironmentInfo?.jdks?.let { jdks ->
                        if (jdks.isNotEmpty()) {
                            item {
                                JdkVersionsChart(jdks)
                            }
                        }
                    }

                    // Gradle Cache Versions Timeline
                    if (uiState.gradleCaches.isNotEmpty()) {
                        item() {
                            GradleCacheTimelineChart(uiState.gradleCaches)
                        }
                    }

                    // Development Tools Overview
                    item() {
                        DevelopmentToolsOverviewChart(uiState)
                    }

                    // Storage Growth Trend
                    item {
                        StorageGrowthTrendChart()
                    }

                    // Cache Efficiency Chart
                    if (uiState.gradleModulesInfo != null) {
                        item {
                            CacheEfficiencyChart(uiState.gradleModulesInfo!!)
                        }
                    }
                }
            }
        }
    }
}

// ===========================================
// UI COMPONENTS
// ===========================================
//
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
                icon = Icons.Default.LibraryBooks,
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
                        "Gradle Cache",
                        devEnv.gradleCache.sizeReadable,
                        Icons.Default.Folder
                    ),
                    BreakdownItem("IDE Cache", devEnv.ideaCache.sizeReadable, Icons.Default.Code),
                    BreakdownItem(
                        "Konan Cache",
                        devEnv.konanInfo.sizeReadable,
                        Icons.Default.Memory
                    ),
                    BreakdownItem(
                        "Skiko Cache",
                        devEnv.skikoInfo.sizeReadable,
                        Icons.Default.Brush
                    ),
                    BreakdownItem(
                        "SDK",
                        uiState.sdkInfo?.totalSize ?: "0 B",
                        Icons.Default.Android
                    ),
                    BreakdownItem(
                        "AVDs",
                        StorageUtils.formatSize(uiState.avds.sumOf { it.sizeBytes }),
                        Icons.Default.PhoneAndroid
                    )
                )
            )
        }
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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

data class BreakdownItem(val name: String, val size: String, val icon: ImageVector)

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
                            item.size,
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

@Composable
fun AvdSummaryCard(avds: List<AvdInfo>) {
    val totalSize = avds.sumOf { it.sizeBytes }
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    "AVD Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem("Total AVDs", avds.size.toString())
                SummaryStatItem("Total Size", StorageUtils.formatSize(totalSize))
            }
        }
    }
}

@Composable
fun SummaryStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AvdDetailCard(avd: AvdInfo) {
    OutlinedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp).size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        avd.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    avd.device?.let { device ->
                        Text(
                            device,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    avd.apiLevel?.let { api ->
                        Text(
                            api,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        avd.actualStorage,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                avd.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
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
                    maxLines = 1
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
                    fontWeight = FontWeight.Medium
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
                    maxLines = 1
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
                    maxLines = 1
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
                    maxLines = 1
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
                SummaryStatItem("Total Size", StorageUtils.formatSize(totalSize))
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
                    maxLines = 1
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

@Composable
fun LibrariesSummaryCard(modules: GradleModulesInfo) {
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
                    Icons.Default.LibraryBooks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    "Gradle Libraries",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem("Total Libraries", modules.libraries.size.toString())
                SummaryStatItem("Cache Size", modules.sizeReadable)
                SummaryStatItem(
                    "Unique Artifacts",
                    modules.libraries.distinctBy { "${it.groupId}:${it.artifactId}" }.size.toString()
                )
            }
        }
    }
}

@Composable
fun LibraryInfoCard(library: GradleLibraryInfo) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.LibraryBooks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp, top = 2.dp).size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        library.artifactId,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        library.groupId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Versions
                    if (library.versions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(library.versions.take(5)) { version -> // Show max 5 versions
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        version,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        )
                                    )
                                }
                            }
                            if (library.versions.size > 5) {
                                item {
                                    Surface(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        shape = MaterialTheme.shapes.extraSmall
                                    ) {
                                        Text(
                                            "+${library.versions.size - 5}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(
                                                horizontal = 6.dp,
                                                vertical = 2.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Version count badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "${library.versions.size} versions",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    icon: ImageVector,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(actionText)
            }
        }
    }
}

// ===========================================
// CHART PRIMITIVES
// ===========================================

data class ChartData(val label: String, val value: Long, val color: Color)


// ===========================================
// MAIN CHARTS TAB CONTENT
// ===========================================

@Composable
fun ChartsTabContent(uiState: StorageAnalyzerUiState) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 400.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Storage Distribution Pie Chart
        item(span = { GridItemSpan(maxLineSpan) }) {
            StorageDistributionChart(uiState)
        }

        // AVDs Size Comparison Bar Chart
        if (uiState.avds.isNotEmpty()) {
            item {
                AvdSizeChart(uiState.avds)
            }
        }

        // SDK Components Chart
        uiState.sdkInfo?.let { sdk ->
            item {
                SdkComponentsChart(sdk)
            }
        }

        // JDK Versions Distribution
        uiState.devEnvironmentInfo?.jdks?.let { jdks ->
            if (jdks.isNotEmpty()) {
                item {
                    JdkVersionsChart(jdks)
                }
            }
        }

        // Gradle Cache Versions Timeline
        if (uiState.gradleCaches.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GradleCacheTimelineChart(uiState.gradleCaches)
            }
        }

        // Development Tools Overview
        item(span = { GridItemSpan(maxLineSpan) }) {
            DevelopmentToolsOverviewChart(uiState)
        }

        // Storage Growth Trend
        item {
            StorageGrowthTrendChart()
        }

        // Cache Efficiency Chart
        if (uiState.gradleModulesInfo != null) {
            item {
                CacheEfficiencyChart(uiState.gradleModulesInfo!!)
            }
        }
    }
}

// ===========================================
// STORAGE DISTRIBUTION PIE CHART
// ===========================================

@Composable
fun StorageDistributionChart(uiState: StorageAnalyzerUiState) {
    Card(
        modifier = Modifier.height(350.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Storage Distribution",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val data = buildList {
                    uiState.devEnvironmentInfo?.let { devEnv ->
                        add(
                            ChartData(
                                "Gradle Cache",
                                devEnv.gradleCache.sizeBytes,
                                Color(0xFF2196F3)
                            )
                        )
                        add(ChartData("IDE Cache", devEnv.ideaCache.sizeBytes, Color(0xFF4CAF50)))
                        add(ChartData("Konan", devEnv.konanInfo.sizeBytes, Color(0xFF9C27B0)))
                        add(ChartData("Skiko", devEnv.skikoInfo.sizeBytes, Color(0xFFFF9800)))
                        add(
                            ChartData(
                                "JDKs",
                                devEnv.jdks.sumOf { it.sizeBytes },
                                Color(0xFFED8B00)
                            )
                        )
                    }
                    uiState.sdkInfo?.let { sdk ->
                        add(ChartData("Android SDK", sdk.totalSizeBytes, Color(0xFF3DDC84)))
                    }
                    add(ChartData("AVDs", uiState.avds.sumOf { it.sizeBytes }, Color(0xFFE91E63)))
                }.filter { it.value > 0 }

                if (data.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Pie Chart
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            PieChart(
                                data = data,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Legend
                        LazyColumn(
                            modifier = Modifier.width(140.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(data) { item ->
                                LegendItem(
                                    color = item.color,
                                    label = item.label,
                                    value = StorageUtils.formatSize(item.value),
                                    percentage = (item.value.toFloat() / data.sumOf { it.value } * 100).toInt()
                                )
                            }
                        }
                    }
                } else {
                    EmptyChartPlaceholder("No storage data available")
                }
            }
        }
    }
}

@Composable
fun PieChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.value }.toFloat()
    if (total <= 0f) return

    Canvas(modifier = modifier.padding(16.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2.5f

        var startAngle = -90f

        data.forEach { item ->
            val sweepAngle = (item.value / total) * 360f

            drawArc(
                color = item.color,
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

@Composable
fun LegendItem(
    color: Color,
    label: String,
    value: String,
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Column(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                "$value ($percentage%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ===========================================
// AVD SIZE BAR CHART
// ===========================================

@Composable
fun AvdSizeChart(avds: List<AvdInfo>) {
    Card(
        modifier = Modifier.height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "AVD Storage Usage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            val chartData = avds.sortedByDescending { it.sizeBytes }.take(8).map {
                ChartData(
                    label = it.name.take(12),
                    value = it.sizeBytes,
                    color = generateColorForIndex(avds.indexOf(it))
                )
            }

            if (chartData.isNotEmpty()) {
                VerticalBarChart(
                    data = chartData,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                EmptyChartPlaceholder("No AVD data available")
            }
        }
    }
}

@Composable
fun VerticalBarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.value }.toFloat()
    if (maxValue <= 0f) return
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier) {
        // Chart area
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp)
        ) {
            val barWidth = size.width / (data.size * 1.5f)
            val maxBarHeight = size.height * 0.9f

            data.forEachIndexed { index, item ->
                val barHeight = (item.value / maxValue) * maxBarHeight
                val x = (index * barWidth * 1.5f) + barWidth * 0.25f
                val y = size.height - barHeight

                // Draw bar with gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            item.color,
                            item.color.copy(alpha = 0.7f)
                        )
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
                val formatted = StorageUtils.formatSize(item.value)

                val textLayoutResult = textMeasurer.measure(
                    text = formatted,
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                )
                // Center text horizontally, place slightly above the bar (y - 10f)
                val textX = x + barWidth / 2 - textLayoutResult.size.width / 2
                val textY = y - 10f - textLayoutResult.size.height / 2

                drawText(
                    textLayoutResult,
                    topLeft = Offset(textX, textY)
                )
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { item ->
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ===========================================
// SDK COMPONENTS DONUT CHART
// ===========================================

@Composable
fun SdkComponentsChart(sdk: SdkInfo) {
    Card(
        modifier = Modifier.height(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Android,
                    contentDescription = null,
                    tint = Color(0xFF3DDC84),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "SDK Components",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            val data = buildList {
                if (sdk.platforms.isNotEmpty()) {
                    add(
                        ChartData(
                            "Platforms",
                            sdk.platforms.sumOf { it.sizeBytes },
                            Color(0xFF4CAF50)
                        )
                    )
                }
                if (sdk.buildTools.isNotEmpty()) {
                    add(
                        ChartData(
                            "Build Tools",
                            sdk.buildTools.sumOf { it.sizeBytes },
                            Color(0xFF2196F3)
                        )
                    )
                }
                if (sdk.systemImages.isNotEmpty()) {
                    add(
                        ChartData(
                            "System Images",
                            sdk.systemImages.sumOf { it.sizeBytes },
                            Color(0xFF9C27B0)
                        )
                    )
                }
                if (sdk.extras.isNotEmpty()) {
                    add(ChartData("Extras", sdk.extras.sumOf { it.sizeBytes }, Color(0xFFFF9800)))
                }
            }.filter { it.value > 0 }

            if (data.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        DonutChart(
                            data = data,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.width(120.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(data) { item ->
                            LegendItem(
                                color = item.color,
                                label = item.label,
                                value = StorageUtils.formatSize(item.value),
                                percentage = (item.value.toFloat() / data.sumOf { it.value } * 100).toInt()
                            )
                        }
                    }
                }
            } else {
                EmptyChartPlaceholder("No SDK component data")
            }
        }
    }
}

@Composable
fun DonutChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.value }.toFloat()
    if (total <= 0f) return
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier.padding(16.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = minOf(size.width, size.height) / 2.5f
        val strokeWidth = outerRadius * 0.3f
        val innerRadius = outerRadius - strokeWidth

        var startAngle = -90f

        data.forEach { item ->
            val sweepAngle = (item.value / total) * 360f

            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            startAngle += sweepAngle
        }

        // Center text
        val textLayoutResult = textMeasurer.measure(
            text = "SDK",
            style = androidx.compose.ui.text.TextStyle(
                color = Color.Gray,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
        )

        // Center the text manually
        val x = center.x - textLayoutResult.size.width / 2
        val y = center.y - textLayoutResult.size.height / 2

        drawText(
            textLayoutResult,
            topLeft = Offset(x, y)
        )
    }
}

// ===========================================
// JDK VERSIONS CHART
// ===========================================

@Composable
fun JdkVersionsChart(jdks: List<JdkInfo>) {
    Card(
        modifier = Modifier.height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Coffee,
                    contentDescription = null,
                    tint = Color(0xFFED8B00),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "JDK Versions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            val versionGroups = jdks.groupBy {
                it.version?.split(".")?.firstOrNull()?.replace("\"", "") ?: "Unknown"
            }.mapValues { (_, jdkList) ->
                jdkList.sumOf { it.sizeBytes }
            }

            val colors = listOf(
                Color(0xFFED8B00),
                Color(0xFFFF5722),
                Color(0xFF795548),
                Color(0xFF607D8B),
                Color(0xFF9E9E9E),
                Color(0xFF673AB7)
            )

            val chartData = versionGroups.entries.mapIndexed { index, (version, size) ->
                ChartData("JDK $version", size, colors[index % colors.size])
            }

            if (chartData.isNotEmpty()) {
                HorizontalBarChart(
                    data = chartData,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                EmptyChartPlaceholder("No JDK data available")
            }
        }
    }
}

@Composable
fun HorizontalBarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.value }.toFloat()
    if (maxValue <= 0f) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        data.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Label
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(80.dp),
                    fontSize = 11.sp
                )

                // Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    // Background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Gray.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                    )

                    // Progress bar
                    val progress = item.value / maxValue
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                item.color,
                                RoundedCornerShape(12.dp)
                            )
                    )
                }

                // Value
                Text(
                    StorageUtils.formatSize(item.value),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.End,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ===========================================
// GRADLE CACHE TIMELINE CHART
// ===========================================

@Composable
fun GradleCacheTimelineChart(caches: List<GradleCacheInfo>) {
    Card(
        modifier = Modifier.height(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Gradle Cache Versions Timeline",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            val sortedCaches = caches.sortedWith(
                compareBy(
                    { it.version.split(".").getOrNull(0)?.toIntOrNull() ?: 0 },
                    { it.version.split(".").getOrNull(1)?.toIntOrNull() ?: 0 }
                ))

            LineChart(
                data = sortedCaches.map {
                    ChartData(
                        label = it.version,
                        value = it.sizeBytes,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun LineChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.value }.toFloat()
    if (maxValue <= 0f) return
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp)
        ) {
            val width = size.width - 40.dp.toPx()
            val height = size.height - 20.dp.toPx()
            val stepX = width / (data.size - 1).coerceAtLeast(1)

            val points = data.mapIndexed { index, item ->
                val x = 20.dp.toPx() + index * stepX
                val y = 10.dp.toPx() + height - (item.value / maxValue) * height
                Offset(x, y)
            }

            if (points.size > 1) {
                // Draw connecting lines
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = data.firstOrNull()?.color ?: Color.Blue,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw points
            points.forEach { point ->
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = data.firstOrNull()?.color ?: Color.Blue,
                    radius = 6.dp.toPx(),
                    center = point
                )
            }

            // Draw values on points
            points.forEachIndexed { index, point ->
                val formatted = StorageUtils.formatSize(data[index].value)

                val textLayoutResult = textMeasurer.measure(
                    text = formatted,
                    style = TextStyle(
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                )

                val x = point.x - textLayoutResult.size.width / 2
                val y = point.y - 20f - textLayoutResult.size.height / 2

                drawText(
                    textLayoutResult,
                    topLeft = Offset(x, y)
                )
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { item ->
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontSize = 10.sp

                )
            }
        }
    }
}

// ===========================================
// ADDITIONAL CHARTS
// ===========================================

@Composable
fun DevelopmentToolsOverviewChart(uiState: StorageAnalyzerUiState) {
    Card(
        modifier = Modifier.height(320.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.DeveloperMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Development Tools Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            val toolsData = buildList {
                add(ChartData("AVDs", uiState.avds.size.toLong(), Color(0xFFE91E63)))
                add(
                    ChartData(
                        "JDKs",
                        (uiState.devEnvironmentInfo?.jdks?.size ?: 0).toLong(),
                        Color(0xFFED8B00)
                    )
                )
                add(
                    ChartData(
                        "Gradle Caches",
                        uiState.gradleCaches.size.toLong(),
                        Color(0xFF2196F3)
                    )
                )
                add(
                    ChartData(
                        "Libraries",
                        (uiState.gradleModulesInfo?.libraries?.size ?: 0).toLong(),
                        Color(0xFF4CAF50)
                    )
                )
                uiState.sdkInfo?.let { sdk ->
                    add(ChartData("SDK Platforms", sdk.platforms.size.toLong(), Color(0xFF9C27B0)))
                    add(ChartData("Build Tools", sdk.buildTools.size.toLong(), Color(0xFF3F51B5)))
                }
            }.filter { it.value > 0 }

            if (toolsData.isNotEmpty()) {
                RadarChart(
                    data = toolsData,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                EmptyChartPlaceholder("No development tools data")
            }
        }
    }
}

@Composable
fun RadarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.value }.toFloat()
    if (maxValue <= 0f) return
    val textMeasurer = rememberTextMeasurer()
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.padding(24.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2.5f
        val angleStep = 360f / data.size

        // Draw background circles
        for (i in 1..5) {
            val circleRadius = radius * (i / 5f)
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = circleRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw axis lines and labels
        data.forEachIndexed { index, item ->
            val angle = (2 * Math.PI * index / data.size).toFloat()

            val labelX = center.x + (radius + 30.dp.toPx()) * cos(angle)
            val labelY = center.y + (radius + 30.dp.toPx()) * sin(angle)

            val textLayoutResult = textMeasurer.measure(
                text = item.label,
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            )

            drawText(
                textLayoutResult,
                topLeft = Offset(
                    x = labelX - textLayoutResult.size.width / 2,
                    y = labelY - textLayoutResult.size.height / 2
                )
            )
        }

        // Draw data polygon
        val path = Path()
        val points = mutableListOf<Offset>()

        data.forEachIndexed { index, item ->
            val angle = Math.toRadians((index * angleStep - 90).toDouble())
            val distance = (item.value / maxValue) * radius
            val x = center.x + distance * cos(angle).toFloat()
            val y = center.y + distance * sin(angle).toFloat()
            points.add(Offset(x, y))

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        if (points.isNotEmpty()) {
            path.close()

            // Fill area
            drawPath(
                path = path,
                color = primary.copy(alpha = 0.3f)
            )

            // Draw border
            drawPath(
                path = path,
                color = primary,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw data points
            points.forEach { point ->
                drawCircle(
                    color = primary,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Composable
fun StorageGrowthTrendChart() {
    Card(
        modifier = Modifier.height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Storage Growth Trend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Simulated growth data
            val growthData = listOf(
                ChartData("Jan", 5000000000L, Color(0xFF4CAF50)),
                ChartData("Feb", 7500000000L, Color(0xFF4CAF50)),
                ChartData("Mar", 12000000000L, Color(0xFF4CAF50)),
                ChartData("Apr", 15000000000L, Color(0xFF4CAF50)),
                ChartData("May", 18000000000L, Color(0xFF4CAF50)),
                ChartData("Jun", 22000000000L, Color(0xFF4CAF50))
            )

            AreaChart(
                data = growthData,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun AreaChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.value }.toFloat()
    if (maxValue <= 0f) return

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp)
        ) {
            val width = size.width - 40.dp.toPx()
            val height = size.height - 40.dp.toPx()
            val stepX = width / (data.size - 1).coerceAtLeast(1)

            val points = data.mapIndexed { index, item ->
                val x = 20.dp.toPx() + index * stepX
                val y = 20.dp.toPx() + height - (item.value / maxValue) * height
                Offset(x, y)
            }

            if (points.size > 1) {
                // Create area path
                val path = Path()
                path.moveTo(points[0].x, size.height - 20.dp.toPx())
                points.forEach { point ->
                    path.lineTo(point.x, point.y)
                }
                path.lineTo(points.last().x, size.height - 20.dp.toPx())
                path.close()

                // Fill area with gradient
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50).copy(alpha = 0.3f),
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        )
                    )
                )

                // Draw line
                val linePath = Path()
                linePath.moveTo(points[0].x, points[0].y)
                points.drop(1).forEach { point ->
                    linePath.lineTo(point.x, point.y)
                }

                drawPath(
                    path = linePath,
                    color = Color(0xFF4CAF50),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw points
                points.forEach { point ->
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = Color(0xFF4CAF50),
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { item ->
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CacheEfficiencyChart(gradleModules: GradleModulesInfo) {
    Card(
        modifier = Modifier.height(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Cache Efficiency",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Group libraries by organization
            val orgGroups = gradleModules.libraries.groupBy {
                it.groupId.split(".").take(2).joinToString(".")
            }.entries.sortedByDescending { it.value.size }.take(8)

            val efficiencyData = orgGroups.map { (org, libraries) ->
                ChartData(
                    label = org.split(".").last().take(10),
                    value = libraries.size.toLong(),
                    color = generateColorForIndex(
                        orgGroups.indexOfFirst { it.key == org && it.value == libraries }
                    )
                )
            }

            if (efficiencyData.isNotEmpty()) {
                BubbleChart(
                    data = efficiencyData,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                EmptyChartPlaceholder("No cache efficiency data")
            }
        }
    }
}

@Composable
fun BubbleChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.value }.toFloat()
    if (maxValue <= 0f) return
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.padding(16.dp)) {
        val width = size.width
        val height = size.height
        val maxRadius = minOf(width, height) / (data.size * 0.8f)

        data.forEachIndexed { index, item ->
            val radius = (item.value / maxValue) * maxRadius * 0.8f + maxRadius * 0.2f
            val x = (index % 3) * (width / 3) + width / 6
            val y = (index / 3) * (height / 3) + height / 6

            // Draw bubble
            drawCircle(
                color = item.color.copy(alpha = 0.3f),
                radius = radius,
                center = Offset(x, y)
            )

            drawCircle(
                color = item.color,
                radius = radius,
                center = Offset(x, y),
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw label
            val labelLayout = textMeasurer.measure(
                text = item.label,
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            )

            drawText(
                labelLayout,
                topLeft = Offset(
                    x = x - labelLayout.size.width / 2,
                    y = y - labelLayout.size.height / 2
                )
            )

            // Draw value below label
            val valueLayout = textMeasurer.measure(
                text = "${item.value} libs",
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            )

            drawText(
                valueLayout,
                topLeft = Offset(
                    x = x - valueLayout.size.width / 2,
                    y = y + 20f - valueLayout.size.height / 2
                )
            )
        }
    }
}

// ===========================================
// UTILITY FUNCTIONS
// ===========================================

fun generateColorForIndex(index: Int): Color {
    val colors = listOf(
        Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF9C27B0),
        Color(0xFFE91E63), Color(0xFF00BCD4), Color(0xFF8BC34A), Color(0xFFFF5722),
        Color(0xFF673AB7), Color(0xFF607D8B), Color(0xFF795548), Color(0xFFFF6B6B),
        Color(0xFF4ECDC4), Color(0xFF45B7D1), Color(0xFF96CEB4), Color(0xFFFFA07A)
    )
    return colors[index % colors.size]
}

@Composable
fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Chart will appear when data is available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}