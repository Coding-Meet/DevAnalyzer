package com.meet.dev.analyzer

import androidx.compose.material3.MaterialTheme
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
import com.meet.dev.analyzer.presentation.screen.app.UpdateDialog
import com.meet.dev.analyzer.presentation.screen.app.UpdateDialogState
import com.meet.dev.analyzer.presentation.theme.DevAnalyzerTheme
import com.meet.dev.analyzer.utility.analytics.AnalyticsEvent
import com.meet.dev.analyzer.utility.analytics.AnalyticsInitializer
import com.meet.dev.analyzer.utility.analytics.AnalyticsManager
import com.meet.dev.analyzer.utility.crash_report.CustomProperties
import com.meet.dev.analyzer.utility.platform.getDesktopOS
import com.meet.dev.analyzer.utility.platform.isMacOs
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.qualifier.named
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit

fun main() {
    val properties = CustomProperties.loadProperties()
    val appConfig = CustomProperties.createAppConfig(properties)
    val analyticsConfig = CustomProperties.createAnalyticsConfig(properties)
    initKoin(
        appConfig = appConfig,
        analyticsConfig = analyticsConfig,
    )
    FileKit.init(appId = "DevAnalyzer")
    System.setProperty("apple.awt.application.appearance", "system")

    //  Initialize PostHog once — before entering the Compose application block
    AnalyticsInitializer.initialize(
        apiKey = analyticsConfig.apiKey,
        host = analyticsConfig.host,
        isDebug = appConfig.appEnvironment.isDebug,
        appVersion = appConfig.version,
        operatingSystem = getDesktopOS().name,
    )
    application {
        val appPreferenceManager = koinInject<AppPreferenceManager>()
        val analyticsManager = koinInject<AnalyticsManager>()
        val analyticsScope = koinInject<CoroutineScope>(named("analyticsScope"))

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
        val isDarkMode by appPreferenceManager.isDarkMode.collectAsState(true)
        Window(
            // Shutdown order: capture → flush → close → cancel scope → exit
            onCloseRequest = {
                analyticsManager.capture(AnalyticsEvent.AppClosed)
                AnalyticsInitializer.flushAndClose()
                analyticsScope.cancel()
                exitApplication()
            },
            state = windowState,
            title = if (getDesktopOS().isMacOs()) {
                ""
            } else "DevAnalyzer",
            icon = if (isDarkMode) painterResource(Res.drawable.dark_mode_logo) else painterResource(
                Res.drawable.light_mode_logo
            )
        ) {
            window.minimumSize = Dimension(1024, 768)
            val appViewModel = koinViewModel<AppViewModel>()
            val appUiState by appViewModel.appUiState.collectAsState()

            //  app_opened: LaunchedEffect(Unit) fires exactly once per app session
            LaunchedEffect(Unit) {
                analyticsManager.capture(AnalyticsEvent.AppOpened)
            }

            //  Theme super property auto-updates on every dark/light change
            LaunchedEffect(appUiState.isDarkMode) {
                AnalyticsInitializer.updateTheme(appUiState.isDarkMode)
            }

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
                val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
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
                    },
                    updateDialogWithNavigation = { currentNavigationItem ->
                        val updateState = appViewModel.updateDialogState
                        if (updateState is UpdateDialogState.Available && currentNavigationItem != null) {
                            LaunchedEffect(updateState) {
                                appViewModel.trackUpdateDialogShown()
                            }
                            UpdateDialog(
                                state = updateState,
                                onDismiss = {
                                    appViewModel.trackUpdateDismissed()
                                    appViewModel.closeUpdateDialog()
                                },
                                onUpdateClicked = {
                                    appViewModel.trackUpdateClicked()
                                }
                            )
                        }
                    }
                )

                LaunchedEffect(windowState) {
                    snapshotFlow { windowState.position }
                        .collect { position ->
                            appViewModel.saveWindowPosition(position = position)
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