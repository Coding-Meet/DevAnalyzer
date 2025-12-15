package com.meet.dev.analyzer.presentation.navigation.navigation_bar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.ui.graphics.vector.ImageVector
import com.meet.dev.analyzer.presentation.navigation.AppRoute

enum class NavigationItem(
    val title: String,
    val appRoute: AppRoute,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    val description: String,
) {
    Project(
        title = "Project",
        appRoute = AppRoute.ProjectAnalyzer,
        selectedIcon = Icons.Filled.AccountTree,
        unSelectedIcon = Icons.Outlined.AccountTree,
        description = "Analyze project structure, modules, plugins, and dependencies."
    ),
    Storage(
        title = "Storage",
        appRoute = AppRoute.StorageAnalyzer,
        selectedIcon = Icons.Filled.Storage,
        unSelectedIcon = Icons.Outlined.Storage,
        description = "Analyze SDK, IDE, Gradle, and library storage usage."
    ),
    CleanBuild(
        title = "Clean Build",
        appRoute = AppRoute.CleanBuild,
        selectedIcon = Icons.Filled.Delete,
        unSelectedIcon = Icons.Outlined.Delete,
        description = "Find and delete build folders to free up space."
    ),
    Settings(
        title = "Settings",
        appRoute = AppRoute.Settings,
        selectedIcon = Icons.Filled.Settings,
        unSelectedIcon = Icons.Outlined.Settings,
        description = "Configure SDK, IDE, JDK, and tool paths, manage preferences, and control privacy options."
    )
}