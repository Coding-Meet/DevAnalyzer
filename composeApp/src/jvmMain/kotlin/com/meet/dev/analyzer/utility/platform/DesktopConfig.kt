package com.meet.dev.analyzer.utility.platform

data class DesktopConfig(
    val sentryDns: String?,
    val version: String,
    val enableAnalytics: Boolean,
    val enableSentry: Boolean,
    val appEnvironment: AppEnvironment,
    val updaterUrl: String,
    val feedbackUrl: String,
    val os: DesktopOS = getDesktopOS(),
)

sealed class AppEnvironment(val label: String) {
    data object Debug : AppEnvironment("Debug")
    data object Release : AppEnvironment("Release")

    val isDebug get() = this == Debug
    val isRelease get() = this == Release
}
