package com.meet.project.analyzer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.meet.project.analyzer.di.initKoin
import com.meet.project.analyzer.presentation.navigation.AppNavigation
import com.meet.project.analyzer.presentation.theme.ProjectAnalyzerTheme
import io.github.vinceglb.filekit.FileKit
import java.awt.Dimension

fun main() {
    initKoin()
    FileKit.init(appId = "Project_Analyzer")
    System.setProperty("apple.awt.application.appearance", "system")
    application {
        val windowState = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 1024.dp, height = 760.dp)
        )
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Project Analyzer",
        ) {
            window.minimumSize = Dimension(1024, 760)
            ProjectAnalyzerTheme(true) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}