package com.meet.dev.analyzer.presentation.navigation

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.meet.dev.analyzer.presentation.navigation.navigation_bar.NavigationItem
import com.meet.dev.analyzer.presentation.navigation.navigation_bar.NavigationRailLayout
import com.meet.dev.analyzer.presentation.screen.onboarding.OnboardingScreen
import com.meet.dev.analyzer.presentation.screen.project.ProjectAnalyzerScreen
import com.meet.dev.analyzer.presentation.screen.splash.SplashScreen
import com.meet.dev.analyzer.presentation.screen.storage.StorageAnalyzerScreen

@Composable
fun AppNavigation(
    isDarkMode: Boolean,
    onThemeChange: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()

    val currentNavigationItem by remember(navBackStackEntry) {
        derivedStateOf {
            NavigationItem.entries.find { navigationItem ->
                navBackStackEntry.value.isRouteInHierarchy(navigationItem.appRoute::class)
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
                visible = currentNavigationItem != null,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { it },
            ) {
                NavigationRailLayout(
                    currentNavigationItem = currentNavigationItem,
                    onNavigate = {
                        navController.navigate(it.appRoute) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    isDarkTheme = isDarkMode,
                    onThemeChange = onThemeChange,
                )
            }
        },
        layoutType = layoutType
    ) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.SplashGraph
        ) {
            // Splash + Onboarding Graph
            navigation<AppRoute.SplashGraph>(
                startDestination = AppRoute.Splash
            ) {
                composable<AppRoute.Splash> {
                    SplashScreen(
                        onSplashFinished = { route ->
                            navController.navigate(route) {
                                popUpTo(AppRoute.SplashGraph) { inclusive = true }
                            }
                        }
                    )
                }
                composable<AppRoute.Onboarding> {
                    OnboardingScreen(
                        onComplete = {
                            navController.navigate(AppRoute.MainGraph) {
                                popUpTo(AppRoute.SplashGraph) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Main Graph (tabs)
            navigation<AppRoute.MainGraph>(
                startDestination = AppRoute.ProjectAnalyzer
            ) {
                composable<AppRoute.ProjectAnalyzer> {
                    val parentEntry = remember(navController) {
                        navController.getBackStackEntry(AppRoute.MainGraph)
                    }
                    ProjectAnalyzerScreen(parentEntry = parentEntry)
                }
                composable<AppRoute.StorageAnalyzer> {
                    val parentEntry = remember(navController) {
                        navController.getBackStackEntry(AppRoute.MainGraph)
                    }
                    StorageAnalyzerScreen(parentEntry = parentEntry)
                }
            }
        }
    }
}
