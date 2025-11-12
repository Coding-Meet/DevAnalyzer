package com.meet.dev.analyzer.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {

    @Serializable
    data object SplashGraph : AppRoute

    @Serializable
    data object Splash : AppRoute

    @Serializable
    data object Onboarding : AppRoute

    @Serializable
    data object MainGraph : AppRoute

    @Serializable
    data object ProjectAnalyzer : AppRoute

    @Serializable
    data object StorageAnalyzer : AppRoute

    @Serializable
    data object Settings : AppRoute

}