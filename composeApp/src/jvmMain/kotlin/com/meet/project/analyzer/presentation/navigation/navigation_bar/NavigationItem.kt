package com.meet.project.analyzer.presentation.navigation.navigation_bar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import com.meet.project.analyzer.core.utility.IconResource
import com.meet.project.analyzer.core.utility.UiText
import com.meet.project.analyzer.presentation.navigation.AppRoute

enum class NavigationItem(
    val title: UiText,
    val route: AppRoute,
    val selectedIcon: IconResource,
    val unSelectedIcon: IconResource,
    val description: UiText,
) {
    PROJECT_SCANNER(
        title = UiText.DynamicString("Scanner"),
        route = AppRoute.ProjectScanner,
        selectedIcon = IconResource.ImageVector(Icons.Filled.FolderOpen),
        unSelectedIcon = IconResource.ImageVector(Icons.Outlined.FolderOpen),
        description = UiText.DynamicString("Scan and analyze projects")
    ),
    DEPENDENCIES(
        title = UiText.DynamicString("Dependencies"),
        route = AppRoute.Dependencies,
        selectedIcon = IconResource.ImageVector(Icons.Filled.AccountTree),
        unSelectedIcon = IconResource.ImageVector(Icons.Outlined.AccountTree),
        description = UiText.DynamicString("View dependency analysis")
    ),
    STORAGE(
        title = UiText.DynamicString("Storage"),
        route = AppRoute.Storage,
        selectedIcon = IconResource.ImageVector(Icons.Filled.Storage),
        unSelectedIcon = IconResource.ImageVector(Icons.Outlined.Storage),
        description = UiText.DynamicString("System storage analysis")
    ),
    SETTINGS(
        title = UiText.DynamicString("Settings"),
        route = AppRoute.Settings,
        selectedIcon = IconResource.ImageVector(Icons.Filled.Settings),
        unSelectedIcon = IconResource.ImageVector(Icons.Outlined.Settings),
        description = UiText.DynamicString("Application settings")
    )
}