package com.meet.project.analyzer.presentation.screen.dependencies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.meet.project.analyzer.data.repository.system_dependency.SystemDependency
import org.koin.compose.koinInject


@Composable
fun SystemDependencyScreen() {
    val systemDependency = koinInject<SystemDependency>()

    LaunchedEffect(Unit) {
        systemDependency.getAllDependencies("/Users/meet/AndroidStudioProjects")
    }
}