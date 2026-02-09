package com.meet.dev.analyzer.core.utility

data class DesktopConfig(
    val sentryDns: String?,
    val version: String?,
    val appEnvironment: AppEnvironment,
    val os: DesktopOS = getDesktopOS(),
)

sealed class AppEnvironment {
    data object Debug : AppEnvironment()
    data object Release : AppEnvironment()

    fun isDebug(): Boolean =
        this is Debug

    fun isRelease(): Boolean =
        this is Release
}
