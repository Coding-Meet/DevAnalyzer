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
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.core.utility.AvdSystemImageSection
import com.meet.dev.analyzer.core.utility.Utils.openFile
import com.meet.dev.analyzer.data.models.storage.AndroidAvdInfo
import com.meet.dev.analyzer.data.models.storage.AndroidSdkInfo
import com.meet.dev.analyzer.data.models.storage.AvdItem
import com.meet.dev.analyzer.data.models.storage.SystemImageInfo
import com.meet.dev.analyzer.data.models.storage.SystemImageInfoItem
import com.meet.dev.analyzer.presentation.components.DetailCardRowLayout
import com.meet.dev.analyzer.presentation.components.DetailCardRowValueLayout
import com.meet.dev.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.dev.analyzer.presentation.components.HeaderCardRow
import com.meet.dev.analyzer.presentation.components.SummaryExpandableSectionLayout
import com.meet.dev.analyzer.presentation.components.SummaryStatItem
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout

@Composable
fun AvdSystemImagesInfoTabContent(
    androidAvdInfo: AndroidAvdInfo?,
    androidSdkInfo: AndroidSdkInfo?,
) {
    var avdListExpanded by rememberSaveable { mutableStateOf(true) }
    var sdkListExpanded by rememberSaveable { mutableStateOf(true) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            state = scrollState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (androidAvdInfo?.avdItemList?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    AvdSummaryCard(
                        androidAvdInfo = androidAvdInfo,
                        avdListExpanded = avdListExpanded,
                        onExpandChange = { avdListExpanded = !avdListExpanded }
                    )
                }
                if (avdListExpanded) {
                    items(
                        items = androidAvdInfo.avdItemList,
                        key = { avdInfo -> avdInfo.uniqueId }
                    ) { avdInfo ->
                        AvdDetailCard(avdItem = avdInfo)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = AvdSystemImageSection.AvdDevices.messageTitle,
                        description = AvdSystemImageSection.AvdDevices.messageDescription,
                        icon = AvdSystemImageSection.AvdDevices.icon
                    )
                }
            }
            // System Images section
            if (androidSdkInfo?.systemImageInfo?.systemImages?.isNotEmpty() == true) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SystemImageSummaryCard(
                        systemImageInfo = androidSdkInfo.systemImageInfo,
                        sdkListExpanded = sdkListExpanded,
                        onExpandChange = { sdkListExpanded = !sdkListExpanded }
                    )
                }
                if (sdkListExpanded) {
                    items(
                        items = androidSdkInfo.systemImageInfo.systemImages,
                        key = { systemImageInfoItem -> systemImageInfoItem.uniqueId }
                    ) { systemImageInfoItem ->
                        SystemImageDetailCard(systemImageInfoItem = systemImageInfoItem)
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = AvdSystemImageSection.SystemImages.messageTitle,
                        description = AvdSystemImageSection.SystemImages.messageDescription,
                        icon = AvdSystemImageSection.SystemImages.icon
                    )
                }
            }
        }
        VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
    }
}

@Composable
fun AvdSummaryCard(
    androidAvdInfo: AndroidAvdInfo,
    avdListExpanded: Boolean,
    onExpandChange: () -> Unit,
) {
    SummaryExpandableSectionLayout(
        expandableSection = AvdSystemImageSection.AvdDevices,
        isExpanded = avdListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = androidAvdInfo.avdItemList.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = androidAvdInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun AvdDetailCard(avdItem: AvdItem) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + Storage
        HeaderCardRow(
            icon = AvdSystemImageSection.AvdDevices.icon,
            sizeReadable = avdItem.actualStorage,
            primaryText = avdItem.name,
            secondaryText = avdItem.device,
            tertiaryText = "API ${avdItem.apiLevel}"
        )

        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = avdItem.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { avdItem.path.openFile() }

        // Configured
        DetailCardRowValueLayout(
            label = "Configured",
            value = avdItem.configuredStorage,
            icon = Icons.Default.Storage
        )
    }
}


@Composable
fun SystemImageSummaryCard(
    systemImageInfo: SystemImageInfo,
    sdkListExpanded: Boolean,
    onExpandChange: () -> Unit
) {
    SummaryExpandableSectionLayout(
        expandableSection = AvdSystemImageSection.SystemImages,
        isExpanded = sdkListExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = it.totalLabel,
            value = systemImageInfo.systemImages.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = systemImageInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}

@Composable
fun SystemImageDetailCard(
    systemImageInfoItem: SystemImageInfoItem
) {
    DetailCardRowLayout {
        // Header Row: Icon + Name + System
        HeaderCardRow(
            icon = AvdSystemImageSection.SystemImages.icon,
            sizeReadable = systemImageInfoItem.sizeReadable,
            primaryText = systemImageInfoItem.name,
        )
        // Path
        DetailCardRowValueLayout(
            label = "Path",
            value = systemImageInfoItem.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { systemImageInfoItem.path.openFile() }
    }

}
