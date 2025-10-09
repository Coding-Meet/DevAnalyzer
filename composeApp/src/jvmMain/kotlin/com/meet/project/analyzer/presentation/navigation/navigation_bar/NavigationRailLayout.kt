package com.meet.project.analyzer.presentation.navigation.navigation_bar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meet.project.analyzer.presentation.components.CustomToolTip
import java.awt.Cursor

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
                CustomToolTip(
                    title = navigationItem.title.asString(),
                    description = navigationItem.description.asString()
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