package com.meet.project.analyzer.presentation.navigation.navigation_bar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import java.awt.Cursor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationRailLayout(
    currentNavigationItem: NavigationItem?,
    onNavigate: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row {
        NavigationRail(
            modifier = modifier.fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Spacer(Modifier.weight(1f))

            NavigationItem.entries.forEach { navigationItem ->
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        RichTooltip(
                            title = {
                                Text(
                                    text = navigationItem.title.asString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            text = {
                                Text(
                                    text = navigationItem.description.asString(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            caretSize = DpSize(32.dp, 16.dp),
                            colors = TooltipDefaults.richTooltipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                                actionContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    state = rememberTooltipState(),
                ) {
                    val isSelected by rememberSaveable(
                        navigationItem.route,
                        currentNavigationItem?.route
                    ) {
                        derivedStateOf {
                            navigationItem.route == currentNavigationItem?.route
                        }
                    }

                    NavigationRailItem(
                        modifier = Modifier.pointerHoverIcon(
                            PointerIcon(
                                Cursor.getPredefinedCursor(
                                    Cursor.HAND_CURSOR
                                )
                            )
                        ).padding(5.dp),
                        icon = {
                            Icon(
                                (if (isSelected) navigationItem.selectedIcon else navigationItem.unSelectedIcon).asPainterResource(),
                                contentDescription = navigationItem.title.asString(),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = navigationItem.title.asString(),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = isSelected,
                        onClick = { onNavigate(navigationItem) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(Modifier.weight(1f))
        }
        VerticalDivider(
            modifier = Modifier.fillMaxHeight()
                .wrapContentWidth(),
        )
    }

}