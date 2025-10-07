package com.meet.project.analyzer.presentation.screen.dependencies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.awt.Cursor
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

// Data classes for our dummy data
data class SdkInfo(val version: String, val apiLevel: Int, val isUsed: Boolean, val size: String)
data class LibraryInfo(val name: String, val version: String, val isUsed: Boolean, val size: String)
data class AvdInfo(val name: String, val apiLevel: Int, val size: String, val sizeBytes: Long)
data class StorageInfo(
    val category: String,
    val path: String,
    val size: String,
    val sizeBytes: Long
)

data class GradleCacheInfo(
    val name: String,
    val type: String,
    val version: String?,
    val path: String,
    val size: String,
    val sizeBytes: Long,
    val lastModified: String,
    val isUsed: Boolean
)

// Dummy data
val dummySdks = listOf(
    SdkInfo("Android 9.0 (API 28)", 28, false, "1.2 GB"),
    SdkInfo("Android 10.0 (API 29)", 29, true, "1.3 GB"),
    SdkInfo("Android 11.0 (API 30)", 30, false, "1.4 GB"),
    SdkInfo("Android 13.0 (API 33)", 33, true, "1.6 GB"),
    SdkInfo("Android 14.0 (API 34)", 34, false, "1.7 GB")
)

val dummyLibraries = listOf(
    LibraryInfo("com.squareup.retrofit2:retrofit", "2.9.0", true, "142 MB"),
    LibraryInfo("io.coil-kt:coil", "1.4.0", false, "23 MB"),
    LibraryInfo("com.squareup.okhttp3:okhttp", "4.10.0", true, "89 MB"),
    LibraryInfo("androidx.compose.ui:ui", "1.5.4", true, "234 MB"),
    LibraryInfo("com.google.dagger:hilt-android", "2.44", false, "156 MB"),
    LibraryInfo("androidx.navigation:navigation-compose", "2.7.4", true, "78 MB")
)

val dummyAvds = listOf(
    AvdInfo("Pixel_4_API_30", 30, "3.2 GB", 3200000000L),
    AvdInfo("Pixel_6_API_34", 34, "5.8 GB", 5800000000L),
    AvdInfo("Nexus_5X_API_29", 29, "2.9 GB", 2900000000L)
)

val dummyStorage = listOf(
    StorageInfo("Gradle Cache", "~/.gradle/caches", "4.2 GB", 4200000000L),
    StorageInfo("Android AVDs", "~/.android/avd", "11.9 GB", 11900000000L),
    StorageInfo("Android SDK", "~/Android/Sdk", "15.4 GB", 15400000000L),
    StorageInfo("Build Outputs", "Various project /build folders", "2.1 GB", 2100000000L)
)

