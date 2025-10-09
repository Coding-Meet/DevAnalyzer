package com.meet.project.analyzer.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import com.meet.project.analyzer.core.utility.TabItem
import java.awt.Cursor


@Composable
fun <EnumEntries : TabItem> TabLayout(
    selectedTabIndex: Int,
    tabList: List<EnumEntries>,
    onClick: (previousTabIndex: Int, currentTabIndex: Int, tabItem: EnumEntries) -> Unit,
) {
    TabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        tabList.forEachIndexed { index, tabItem ->
            val isSelected = selectedTabIndex == index
            CustomToolTip(
                title = tabItem.title,
                description = tabItem.description
            ) {
                Tab(
                    selected = isSelected,
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                            )
                        )
                    ),
                    onClick = {
                        onClick(
                            selectedTabIndex,
                            index,
                            tabItem
                        )
                    },
                    text = {
                        Text(
                            tabItem.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun <S> TabSlideAnimation(
    selectedTabIndex: Int,
    previousTabIndex: Int,
    targetState: S,
    content: @Composable() AnimatedContentScope.(targetState: S) -> Unit
) {
    val direction by rememberSaveable(selectedTabIndex) {
        derivedStateOf {
            if (selectedTabIndex > previousTabIndex) 1 else -1
        }
    }
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth * direction }
            ) + fadeIn()).togetherWith(
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth * direction }
                ) + fadeOut())
        },
        content = content
    )
}