package com.meet.dev.analyzer

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager
import com.meet.dev.analyzer.di.initKoin
import com.meet.dev.analyzer.presentation.navigation.AppNavigation
import com.meet.dev.analyzer.presentation.screen.app.AppUiIntent
import com.meet.dev.analyzer.presentation.screen.app.AppViewModel
import com.meet.dev.analyzer.presentation.theme.DevAnalyzerTheme
import com.meet.dev.analyzer.utility.crash_report.CustomProperties
import com.meet.dev.analyzer.utility.platform.getDesktopOS
import com.meet.dev.analyzer.utility.platform.isMacOs
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit

fun main() {
    val properties = CustomProperties.loadProperties()
    val appConfig = CustomProperties.createAppConfig(properties)
    initKoin()
    FileKit.init(appId = "DevAnalyzer")
    System.setProperty("apple.awt.application.appearance", "system")
    application {
        val appPreferenceManager = koinInject<AppPreferenceManager>()
        val windowWidth by appPreferenceManager.windowWidth.collectAsState()
        val windowHeight by appPreferenceManager.windowHeight.collectAsState()
        val windowPositionX by appPreferenceManager.windowPositionX.collectAsState()
        val windowPositionY by appPreferenceManager.windowPositionY.collectAsState()
        if (windowWidth == null || windowHeight == null) {
            return@application
        }
        val windowState = windowState(
            savedWidthDp = windowWidth!!,
            savedHeightDp = windowHeight!!,
            savedPositionX = windowPositionX,
            savedPositionY = windowPositionY
        )
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = if (getDesktopOS().isMacOs()) {
                ""
            } else "DevAnalyzer",
            icon = painterResource(Res.drawable.app_logo)
        ) {
            window.minimumSize = Dimension(1024, 768)
            val appViewModel = koinViewModel<AppViewModel>()
            val appUiState by appViewModel.appUiState.collectAsState()
            LaunchedEffect(appUiState.crashReportingEnabled) {
                CustomProperties.setupCrashReporting(
                    appConfig = appConfig,
                    isCrashReportEnabled = appUiState.crashReportingEnabled
                )
            }
            LaunchedEffect(appUiState.isLocalLogsEnabled) {
                CustomProperties.setupLocalLogs(
                    isLocalLogsEnabled = appUiState.isLocalLogsEnabled
                )
            }

            DevAnalyzerTheme(darkTheme = appUiState.isDarkMode) {
                val surfaceColor = MaterialTheme.colors.surface.toArgb()
                val backgroundColor = Color(surfaceColor)

                LaunchedEffect(appUiState.isDarkMode, window.rootPane) {
                    window.background = backgroundColor
                    window.contentPane.background = backgroundColor
                    if (getDesktopOS().isMacOs()) {
                        window.rootPane.background = backgroundColor
                        with(window.rootPane) {
                            putClientProperty("apple.awt.transparentTitleBar", true)
                            putClientProperty("apple.awt.fullWindowContent", true)
                        }
                    }
                }
                AppNavigation(
                    isDarkMode = appUiState.isDarkMode,
                    onThemeChange = {
                        appViewModel.handleIntent(AppUiIntent.ChangeTheme(appUiState.isDarkMode))
                    }
                )

                LaunchedEffect(windowState) {
                    snapshotFlow { windowState.position }
                        .collect { position ->
                            appViewModel.saveWindowPosition(
                                position = position
                            )
                        }
                }
                LaunchedEffect(windowState) {
                    snapshotFlow { windowState.size }
                        .collect { size ->
                            appViewModel.saveWindowWidthHeight(
                                width = size.width.value,
                                height = size.height.value,
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