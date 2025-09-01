package com.meet.project.analyzer.presentation.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import kotlin.reflect.KClass

fun NavBackStackEntry?.isRouteInHierarchy(route: KClass<*>) =
    this?.destination?.hierarchy?.any {
        it.hasRoute(route)
    } == true