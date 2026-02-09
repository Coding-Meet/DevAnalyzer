package com.meet.dev.analyzer.utility

import com.meet.dev.analyzer.utility.platform.DesktopOS
import com.meet.dev.analyzer.utility.platform.getDesktopOS


fun getDefaultAndroidSdkPath(): String {
    val userHome = System.getProperty("user.home")
    return when (getDesktopOS()) {
        DesktopOS.WINDOWS -> System.getenv("ANDROID_HOME")
            ?: "$userHome\\AppData\\Local\\Android\\Sdk"

        DesktopOS.MAC -> System.getenv("ANDROID_HOME")
            ?: "$userHome/Library/Android/sdk"

        DesktopOS.LINUX -> System.getenv("ANDROID_HOME")
            ?: "$userHome/Android/Sdk"
    }
}

fun getDefaultGradleHomePath(): String {
    val userHome = System.getProperty("user.home")
    return System.getenv("GRADLE_HOME")
        ?: "$userHome/.gradle"
}

fun getDefaultAvdLocationPath(): String {
    val userHome = System.getProperty("user.home")
    return System.getenv("ANDROID_AVD_HOME")
        ?: "$userHome/.android/avd"
}

fun getDefaultAndroidFolderPath(): String {
    val userHome = System.getProperty("user.home")
    return "$userHome/.android"
}

fun getDefaultKonanFolderPath(): String {
    val userHome = System.getProperty("user.home")
    return "$userHome/.konan"
}

fun getDefaultJetbrainsFolderPaths(): List<String> {
    val userHome = System.getProperty("user.home")
    return when (getDesktopOS()) {
        DesktopOS.WINDOWS -> listOf(
            "C:/Program Files/JetBrains",
            "$userHome\\AppData\\Local\\JetBrains",
            "$userHome\\AppData\\Roaming\\JetBrains"
        )

        DesktopOS.MAC -> listOf(
            "$userHome/Library/Caches/JetBrains",
            "$userHome/Library/Logs/JetBrains",
            "$userHome/Library/Application Support/JetBrains"
        )

        DesktopOS.LINUX -> listOf( // Linux / Unix
            "$userHome/.cache/JetBrains",
            "$userHome/.cache/JetBrains",
            "$userHome/.config/JetBrains"
        )
    }
}

fun getDefaultGoogleFolderPaths(): List<String> {
    val userHome = System.getProperty("user.home")
    return when (getDesktopOS()) {
        DesktopOS.WINDOWS -> listOf(
            "C:/Program Files/Android",
            "$userHome\\AppData\\Local\\Google",
            "$userHome\\AppData\\Roaming\\Google"
        )

        DesktopOS.MAC -> listOf(
            "$userHome/Library/Caches/Google",
            "$userHome/Library/Logs/Google",
            "$userHome/Library/Application Support/Google"
        )

        DesktopOS.LINUX -> listOf( // Linux / Unix
            "$userHome/.cache/Google",
            "$userHome/.cache/Google",
            "$userHome/.config/Google"
        )
    }
}

fun getDefaultJdkFolderPaths(): List<String> {
    val userHome = System.getProperty("user.home")

    return when (getDesktopOS()) {
        DesktopOS.MAC ->
            listOf(
                "$userHome/Library/Java/JavaVirtualMachines",
                "/Library/Java/JavaVirtualMachines",
                "$userHome/.gradle/jdks",
            )

        DesktopOS.WINDOWS ->
            listOf(
                "C:\\Program Files\\Java",
                "C:\\Program Files\\Eclipse Adoptium",
                "$userHome\\.gradle\\jdks"
            )

        DesktopOS.LINUX ->
            listOf(
                "/usr/lib/jvm",
                "$userHome/.gradle/jdks",
                "$userHome/.sdkman/candidates/java"
            )
    }
}

