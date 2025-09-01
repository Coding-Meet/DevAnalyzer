package com.meet.project.analyzer.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {

    @Serializable
    data object ProjectScanner : AppRoute

    @Serializable
    data object Dependencies : AppRoute

    @Serializable
    data object Storage : AppRoute

    @Serializable
    data object Settings : AppRoute

}