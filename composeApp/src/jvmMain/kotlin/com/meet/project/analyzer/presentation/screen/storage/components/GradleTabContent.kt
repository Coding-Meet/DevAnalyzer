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
import com.meet.project.analyzer.core.utility.GradleSection
import com.meet.project.analyzer.core.utility.IdeDataSection
import com.meet.project.analyzer.core.utility.Utils.openFile
import com.meet.project.analyzer.data.models.storage.CachesGradleWrapperInfo
import com.meet.project.analyzer.data.models.storage.CachesGradleWrapperItem
import com.meet.project.analyzer.data.models.storage.DaemonInfo
import com.meet.project.analyzer.data.models.storage.DaemonItem
import com.meet.project.analyzer.data.models.storage.GradleInfo
import com.meet.project.analyzer.data.models.storage.OtherGradleFolderInfo
import com.meet.project.analyzer.data.models.storage.OtherGradleFolderItem
import com.meet.project.analyzer.data.models.storage.WrapperInfo
import com.meet.project.analyzer.data.models.storage.WrapperItem
import com.meet.project.analyzer.presentation.components.DetailCardRowLayout
import com.meet.project.analyzer.presentation.components.DetailCardRowValueLayout
import com.meet.project.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.project.analyzer.presentation.components.HeaderCardRow
import com.meet.project.analyzer.presentation.components.SummaryExpandableSectionLayout
import com.meet.project.analyzer.presentation.components.SummaryStatItem
import com.meet.project.analyzer.presentation.components.VerticalScrollBarLayout

@Composable
fun GradleTabContent(
    gradleInfo: GradleInfo?,
) {
    var gradleWrapperListExpanded by rememberSaveable { mutableStateOf(true) }
    var daemonListExpanded by rememberSaveable { mutableStateOf(true) }
    var gradleCacheWrapperListExpanded by rememberSaveable { mutableStateOf(true) }
    var otherGradleFolderExpanded by rememberSaveable { mutableStateOf(true) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()
        LazyVerticalGrid(
            state = scrollState,
            columns = GridCells.Adaptive(minSize = 280.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (gradleInfo != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GradleSummaryCard(gradleInfo = gradleInfo)
                }
            }
            // Gradle Wrappers
            if (gradleInfo?.wrapperInfo?.wrapperItems?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GradleWrapperSummaryCard(
                        wrapperInfo = gradleInfo.wrapperInfo,
                        gradleWrapperListExpanded = gradleWrapperListExpanded,
                        onExpandChange = { gradleWrapperListExpanded = !gradleWrapperListExpanded }
                    )
                }
                if (gradleWrapperListExpanded) {
                    items(
                        items = gradleInfo.wrapperInfo.wrapperItems,
                        key = { wrapper -> wrapper.uniqueId }) { wrapper ->
                        GradleWrapperDetailCard(wrapperItem = wrapper)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = GradleSection.GradleWrapper.messageTitle,
                        description = GradleSection.GradleWrapper.messageDescription,
                        icon = GradleSection.GradleWrapper.icon
                    )
                }
            }

            // Daemon
            if (gradleInfo?.daemonInfo?.daemonItems?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DaemonSummaryCard(
                        daemonInfo = gradleInfo.daemonInfo,
                        gradleCacheListExpanded = daemonListExpanded,
                        onExpandChange = { daemonListExpanded = !daemonListExpanded }
                    )
                }
                if (daemonListExpanded) {
                    items(
                        items = gradleInfo.daemonInfo.daemonItems,
                        key = { daemonItem -> daemonItem.uniqueId }) { daemonItem ->
                        DaemonDetailCard(daemonItem = daemonItem)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = GradleSection.GradleDaemon.messageTitle,
                        description = GradleSection.GradleDaemon.messageDescription,
                        icon = GradleSection.GradleDaemon.icon
                    )
                }
            }

            // Gradle Cache
            if (gradleInfo?.cachesGradleWrapperInfo?.cachesGradleWrapperItems?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GradleCacheWrapperSummaryCard(
                        cachesGradleWrapperInfo = gradleInfo.cachesGradleWrapperInfo,
                        gradleCacheWrapperListExpanded = gradleCacheWrapperListExpanded,
                        onExpandChange = {
                            gradleCacheWrapperListExpanded = !gradleCacheWrapperListExpanded
                        }
                    )
                }
                if (gradleCacheWrapperListExpanded) {
                    items(
                        items = gradleInfo.cachesGradleWrapperInfo.cachesGradleWrapperItems,
                        key = { cachesGradleWrapperItem -> cachesGradleWrapperItem.uniqueId })
                    { cachesGradleWrapperItem ->
                        GradleCacheWrapperDetailCard(cachesGradleWrapperItem = cachesGradleWrapperItem)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = GradleSection.GradleCaches.messageTitle,
                        description = GradleSection.GradleCaches.messageDescription,
                        icon = GradleSection.GradleCaches.icon
                    )
                }
            }

            // other
            if (gradleInfo?.otherGradleFolderInfo?.otherGradleFolderItems?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OtherGradleFolderSummaryCard(
                        otherGradleFolderInfo = gradleInfo.otherGradleFolderInfo,
                        otherGradleFolderListExpanded = otherGradleFolderExpanded,
                        onExpandChange = { otherGradleFolderExpanded = !otherGradleFolderExpanded }
                    )
                }
                if (otherGradleFolderExpanded) {
                    items(
                        items = gradleInfo.otherGradleFolderInfo.otherGradleFolderItems,
                        key = { otherGradleFolderItem -> otherGradleFolderItem.uniqueId })
                    { otherGradleFolderItem ->
                        OtherGradleFolderDetailCard(otherGradleFolderItem = otherGradleFolderItem)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = GradleSection.OtherGradle.messageTitle,
                        description = GradleSection.OtherGradle.messageDescription,
                        icon = GradleSection.OtherGradle.icon
                    )
                }
            }
        }
        VerticalScrollBarLayout(rememberScrollbarAdapter(scrollState))
    }
}

