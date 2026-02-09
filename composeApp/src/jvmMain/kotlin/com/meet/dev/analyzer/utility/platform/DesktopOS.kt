package com.meet.dev.analyzer.utility.platform

enum class DesktopOS {
    WINDOWS,
    MAC,
    LINUX,
}

fun getDesktopOS(): DesktopOS {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains("win") -> DesktopOS.WINDOWS
        osName.contains("mac") -> DesktopOS.MAC
        else -> DesktopOS.LINUX
    }
}

fun DesktopOS.isMacOs() = this == DesktopOS.MAC
fun DesktopOS.isNotMacOs() = !isMacOs()

fun DesktopOS.isLinux() = this == DesktopOS.LINUX
fun DesktopOS.isNotLinux() = !isLinux()

fun DesktopOS.isWindows() = this == DesktopOS.WINDOWS
fun DesktopOS.isNotWindows() = !isWindows()

