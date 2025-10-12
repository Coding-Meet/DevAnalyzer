package com.meet.project.analyzer.presentation.screen.dependencies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.meet.project.analyzer.data.repository.system_dependency.SystemDependencyRepository
import org.koin.compose.koinInject


@Composable
fun SystemDependencyScreen() {
    val systemDependencyRepository = koinInject<SystemDependencyRepository>()

    LaunchedEffect(Unit) {
        systemDependencyRepository.getAllDependencies("/Users/meet/AndroidStudioProjects")
    }
}