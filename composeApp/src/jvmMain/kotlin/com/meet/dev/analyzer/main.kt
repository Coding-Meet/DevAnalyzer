package com.meet.dev.analyzer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.meet.dev.analyzer.core.utility.AppLogger
import com.meet.dev.analyzer.data.datastore.defaultWindowSize
import com.meet.dev.analyzer.di.initKoin
import com.meet.dev.analyzer.presentation.navigation.AppNavigation
import com.meet.dev.analyzer.presentation.screen.app.AppUiIntent
import com.meet.dev.analyzer.presentation.screen.app.AppViewModel
import com.meet.dev.analyzer.presentation.theme.DevAnalyzerTheme
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Dimension
import java.awt.Toolkit

fun main() {
    initKoin()
    FileKit.init(appId = "DevAnalyzer")
    System.setProperty("apple.awt.application.appearance", "system")
    application {
//        val appPreferenceManager = koinInject<AppPreferenceManager>()
//        val preferredWindowSize by appPreferenceManager.windowSize.collectAsState()
//        val preferredWindowPosition by appPreferenceManager.windowPosition.collectAsState()
//        val windowWidth by appPreferenceManager.windowWidth.collectAsState()
//        val windowHeight by appPreferenceManager.windowHeight.collectAsState()
//        val windowPositionX by appPreferenceManager.windowPositionX.collectAsState()
//        val windowPositionY by appPreferenceManager.windowPositionY.collectAsState()
//        val windowState = windowState(
//            savedWidthDp = windowWidth,
//            savedHeightDp = windowHeight,
//            savedPositionX = windowPositionX,
//            savedPositionY = windowPositionY
//        )
        val windowState = rememberWindowState(
            size = defaultWindowSize,
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

                LaunchedEffect(windowState) {
                    snapshotFlow { windowState.position }
                        .collect { position ->
                            AppLogger.d("window_position") {
                                "window position changed to $position"
                            }
                            appViewModel.saveWindowPosition(
                                position = position
                            )
                        }
                }
                LaunchedEffect(windowState) {
                    snapshotFlow { windowState.size }
                        .collect { size ->
                            AppLogger.d("window_size") {
                                "window size changed to $size"
                            }
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

@Composable
private fun windowState(
    savedWidthDp: Dp,
    savedHeightDp: Dp,
    savedPositionX: Dp?,
    savedPositionY: Dp?
): WindowState {

    val toolkit = Toolkit.getDefaultToolkit()
    val screenSize = toolkit.screenSize
    val maxWidth = screenSize.width.dp
    val maxHeight = screenSize.height.dp

    val width = savedWidthDp.coerceAtMost(maxWidth)
    val height = savedHeightDp.coerceAtMost(maxHeight)

    val xPos = savedPositionX
        ?.coerceIn(0.dp, (maxWidth - width).coerceAtLeast(minimumValue = 0.dp))
        ?: Dp.Unspecified

    val yPos = savedPositionY
        ?.coerceIn(0.dp, (maxHeight - height).coerceAtLeast(minimumValue = 0.dp))
        ?: Dp.Unspecified

    val position = if (xPos != Dp.Unspecified && yPos != Dp.Unspecified) {
        WindowPosition.Absolute(
            x = xPos,
            y = yPos,
        )
    } else {
        WindowPosition.PlatformDefault
    }

    val windowState = rememberWindowState(
        size = DpSize(width, height),
        position = position,
    )
    return windowState
}