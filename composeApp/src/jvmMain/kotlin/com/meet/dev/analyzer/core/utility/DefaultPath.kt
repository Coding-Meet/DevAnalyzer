package com.meet.dev.analyzer.core.utility


fun getDefaultAndroidSdkPath(): String {
    val userHome = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("windows") -> System.getenv("ANDROID_HOME")
            ?: "$userHome\\AppData\\Local\\Android\\Sdk"

        os.contains("mac") -> System.getenv("ANDROID_HOME")
            ?: "$userHome/Library/Android/sdk"

        else -> System.getenv("ANDROID_HOME")
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
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("windows") -> listOf(
            "C:/Program Files/JetBrains",
            "$userHome\\AppData\\Local\\JetBrains",
            "$userHome\\AppData\\Roaming\\JetBrains"
        )

        else -> listOf(
            "$userHome/Library/Caches/JetBrains",
            "$userHome/Library/Logs/JetBrains",
            "$userHome/Library/Application Support/JetBrains"
        )
    }
}

fun getDefaultGoogleFolderPaths(): List<String> {
    val userHome = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("windows") -> listOf(
            "C:/Program Files/Android",
            "$userHome\\AppData\\Local\\Google",
            "$userHome\\AppData\\Roaming\\Google"
        )

        else -> listOf(
            "$userHome/Library/Caches/Google",
            "$userHome/Library/Logs/Google",
            "$userHome/Library/Application Support/Google"
        )
    }
}

fun getDefaultJdkFolderPaths(): List<String> {
    val userHome = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()

    val defaultPaths = mutableListOf<String>()
    when {
        os.contains("mac") -> defaultPaths.addAll(
            listOf(
                "$userHome/Library/Java/JavaVirtualMachines",
                "/Library/Java/JavaVirtualMachines",
                "$userHome/.gradle/jdks",
            )
        )

        os.contains("windows") -> defaultPaths.addAll(
            listOf(
                "C:\\Program Files\\Java",
                "C:\\Program Files\\Eclipse Adoptium",
                "$userHome\\.gradle\\jdks"
            )
        )

        else -> defaultPaths.addAll(
            listOf(
                "/usr/lib/jvm",
                "$userHome/.gradle/jdks",
                "$userHome/.sdkman/candidates/java"
            )
        )
    }

    return defaultPaths
}

