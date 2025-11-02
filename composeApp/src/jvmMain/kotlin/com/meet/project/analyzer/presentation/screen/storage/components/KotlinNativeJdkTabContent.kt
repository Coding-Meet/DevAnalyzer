package com.meet.project.analyzer.presentation.screen.storage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.core.utility.KotlinNativeJdkSection
import com.meet.project.analyzer.core.utility.Utils.openFile
import com.meet.project.analyzer.data.models.storage.DependenciesInfo
import com.meet.project.analyzer.data.models.storage.DependenciesItem
import com.meet.project.analyzer.data.models.storage.JdkInfo
import com.meet.project.analyzer.data.models.storage.JdkItem
import com.meet.project.analyzer.data.models.storage.KonanInfo
import com.meet.project.analyzer.data.models.storage.KotlinNativeInfo
import com.meet.project.analyzer.data.models.storage.KotlinNativeItem
import com.meet.project.analyzer.presentation.components.DetailCardRowLayout
import com.meet.project.analyzer.presentation.components.DetailCardRowValueLayout
import com.meet.project.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.project.analyzer.presentation.components.HeaderCardRow
import com.meet.project.analyzer.presentation.components.SummaryExpandableSectionLayout
import com.meet.project.analyzer.presentation.components.SummaryStatItem
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout

@Composable
fun KotlinNativeJdkTabContent(
    jdkInfo: JdkInfo?,
    konanInfo: KonanInfo?,
) {

    var jdkListExpanded by rememberSaveable { mutableStateOf(true) }
    var kotlinNativeListExpanded by rememberSaveable { mutableStateOf(true) }
    var dependenciesListExpanded by rememberSaveable { mutableStateOf(true) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()
        LazyVerticalGrid(
            state = scrollState,
            columns = GridCells.Adaptive(minSize = 280.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // JDK versions
            if (jdkInfo?.jdkItems?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    JdkSummaryCard(
                        jdkInfo = jdkInfo,
                        jdkListExpanded = jdkListExpanded,
                        onExpandChange = { jdkListExpanded = !jdkListExpanded }
                    )
                }
                if (jdkListExpanded) {
                    items(
                        items = jdkInfo.jdkItems,
                        key = { jdkItem -> jdkItem.uniqueId }
                    ) { jdkItem ->
                        JdkDetailCard(jdkItem = jdkItem)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = KotlinNativeJdkSection.JdkVersions.messageTitle,
                        description = KotlinNativeJdkSection.JdkVersions.messageDescription,
                        icon = KotlinNativeJdkSection.JdkVersions.icon
                    )
                }
            }

            // Kotlin/Native versions
            if (konanInfo?.kotlinNativeInfo?.kotlinNativeItems?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    KotlinNativeSummaryCard(
                        kotlinNativeInfo = konanInfo.kotlinNativeInfo,
                        kotlinNativeListExpanded = kotlinNativeListExpanded,
                        onExpandChange = { kotlinNativeListExpanded = !kotlinNativeListExpanded }
                    )
                }
                if (kotlinNativeListExpanded) {
                    items(
                        items = konanInfo.kotlinNativeInfo.kotlinNativeItems,
                        key = { kotlinNativeItem -> kotlinNativeItem.uniqueId }
                    ) { kotlinNativeItem ->
                        KotlinNativeDetailCard(kotlinNativeItem = kotlinNativeItem)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = KotlinNativeJdkSection.KotlinNative.messageTitle,
                        description = KotlinNativeJdkSection.KotlinNative.messageDescription,
                        icon = KotlinNativeJdkSection.KotlinNative.icon
                    )
                }
            }

            // konan lldb
            if (konanInfo?.dependenciesInfo?.dependenciesItems?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LldbVersionsSummaryCard(
                        dependenciesInfo = konanInfo.dependenciesInfo,
                        dependenciesListExpanded = dependenciesListExpanded,
                        onExpandChange = { dependenciesListExpanded = !dependenciesListExpanded }
                    )
                }
                if (dependenciesListExpanded) {
                    items(
                        items = konanInfo.dependenciesInfo.dependenciesItems,
                        key = { dependenciesItem -> dependenciesItem.uniqueId }) { dependenciesItem ->
                        LldbVersionsDetailCard(dependenciesItem = dependenciesItem)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = KotlinNativeJdkSection.LldbVersions.messageTitle,
                        description = KotlinNativeJdkSection.LldbVersions.messageDescription,
                        icon = KotlinNativeJdkSection.LldbVersions.icon
                    )
                }
            }
        }
        VerticalScrollBarLayout(rememberScrollbarAdapter(scrollState))
    }
}


@Composable
fun JdkSummaryCard(
    jdkInfo: JdkInfo,
    jdkListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        type = KotlinNativeJdkSection.JdkVersions,
        isExpanded = jdkListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = jdkInfo.jdkItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = jdkInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun JdkDetailCard(jdkItem: JdkItem) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + Storage
        HeaderCardRow(
            icon = KotlinNativeJdkSection.JdkVersions.icon,
            sizeReadable = jdkItem.sizeReadable,
            primaryText = jdkItem.name ?: "Unknown",
        )

        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = jdkItem.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { jdkItem.path.openFile() }

    }
}

@Composable
fun KotlinNativeSummaryCard(
    kotlinNativeInfo: KotlinNativeInfo,
    kotlinNativeListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        type = KotlinNativeJdkSection.KotlinNative,
        isExpanded = kotlinNativeListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = kotlinNativeInfo.kotlinNativeItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = kotlinNativeInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun KotlinNativeDetailCard(kotlinNativeItem: KotlinNativeItem) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + Storage
        HeaderCardRow(
            icon = KotlinNativeJdkSection.KotlinNative.icon,
            sizeReadable = kotlinNativeItem.sizeReadable,
            primaryText = kotlinNativeItem.version ?: "Unknown",
        )

        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = kotlinNativeItem.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { kotlinNativeItem.path.openFile() }

    }
}

@Composable
fun LldbVersionsSummaryCard(
    dependenciesInfo: DependenciesInfo,
    dependenciesListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        type = KotlinNativeJdkSection.LldbVersions,
        isExpanded = dependenciesListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = dependenciesInfo.dependenciesItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = dependenciesInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun LldbVersionsDetailCard(dependenciesItem: DependenciesItem) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + Storage
        HeaderCardRow(
            icon = KotlinNativeJdkSection.LldbVersions.icon,
            sizeReadable = dependenciesItem.sizeReadable,
            primaryText = dependenciesItem.version,
        )

        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = dependenciesItem.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { dependenciesItem.path.openFile() }

    }
}