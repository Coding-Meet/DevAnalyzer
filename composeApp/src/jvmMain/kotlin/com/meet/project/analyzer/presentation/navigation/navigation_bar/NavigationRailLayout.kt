package com.meet.project.analyzer.presentation.navigation.navigation_bar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onThemeChange: () -> Unit
) {
    Row {
        NavigationRail(
            modifier = modifier.fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Spacer(Modifier.weight(1f))

            NavigationItem.entries.forEach { navigationItem ->
                CustomToolTip(
                    title = navigationItem.title,
                    description = navigationItem.description
                ) {
                    val isSelected by rememberSaveable(
                        navigationItem.appRoute,
                        currentNavigationItem?.appRoute
                    ) {
                        derivedStateOf {
                            navigationItem.appRoute == currentNavigationItem?.appRoute
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
                                if (isSelected) navigationItem.selectedIcon else navigationItem.unSelectedIcon,
                                contentDescription = navigationItem.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = navigationItem.title,
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
            ThemeSwitch(
                isDarkMode = isDarkTheme,
                onThemeChange = onThemeChange
            )
            Spacer(Modifier.height(4.dp))
        }
        VerticalDivider(
            modifier = Modifier.fillMaxHeight()
                .wrapContentWidth(),
        )
    }

}

@Composable
fun ThemeSwitch(
    isDarkMode: Boolean,
    onThemeChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode
    val tooltipText = if (isDarkMode) "Switch to Light Theme" else "Switch to Dark Theme"

    CustomToolTip(
        title = "Theme",
        description = tooltipText,
    ) {
        IconButton(
            onClick = onThemeChange,
            modifier = modifier
                .padding(8.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        ) {
            val rotation by animateFloatAsState(if (isDarkMode) 360f else 0f)
            Icon(
                imageVector = icon,
                contentDescription = tooltipText,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(rotation)
            )
        }
    }
}