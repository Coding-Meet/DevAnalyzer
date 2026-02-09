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
import com.meet.dev.analyzer.data.models.storage.IdeDataInfo
import com.meet.dev.analyzer.data.models.storage.IdeGroup
import com.meet.dev.analyzer.data.models.storage.IdeInstallation
import com.meet.dev.analyzer.presentation.components.DetailCardRowLayout
import com.meet.dev.analyzer.presentation.components.DetailCardRowValueLayout
import com.meet.dev.analyzer.presentation.components.EmptyStateCardLayout
import com.meet.dev.analyzer.presentation.components.HeaderCardRow
import com.meet.dev.analyzer.presentation.components.SummaryExpandableSectionLayout
import com.meet.dev.analyzer.presentation.components.SummaryStatItem
import com.meet.dev.analyzer.presentation.components.VerticalScrollBarLayout
import com.meet.dev.analyzer.utility.platform.FolderFileUtils.openFile
import com.meet.dev.analyzer.utility.ui.ExpandableSection
import com.meet.dev.analyzer.utility.ui.IdeDataSection


@Composable
fun IdeInfoTabContent(
    ideDataInfo: IdeDataInfo?
) {

    var firstExpanded by rememberSaveable { mutableStateOf(true) }
    var secondExpanded by rememberSaveable { mutableStateOf(true) }
    var thirdExpanded by rememberSaveable { mutableStateOf(true) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberLazyGridState()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            state = scrollState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (ideDataInfo != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    IdeSummaryCard(ideDataInfo = ideDataInfo)
                }


                if (ideDataInfo.firstCategoryGroup.installations.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        IdeGroupSummaryCard(
                            isExpanded = firstExpanded,
                            type = ideDataInfo.firstCategoryGroup.type,
                            label = ideDataInfo.firstCategoryGroup.totalLabel,
                            dataInfo = ideDataInfo.firstCategoryGroup,
                            onExpandChange = { firstExpanded = !firstExpanded }
                        )
                    }
                    if (firstExpanded) {
                        items(
                            items = ideDataInfo.firstCategoryGroup.installations,
                            key = { ideInstallation -> ideInstallation.uniqueId }
                        ) { ideInstallation ->
                            IdeInstallationItem(
                                ideInstallation = ideInstallation,
                                type = ideDataInfo.firstCategoryGroup.type
                            )
                        }
                    }
                } else {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateCardLayout(
                            title = ideDataInfo.firstCategoryGroup.type.messageTitle,
                            description = ideDataInfo.firstCategoryGroup.type.messageDescription,
                            icon = ideDataInfo.firstCategoryGroup.type.icon
                        )
                    }
                }
                if (ideDataInfo.secondCategoryGroup.installations.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        IdeGroupSummaryCard(
                            isExpanded = secondExpanded,
                            type = ideDataInfo.secondCategoryGroup.type,
                            label = ideDataInfo.secondCategoryGroup.totalLabel,
                            dataInfo = ideDataInfo.secondCategoryGroup,
                            onExpandChange = { secondExpanded = !secondExpanded }
                        )
                    }
                    if (secondExpanded) {
                        items(
                            items = ideDataInfo.secondCategoryGroup.installations,
                            key = { ideInstallation -> ideInstallation.uniqueId }
                        ) { ideInstallation ->
                            IdeInstallationItem(
                                ideInstallation = ideInstallation,
                                type = ideDataInfo.secondCategoryGroup.type
                            )
                        }
                    }
                } else {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateCardLayout(
                            title = ideDataInfo.secondCategoryGroup.type.messageTitle,
                            description = ideDataInfo.secondCategoryGroup.type.messageDescription,
                            icon = ideDataInfo.secondCategoryGroup.type.icon
                        )
                    }
                }

                if (ideDataInfo.thirdCategoryGroup.installations.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        IdeGroupSummaryCard(
                            isExpanded = thirdExpanded,
                            type = ideDataInfo.thirdCategoryGroup.type,
                            label = ideDataInfo.thirdCategoryGroup.totalLabel,
                            dataInfo = ideDataInfo.thirdCategoryGroup,
                            onExpandChange = { thirdExpanded = !thirdExpanded }
                        )
                    }
                    if (thirdExpanded) {
                        items(
                            items = ideDataInfo.thirdCategoryGroup.installations,
                            key = { ideInstallation -> ideInstallation.uniqueId }
                        ) { ideInstallation ->
                            IdeInstallationItem(
                                ideInstallation = ideInstallation,
                                type = ideDataInfo.thirdCategoryGroup.type
                            )
                        }
                    }
                } else {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateCardLayout(
                            title = ideDataInfo.thirdCategoryGroup.type.messageTitle,
                            description = ideDataInfo.thirdCategoryGroup.type.messageDescription,
                            icon = ideDataInfo.thirdCategoryGroup.type.icon
                        )
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateCardLayout(
                        title = "No Data Found",
                        description = "No Data Found",
                        icon = Icons.Default.FolderOpen
                    )
                }
            }
        }

        VerticalScrollBarLayout(adapter = rememberScrollbarAdapter(scrollState))
    }
}

@Composable
fun IdeSummaryCard(
    ideDataInfo: IdeDataInfo,
) {
    SummaryExpandableSectionLayout(
        expandableSection = IdeDataSection.IdeSummary
    ) {
        SummaryStatItem(
            label = ideDataInfo.firstCategoryGroup.totalLabel,
            value = ideDataInfo.firstCategoryGroup.installations.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = ideDataInfo.secondCategoryGroup.totalLabel,
            value = ideDataInfo.secondCategoryGroup.installations.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = ideDataInfo.thirdCategoryGroup.totalLabel,
            value = ideDataInfo.thirdCategoryGroup.installations.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Installations",
            value = ideDataInfo.totalInstallations.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = ideDataInfo.totalSizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}


@Composable
fun IdeInstallationItem(
    ideInstallation: IdeInstallation,
    type: ExpandableSection,
) {
    DetailCardRowLayout {
        HeaderCardRow(
            icon = type.icon,
            sizeReadable = ideInstallation.sizeReadable,
            primaryText = ideInstallation.ideName,
            secondaryText = ideInstallation.version,
            tertiaryText = ideInstallation.vendor
        )
        DetailCardRowValueLayout(
            label = "Path",
            value = ideInstallation.path,
            icon = Icons.Default.FolderOpen,
            showFullValue = true
        ) { ideInstallation.path.openFile() }
    }
}

@Composable
fun IdeGroupSummaryCard(
    type: ExpandableSection,
    isExpanded: Boolean,
    label: String,
    dataInfo: IdeGroup,
    onExpandChange: () -> Unit
) {
    SummaryExpandableSectionLayout(
        expandableSection = type,
        isExpanded = isExpanded,
        onExpandChange = onExpandChange
    ) {
        SummaryStatItem(
            label = label,
            value = dataInfo.installations.size.toString(),
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
        SummaryStatItem(
            label = "Total Size",
            value = dataInfo.sizeReadable,
            valueColor = it.valueColor(),
            labelColor = it.labelColor()
        )
    }
}
