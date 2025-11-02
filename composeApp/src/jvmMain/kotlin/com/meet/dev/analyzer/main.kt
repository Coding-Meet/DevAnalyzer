package com.meet.dev.analyzer

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.meet.dev.analyzer.core.utility.AppLogger
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager
import com.meet.dev.analyzer.data.datastore.defaultWindowPosition
import com.meet.dev.analyzer.data.datastore.defaultWindowSize
import com.meet.dev.analyzer.di.initKoin
import com.meet.dev.analyzer.presentation.navigation.AppNavigation
import com.meet.dev.analyzer.presentation.screen.app.AppUiIntent
import com.meet.dev.analyzer.presentation.screen.app.AppViewModel
import com.meet.dev.analyzer.presentation.theme.DevAnalyzerTheme
import devanalyzer.composeapp.generated.resources.Res
import devanalyzer.composeapp.generated.resources.app_logo
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Dimension

fun main() {
    initKoin()
    FileKit.init(appId = "DevAnalyzer")
    System.setProperty("apple.awt.application.appearance", "system")
    application {
        val appPreferenceManager = koinInject<AppPreferenceManager>()
        val preferredWindowSize by appPreferenceManager.windowSize.collectAsState(
            initial = defaultWindowSize
        )
        val preferredWindowPosition by appPreferenceManager.windowPosition.collectAsState(
            initial = defaultWindowPosition
        )
        val windowState = rememberWindowState(
            size = preferredWindowSize,
            position = WindowPosition(Alignment.Center), /// preferredWindowPosition it not work proper
        )
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "DevAnalyzer",
            icon = painterResource(Res.drawable.app_logo)
        ) {
            window.minimumSize = Dimension(1024, 768)
            val appViewModel = koinViewModel<AppViewModel>()
            val appUiState by appViewModel.appUiState.collectAsState()

            DevAnalyzerTheme(appUiState.isDarkMode) {
                AppNavigation(
                    isDarkMode = appUiState.isDarkMode,
                    onThemeChange = {
                        appViewModel.handleIntent(AppUiIntent.ChangeTheme(appUiState.isDarkMode))
                    }
                )
                LaunchedEffect(preferredWindowSize) {
                    windowState.size = preferredWindowSize
                }
//                LaunchedEffect(preferredWindowPosition) {
//                    windowState.position = preferredWindowPosition
//                }
                LaunchedEffect(windowState) {
                    snapshotFlow { windowState.position }
                        .collect { position ->
                            AppLogger.d("window_position") {
                                "window position changed to $position"
                            }
                            if (preferredWindowPosition != position) {
                                appViewModel.saveWindowPosition(
                                    position = position
                                )
                            }
                        }
                }
                LaunchedEffect(windowState) {
                    snapshotFlow { windowState.size }
                        .collect { size ->
                            AppLogger.d("window_size") {
                                "window size changed to $size"
                            }
                            if (preferredWindowSize != size) {
                                appViewModel.saveWindowWidthHeight(
                                    width = windowState.size.width.value,
                                    height = windowState.size.height.value,
                                )
                            }
                        }
                }
            }
        }
    }
}