val dummyGradleCaches = listOf(
    // JDK Caches
    GradleCacheInfo(
        "JDK 17.0.8",
        "JDK",
        "17.0.8",
        "~/.gradle/jdks/jdk-17.0.8",
        "234 MB",
        234000000L,
        "2 days ago",
        true
    ),
    GradleCacheInfo(
        "JDK 11.0.19",
        "JDK",
        "11.0.19",
        "~/.gradle/jdks/jdk-11.0.19",
        "198 MB",
        198000000L,
        "1 week ago",
        false
    ),
    GradleCacheInfo(
        "JDK 8u372",
        "JDK",
        "1.8.0_372",
        "~/.gradle/jdks/jdk-8u372",
        "156 MB",
        156000000L,
        "3 weeks ago",
        false
    ),

    // Skiko Caches
    GradleCacheInfo(
        "Skiko Windows",
        "Skiko",
        "0.7.85",
        "~/.gradle/caches/modules-2/files-2.1/org.jetbrains.skiko/skiko-awt-runtime-windows-x64",
        "45 MB",
        45000000L,
        "1 day ago",
        true
    ),
    GradleCacheInfo(
        "Skiko macOS",
        "Skiko",
        "0.7.85",
        "~/.gradle/caches/modules-2/files-2.1/org.jetbrains.skiko/skiko-awt-runtime-macos-x64",
        "42 MB",
        42000000L,
        "5 days ago",
        false
    ),
    GradleCacheInfo(
        "Skiko Linux",
        "Skiko",
        "0.7.82",
        "~/.gradle/caches/modules-2/files-2.1/org.jetbrains.skiko/skiko-awt-runtime-linux-x64",
        "38 MB",
        38000000L,
        "2 weeks ago",
        false
    ),

    // Konan Caches
    GradleCacheInfo(
        "Kotlin/Native 1.9.20",
        "Konan",
        "1.9.20",
        "~/.konan/kotlin-native-prebuilt-linux-x86_64-1.9.20",
        "189 MB",
        189000000L,
        "3 days ago",
        true
    ),
    GradleCacheInfo(
        "Kotlin/Native 1.9.10",
        "Konan",
        "1.9.10",
        "~/.konan/kotlin-native-prebuilt-linux-x86_64-1.9.10",
        "182 MB",
        182000000L,
        "2 weeks ago",
        false
    ),
    GradleCacheInfo(
        "Kotlin/Native Dependencies",
        "Konan",
        null,
        "~/.konan/dependencies",
        "67 MB",
        67000000L,
        "1 day ago",
        true
    ),

    // Android Studio Caches
    GradleCacheInfo(
        "Android Studio Flamingo",
        "Android Studio",
        "2022.2.1",
        "~/Library/Caches/Google/AndroidStudio2022.2",
        "892 MB",
        892000000L,
        "1 day ago",
        true
    ),
    GradleCacheInfo(
        "Android Studio Electric Eel",
        "Android Studio",
        "2022.1.1",
        "~/Library/Caches/Google/AndroidStudio2022.1",
        "745 MB",
        745000000L,
        "1 week ago",
        false
    ),
    GradleCacheInfo(
        "Android Studio Dolphin",
        "Android Studio",
        "2021.3.1",
        "~/Library/Caches/Google/AndroidStudio2021.3",
        "623 MB",
        623000000L,
        "1 month ago",
        false
    ),

    // General Gradle Caches
    GradleCacheInfo(
        "Gradle Daemon",
        "Gradle",
        "8.4",
        "~/.gradle/daemon/8.4",
        "234 MB",
        234000000L,
        "2 hours ago",
        true
    ),
    GradleCacheInfo(
        "Gradle Wrapper",
        "Gradle",
        "8.2",
        "~/.gradle/wrapper/dists/gradle-8.2-bin",
        "156 MB",
        156000000L,
        "1 week ago",
        false
    ),
    GradleCacheInfo(
        "Build Cache",
        "Gradle",
        null,
        "~/.gradle/caches/build-cache-1",
        "1.2 GB",
        1200000000L,
        "1 hour ago",
        true
    ),
    GradleCacheInfo(
        "Modules Cache",
        "Gradle",
        null,
        "~/.gradle/caches/modules-2",
        "3.4 GB",
        3400000000L,
        "30 minutes ago",
        true
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("SDKs", "Libraries", "AVDs", "Storage", "Cache Details")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar with Material 3 styling
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
                        "Android Dev Storage Analyzer",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primary
            ),
            actions = {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                            )
                        )
                    ),
                    onClick = { /* Refresh action */ }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                            )
                        )
                    ),
                    onClick = { /* Settings action */ }) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )

        // Primary Tab Row with Material 3 styling
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                indicator = { tabPositions ->
//                    TabRowDefaults.PrimaryIndicator(
//                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> SdkScreen()
                1 -> LibrariesScreen()
                2 -> AvdScreen()
                3 -> StorageScreen()
                4 -> CacheDetailScreen()
            }
        }
    }

}

