package com.meet.project.analyzer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.meet.project.analyzer.di.initKoin
import com.meet.project.analyzer.presentation.navigation.AppNavigation
import com.meet.project.analyzer.presentation.theme.ProjectAnalyzerTheme
import java.awt.Dimension

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(
            position = WindowPosition(Alignment.Center)
        ),
        title = "Project Analyzer",
    ) {
        window.minimumSize = Dimension(1280, 720)
        ProjectAnalyzerTheme(true) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigation()
            }
        }
    }
}