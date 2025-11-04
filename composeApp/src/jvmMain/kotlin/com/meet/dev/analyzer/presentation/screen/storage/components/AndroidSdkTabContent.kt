package com.meet.dev.analyzer.presentation.screen.storage.components

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
import com.meet.dev.analyzer.core.utility.AndroidSdkSection
import com.meet.dev.analyzer.core.utility.Utils.openFile
import com.meet.dev.analyzer.data.models.storage.AndroidSdkInfo
import com.meet.dev.analyzer.data.models.storage.BuildToolInfo
import com.meet.dev.analyzer.data.models.storage.BuildToolItem
import com.meet.dev.analyzer.data.models.storage.CmakeInfo
import com.meet.dev.analyzer.data.models.storage.CmakeInfoItem
import com.meet.dev.analyzer.data.models.storage.ExtrasInfo
import com.meet.dev.analyzer.data.models.storage.ExtrasInfoItem
import com.meet.dev.analyzer.data.models.storage.NdkInfo
import com.meet.dev.analyzer.data.models.storage.NdkItem
import com.meet.dev.analyzer.data.models.storage.PlatformInfo
import com.meet.dev.analyzer.data.models.storage.PlatformItem
import com.meet.dev.analyzer.data.models.storage.SourcesInfo
import com.meet.dev.analyzer.data.models.storage.SourcesInfoItem
import com.meet.dev.analyzer.presentation.components.DetailCardRowLayout
import com.meet.dev.analyzer.presentation.components.DetailCardRowValueLayout
import com.meet.dev.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.dev.analyzer.presentation.components.HeaderCardRow
import com.meet.dev.analyzer.presentation.components.SummaryExpandableSectionLayout
import com.meet.dev.analyzer.presentation.components.SummaryStatItem
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout

@Composable
fun AndroidSdkTabContent(
    androidSdkInfo: AndroidSdkInfo?,
) {
    var platformListExpanded by rememberSaveable { mutableStateOf(true) }
    var buildToolListExpanded by rememberSaveable { mutableStateOf(true) }
    var ndkListExpanded by rememberSaveable { mutableStateOf(true) }
    var cmakeListExpanded by rememberSaveable { mutableStateOf(true) }
    var sourcesListExpanded by rememberSaveable { mutableStateOf(true) }
    var extrasListExpanded by rememberSaveable { mutableStateOf(true) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()
        LazyVerticalGrid(
            state = scrollState,
            columns = GridCells.Adaptive(minSize = 280.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Platforms section
            if (androidSdkInfo?.platformInfo?.platforms?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PlatformSummaryCard(
                        platformInfo = androidSdkInfo.platformInfo,
                        platformListExpanded = platformListExpanded,
                        onExpandChange = {
                            platformListExpanded = !platformListExpanded
                        }
                    )
                }
                if (platformListExpanded) {
                    items(
                        items = androidSdkInfo.platformInfo.platforms,
                        key = { platform -> platform.uniqueId }
                    ) { platform ->
                        PlatformItemCard(platform = platform)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = AndroidSdkSection.SdkPlatforms.messageTitle,
                        description = AndroidSdkSection.SdkPlatforms.messageDescription,
                        icon = AndroidSdkSection.SdkPlatforms.icon
                    )
                }
            }

            // Build Tools section
            if (androidSdkInfo?.buildToolInfo?.buildTools?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BuildToolSummaryCard(
                        buildToolInfo = androidSdkInfo.buildToolInfo,
                        buildToolListExpanded = buildToolListExpanded,
                        onExpandChange = {
                            buildToolListExpanded = !buildToolListExpanded
                        }
                    )
                }
                if (buildToolListExpanded) {
                    items(
                        items = androidSdkInfo.buildToolInfo.buildTools,
                        key = { buildTool -> buildTool.uniqueId }
                    ) { buildTool ->
                        BuildToolItemCard(buildToolItem = buildTool)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = AndroidSdkSection.BuildTools.messageTitle,
                        description = AndroidSdkSection.BuildTools.messageDescription,
                        icon = AndroidSdkSection.BuildTools.icon
                    )
                }
            }
            // Source Section
            if (androidSdkInfo?.sourcesInfo?.sources?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SourceSummaryCard(
                        sourcesInfo = androidSdkInfo.sourcesInfo,
                        sourcesListExpanded = sourcesListExpanded,
                        onExpandChange = {
                            sourcesListExpanded = !sourcesListExpanded
                        }
                    )
                }
                if (sourcesListExpanded) {
                    items(
                        items = androidSdkInfo.sourcesInfo.sources,
                        key = { source -> source.uniqueId }
                    ) { source ->
                        SourceItemCard(source = source)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = AndroidSdkSection.Sources.messageTitle,
                        description = AndroidSdkSection.Sources.messageDescription,
                        icon = AndroidSdkSection.Sources.icon
                    )
                }
            }

            // Ndk Section
            if (androidSdkInfo?.ndkInfo?.ndkItems?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    NdkSummaryCard(
                        ndkInfo = androidSdkInfo.ndkInfo,
                        ndkListExpanded = ndkListExpanded,
                        onExpandChange = {
                            ndkListExpanded = !ndkListExpanded
                        }
                    )
                }
                if (ndkListExpanded) {
                    items(
                        items = androidSdkInfo.ndkInfo.ndkItems,
                        key = { ndk -> ndk.uniqueId }
                    ) { ndk ->
                        NdkItemCard(ndk = ndk)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = AndroidSdkSection.NdkVersions.messageTitle,
                        description = AndroidSdkSection.NdkVersions.messageDescription,
                        icon = AndroidSdkSection.NdkVersions.icon
                    )
                }
            }

            // Cmake Section
            if (androidSdkInfo?.cmakeInfo?.cmakeItems?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CmakeSummaryCard(
                        cmakeInfo = androidSdkInfo.cmakeInfo,
                        cmakeListExpanded = cmakeListExpanded,
                        onExpandChange = {
                            cmakeListExpanded = !cmakeListExpanded
                        }
                    )
                }
                if (cmakeListExpanded) {
                    items(
                        items = androidSdkInfo.cmakeInfo.cmakeItems,
                        key = { cmake -> cmake.uniqueId }
                    ) { cmake ->
                        CmakeItemCard(cmake = cmake)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = AndroidSdkSection.Cmake.messageTitle,
                        description = AndroidSdkSection.Cmake.messageDescription,
                        icon = AndroidSdkSection.Cmake.icon
                    )
                }
            }


            // Extras Section
            if (androidSdkInfo?.extrasInfo?.extrasInfoItems?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ExtrasSummaryCard(
                        extrasInfo = androidSdkInfo.extrasInfo,
                        extrasListExpanded = extrasListExpanded,
                        onExpandChange = {
                            extrasListExpanded = !extrasListExpanded
                        }
                    )
                }
                if (extrasListExpanded) {
                    items(
                        items = androidSdkInfo.extrasInfo.extrasInfoItems,
                        key = { extras -> extras.uniqueId }
                    ) { extras ->
                        ExtrasItemCard(extras = extras)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = AndroidSdkSection.Extras.messageTitle,
                        description = AndroidSdkSection.Extras.messageDescription,
                        icon = AndroidSdkSection.Extras.icon
                    )
                }
            }
        }
        VerticalScrollBarLayout(rememberScrollbarAdapter(scrollState))
    }
}

