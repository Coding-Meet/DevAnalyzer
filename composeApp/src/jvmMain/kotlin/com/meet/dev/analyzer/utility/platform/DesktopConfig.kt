package com.meet.dev.analyzer.utility.platform

data class DesktopConfig(
    val sentryDns: String?,
    val version: String?,
    val appEnvironment: AppEnvironment,
    val os: DesktopOS = getDesktopOS(),
)

sealed class AppEnvironment(val label: String) {
    data object Debug : AppEnvironment("Debug")
    data object Release : AppEnvironment("Release")

    fun isDebug(): Boolean =
        this is Debug

    fun isRelease(): Boolean =
        this is Release
}
