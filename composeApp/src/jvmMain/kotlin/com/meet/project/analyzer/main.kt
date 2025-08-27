package com.meet.project.analyzer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Project Analyzer",
    ) {
        App()
    }
}