@Composable
fun SdkScreen() {
    val usedSdks = dummySdks.filter { it.isUsed }
    val unusedSdks = dummySdks.filter { !it.isUsed }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryCard(
                title = "SDK Summary",
                items = listOf(
                    "Total SDKs: ${dummySdks.size}",
                    "Used in projects: ${usedSdks.size}",
                    "Unused SDKs: ${unusedSdks.size}"
                ),
                icon = Icons.Default.Android,
                color = Color(0xFF4CAF50)
            )
        }

        item {
            Text(
                "Used SDKs",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(usedSdks) { sdk ->
            SdkCard(sdk = sdk, isUsed = true)
        }

        item {
            Text(
                "Unused SDKs",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(unusedSdks) { sdk ->
            SdkCard(sdk = sdk, isUsed = false)
        }
    }
}

@Composable
fun LibrariesScreen() {
    val usedLibs = dummyLibraries.filter { it.isUsed }
    val unusedLibs = dummyLibraries.filter { !it.isUsed }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryCard(
                title = "Gradle Cache Summary",
                items = listOf(
                    "Total cached libraries: ${dummyLibraries.size}",
                    "Used in projects: ${usedLibs.size}",
                    "Unused libraries: ${unusedLibs.size}"
                ),
                icon = Icons.Default.LibraryBooks,
                color = Color(0xFF9C27B0)
            )
        }

        item {
            Text(
                "Used Libraries",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(usedLibs) { lib ->
            LibraryCard(library = lib, isUsed = true)
        }

        item {
            Text(
                "Unused Libraries",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(unusedLibs) { lib ->
            LibraryCard(library = lib, isUsed = false)
        }
    }
}

@Composable
fun StorageCard(storage: StorageInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (storage.category) {
                    "Gradle Cache" -> Icons.Default.Folder
                    "Android AVDs" -> Icons.Default.PhoneAndroid
                    "Android SDK" -> Icons.Default.Android
                    else -> Icons.Default.Storage
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(end = 16.dp).size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    storage.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    storage.path,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    storage.size,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AvdScreen() {
    val totalSize = dummyAvds.sumOf { it.sizeBytes }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryCard(
                title = "AVD Summary",
                items = listOf(
                    "Total AVDs: ${dummyAvds.size}",
                    "Total AVD size: ${formatBytes(totalSize)}"
                ),
                icon = Icons.Default.PhoneAndroid,
                color = Color(0xFF2196F3)
            )
        }

        items(dummyAvds) { avd ->
            AvdCard(avd = avd)
        }
    }
}

fun formatBytes(var0: Long): String {
    return if (var0 != 1L && var0 != -1L) {
        if (var0 < 1024L && var0 > -1024L) "$var0 bytes" else formatDataAmount(
            "%.1f %cB",
            var0
        )
    } else {
        "$var0 byte"
    }
}

private fun formatDataAmount(var0: String, var1: Long): String {
    if (var1 == Long.MIN_VALUE) {
        return "N/A"
    } else {
        val var3 = (ln(abs(var1).toDouble()) / ln(1024.0)).toInt()
        val var4 = "kMGTPE"[var3 - 1]
        return String.format(var0, var1.toDouble() / 1024.0.pow(var3.toDouble()), var4)
    }
}

@Composable
fun StorageScreen() {
    val totalSize = dummyStorage.sumOf { it.sizeBytes }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryCard(
                title = "Storage Summary",
                items = listOf(
                    "Total Android Dev Storage: ${formatBytes(totalSize)}"
                ),
                icon = Icons.Default.Storage,
                color = Color(0xFFFF9800)
            )
        }

        items(dummyStorage) { storage ->
            StorageCard(storage = storage)
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    items: List<String>,
    icon: ImageVector,
    color: Color
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(56.dp)
                    .padding(end = 20.dp)
            )
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                items.forEach { item ->
                    Text(
                        item,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SdkCard(sdk: SdkInfo, isUsed: Boolean) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isUsed)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
//            brush = null,
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isUsed) Icons.Default.CheckCircle else Icons.Outlined.Cancel,
                contentDescription = null,
                tint = if (isUsed)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(end = 16.dp).size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    sdk.version,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (isUsed) "Used in projects" else "Unused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUsed)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    sdk.size,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CacheDetailScreen() {
    val cachesByType = dummyGradleCaches.groupBy { it.type }
    val totalSize = dummyGradleCaches.sumOf { it.sizeBytes }
    val usedCaches = dummyGradleCaches.filter { it.isUsed }
    val unusedCaches = dummyGradleCaches.filter { !it.isUsed }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
    ) {
        // Summary Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 12.dp)
                        )
                        Text(
                            "Development Cache Summary",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Summary stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CacheSummaryItem("Total Caches", dummyGradleCaches.size.toString())
                        CacheSummaryItem("Active", usedCaches.size.toString())
                        CacheSummaryItem("Unused", unusedCaches.size.toString())
                        CacheSummaryItem("Total Size", formatBytes(totalSize))
                    }
                }
            }
        }

        // Cache types
        cachesByType.forEach { (type, caches) ->
            item {
                Text(
                    "$type Caches",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(caches) { cache ->
                CacheItemCard(cache = cache)
            }
        }
    }
}

@Composable
fun CacheSummaryItem(label: String, value: String) {
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
fun CacheItemCard(cache: GradleCacheInfo) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (cache.isUsed)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (cache.type) {
                        "JDK" -> Icons.Default.Coffee
                        "Skiko" -> Icons.Default.Brush
                        "Konan" -> Icons.Default.Memory
                        "Android Studio" -> Icons.Default.Code
                        "Gradle" -> Icons.Default.Build
                        else -> Icons.Default.Folder
                    },
                    contentDescription = null,
                    tint = getCacheTypeColor(cache.type),
                    modifier = Modifier.padding(end = 12.dp).size(24.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        cache.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (cache.version != null) {
                        Text(
                            "Version: ${cache.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status indicator
                Surface(
                    color = if (cache.isUsed)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.error,
                    contentColor = if (cache.isUsed)
                        MaterialTheme.colorScheme.onSecondary
                    else
                        MaterialTheme.colorScheme.onError,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        if (cache.isUsed) "ACTIVE" else "UNUSED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        cache.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        "Last modified: ${cache.lastModified}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        cache.size,
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
fun getCacheTypeColor(type: String): Color {
    return when (type) {
        "JDK" -> Color(0xFFED8B00) // Orange
        "Skiko" -> Color(0xFF7C4DFF) // Purple
        "Konan" -> Color(0xFF0F9D58) // Green
        "Android Studio" -> Color(0xFF3DDC84) // Android Green
        "Gradle" -> Color(0xFF02303A) // Dark teal
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
fun LibraryCard(library: LibraryInfo, isUsed: Boolean) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isUsed)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
//            brush = null,
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isUsed) Icons.Default.CheckCircle else Icons.Outlined.Cancel,
                contentDescription = null,
                tint = if (isUsed)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(end = 16.dp).size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    library.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "v${library.version} • ${if (isUsed) "Used" else "Unused"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUsed)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    library.size,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AvdCard(avd: AvdInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp).size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    avd.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "API Level ${avd.apiLevel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    avd.size,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}