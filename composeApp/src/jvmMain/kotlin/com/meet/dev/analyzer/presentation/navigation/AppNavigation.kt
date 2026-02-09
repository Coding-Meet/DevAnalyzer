package com.meet.dev.analyzer.presentation.navigation

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldValue
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.meet.dev.analyzer.presentation.navigation.navigation_bar.NavigationItem
import com.meet.dev.analyzer.presentation.navigation.navigation_bar.NavigationRailLayout
import com.meet.dev.analyzer.presentation.screen.cleanbuild.CleanBuildScreen
import com.meet.dev.analyzer.presentation.screen.onboarding.OnboardingScreen
import com.meet.dev.analyzer.presentation.screen.project.ProjectAnalyzerScreen
import com.meet.dev.analyzer.presentation.screen.setting.SettingsScreen
import com.meet.dev.analyzer.presentation.screen.splash.SplashScreen
import com.meet.dev.analyzer.presentation.screen.storage.StorageAnalyzerScreen

@Composable
fun AppNavigation(
    isDarkMode: Boolean,
    onThemeChange: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentNavigationItem by remember(navBackStackEntry) {
        derivedStateOf {
            NavigationItem.entries.find { navigationItem ->
                navBackStackEntry.isRouteInHierarchy(navigationItem.appRoute::class)
            }
        }
    }
    val navigationSuiteState = rememberNavigationSuiteScaffoldState(
        initialValue = NavigationSuiteScaffoldValue.Hidden
    )
    LaunchedEffect(currentNavigationItem) {
        if (currentNavigationItem != null) {
            navigationSuiteState.show()
        } else {
            navigationSuiteState.hide()
        }
    }
    NavigationSuiteScaffoldLayout(
        state = navigationSuiteState,
        navigationSuite = {
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
        },
        layoutType = NavigationSuiteType.NavigationRail
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
                composable<AppRoute.Settings> {
                    val parentEntry = remember(navController) {
                        navController.getBackStackEntry(AppRoute.MainGraph)
                    }
                    SettingsScreen(
                        parentEntry = parentEntry
                    )
                }
                composable<AppRoute.CleanBuild> {
                    val parentEntry = remember(navController) {
                        navController.getBackStackEntry(AppRoute.MainGraph)
                    }
                    CleanBuildScreen(
                        parentEntry = parentEntry
                    )
                }
            }
        }
    }
}
