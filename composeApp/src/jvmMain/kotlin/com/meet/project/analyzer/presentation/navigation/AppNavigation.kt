package com.meet.project.analyzer.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.meet.project.analyzer.presentation.navigation.navigation_bar.NavigationItem
import com.meet.project.analyzer.presentation.navigation.navigation_bar.NavigationRailLayout
import com.meet.project.analyzer.presentation.screen.dependencies.MainApp
import com.meet.project.analyzer.presentation.screen.storage.StorageAnalyzerScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()

    val currentNavigationItem by remember(navBackStackEntry) {
        derivedStateOf {
            NavigationItem.entries.find { navigationItem ->
                navBackStackEntry.value.isRouteInHierarchy(navigationItem.route::class)
            }
        }
    }

    val currentWindowAdaptiveInfo = currentWindowAdaptiveInfo()
    val layoutType by remember(currentNavigationItem, currentWindowAdaptiveInfo) {
        derivedStateOf {
            if (currentNavigationItem != null) {
                NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo)
            } else {
                NavigationSuiteType.None
            }
        }
    }
    NavigationSuiteScaffoldLayout(
        navigationSuite = {
            AnimatedVisibility(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                visible = currentNavigationItem != null,
                enter = slideInHorizontally(
                    // Slide in from the left
                    initialOffsetX = { fullWidth -> -fullWidth }
                ),
                exit = slideOutHorizontally(
                    // Slide out to the right
                    targetOffsetX = { fullWidth -> -fullWidth }
                ),
            ) {
                NavigationRailLayout(
                    currentNavigationItem = currentNavigationItem,
                    onNavigate = {
                        navController.navigate(it.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // re-selecting the same item
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        },
        layoutType = layoutType
    ) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Storage
        ) {
            composable<AppRoute.ProjectScanner> {

            }
            composable<AppRoute.Dependencies> {
                MainApp()
            }
            composable<AppRoute.Storage> {
                StorageAnalyzerScreen()
            }
            composable<AppRoute.Settings> {

            }
        }
    }
}