@Composable
fun PlatformSummaryCard(
    platformInfo: PlatformInfo,
    platformListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        expandableSection = AndroidSdkSection.SdkPlatforms,
        isExpanded = platformListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = platformInfo.platforms.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = platformInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun PlatformItemCard(
    platform: PlatformItem
) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + System
        HeaderCardRow(
            icon = AndroidSdkSection.SdkPlatforms.icon,
            sizeReadable = platform.sizeReadable,
            primaryText = platform.name,
        )
        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = platform.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { platform.path.openFile() }

    }
}

@Composable
fun BuildToolSummaryCard(
    buildToolInfo: BuildToolInfo,
    buildToolListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        expandableSection = AndroidSdkSection.BuildTools,
        isExpanded = buildToolListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = buildToolInfo.buildTools.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = buildToolInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun BuildToolItemCard(
    buildToolItem: BuildToolItem
) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + System
        HeaderCardRow(
            icon = AndroidSdkSection.BuildTools.icon,
            sizeReadable = buildToolItem.sizeReadable,
            primaryText = buildToolItem.name,
        )
        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = buildToolItem.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { buildToolItem.path.openFile() }
    }
}

@Composable
fun SourceSummaryCard(
    sourcesInfo: SourcesInfo,
    sourcesListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        expandableSection = AndroidSdkSection.Sources,
        isExpanded = sourcesListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = sourcesInfo.sources.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = sourcesInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun SourceItemCard(
    source: SourcesInfoItem
) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + System
        HeaderCardRow(
            icon = AndroidSdkSection.Sources.icon,
            sizeReadable = source.sizeReadable,
            primaryText = source.name,
        )
        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = source.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { source.path.openFile() }
    }
}

@Composable
fun NdkSummaryCard(
    ndkInfo: NdkInfo,
    ndkListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        expandableSection = AndroidSdkSection.NdkVersions,
        isExpanded = ndkListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = ndkInfo.ndkItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = ndkInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun NdkItemCard(
    ndk: NdkItem
) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + System
        HeaderCardRow(
            icon = AndroidSdkSection.NdkVersions.icon,
            sizeReadable = ndk.sizeReadable,
            primaryText = ndk.name,
        )
        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = ndk.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { ndk.path.openFile() }
    }
}

@Composable
fun CmakeSummaryCard(
    cmakeInfo: CmakeInfo,
    cmakeListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        expandableSection = AndroidSdkSection.Cmake,
        isExpanded = cmakeListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = cmakeInfo.cmakeItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = cmakeInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun CmakeItemCard(
    cmake: CmakeInfoItem
) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + System
        HeaderCardRow(
            icon = AndroidSdkSection.Cmake.icon,
            sizeReadable = cmake.sizeReadable,
            primaryText = cmake.name,
        )
        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = cmake.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { cmake.path.openFile() }
    }
}

@Composable
fun ExtrasSummaryCard(
    extrasInfo: ExtrasInfo,
    extrasListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        expandableSection = AndroidSdkSection.Extras,
        isExpanded = extrasListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = extrasInfo.extrasInfoItems.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = extrasInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun ExtrasItemCard(
    extras: ExtrasInfoItem
) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + System
        HeaderCardRow(
            icon = AndroidSdkSection.Extras.icon,
            sizeReadable = extras.sizeReadable,
            primaryText = extras.name,
        )
        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = extras.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { extras.path.openFile() }
    }
}