@Composable
fun GradleSummaryCard(
    gradleInfo: GradleInfo,
) {
    SummaryExpandableSectionLayout(
        type = IdeDataSection.IdeSummary
    ) {
        SummaryStatItem(
            label = GradleSection.GradleWrapper.totalLabel,
            value = gradleInfo.wrapperInfo.wrapperItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = GradleSection.GradleDaemon.totalLabel,
            value = gradleInfo.daemonInfo.daemonItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = GradleSection.GradleCaches.totalLabel,
            value = gradleInfo.cachesGradleWrapperInfo.cachesGradleWrapperItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = GradleSection.OtherGradle.totalLabel,
            value = gradleInfo.otherGradleFolderInfo.otherGradleFolderItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = gradleInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun GradleWrapperSummaryCard(
    wrapperInfo: WrapperInfo,
    gradleWrapperListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        type = GradleSection.GradleWrapper,
        isExpanded = gradleWrapperListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = wrapperInfo.wrapperItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = wrapperInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun GradleWrapperDetailCard(wrapperItem: WrapperItem) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + Storage
        HeaderCardRow(
            icon = GradleSection.GradleWrapper.icon,
            sizeReadable = wrapperItem.sizeReadable,
            primaryText = wrapperItem.version,
        )

        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = wrapperItem.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { wrapperItem.path.openFile() }

    }
}

@Composable
fun DaemonSummaryCard(
    daemonInfo: DaemonInfo,
    gradleCacheListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        type = GradleSection.GradleDaemon,
        isExpanded = gradleCacheListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = daemonInfo.daemonItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = daemonInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun DaemonDetailCard(daemonItem: DaemonItem) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + Storage
        HeaderCardRow(
            icon = GradleSection.GradleDaemon.icon,
            sizeReadable = daemonItem.sizeReadable,
            primaryText = daemonItem.name,
        )

        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = daemonItem.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { daemonItem.path.openFile() }

    }
}

@Composable
fun GradleCacheWrapperSummaryCard(
    cachesGradleWrapperInfo: CachesGradleWrapperInfo,
    gradleCacheWrapperListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        type = GradleSection.GradleCaches,
        isExpanded = gradleCacheWrapperListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = cachesGradleWrapperInfo.cachesGradleWrapperItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = cachesGradleWrapperInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun GradleCacheWrapperDetailCard(cachesGradleWrapperItem: CachesGradleWrapperItem) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + Storage
        HeaderCardRow(
            icon = GradleSection.GradleCaches.icon,
            sizeReadable = cachesGradleWrapperItem.sizeReadable,
            primaryText = cachesGradleWrapperItem.version,
        )

        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = cachesGradleWrapperItem.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { cachesGradleWrapperItem.path.openFile() }

    }
}

@Composable
fun OtherGradleFolderSummaryCard(
    otherGradleFolderInfo: OtherGradleFolderInfo,
    otherGradleFolderListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        type = GradleSection.OtherGradle,
        isExpanded = otherGradleFolderListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = otherGradleFolderInfo.otherGradleFolderItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = otherGradleFolderInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun OtherGradleFolderDetailCard(otherGradleFolderItem: OtherGradleFolderItem) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + Storage
        HeaderCardRow(
            icon = GradleSection.OtherGradle.icon,
            sizeReadable = otherGradleFolderItem.sizeReadable,
            primaryText = otherGradleFolderItem.version,
        )

        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = otherGradleFolderItem.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { otherGradleFolderItem.path.openFile() }

    }
}
