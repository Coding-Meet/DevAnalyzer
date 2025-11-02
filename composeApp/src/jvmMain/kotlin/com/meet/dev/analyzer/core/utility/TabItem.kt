package com.meet.dev.analyzer.core.utility

interface TabItem {
    val title: String
    val description: String
}

enum class ProjectScreenTabs(
    override val title: String,
    override val description: String
) : TabItem {

    Overview(
        title = "Overview",
        description = "View overall project summary including size, modules, plugins, dependencies, and SDK details."
    ),

    Modules(
        title = "Modules",
        description = "Inspect each Gradle module, its configurations, and applied plugins or dependency blocks."
    ),

    Plugins(
        title = "Plugins",
        description = "List all Gradle plugins used across modules with versions, configurations, and usage details."
    ),

    Dependencies(
        title = "Dependencies",
        description = "Analyze all project dependencies with versions, configurations, and module mapping."
    ),

    BuildFiles(
        title = "Build Files",
        description = "Browse and preview build.gradle.kts files for each module in a single view."
    ),

    ProjectFiles(
        title = "Project Files",
        description = "Search and open any file from the project directory including configuration and documentation files."
    )
}

enum class StorageAnalyzerTabs(
    override val title: String,
    override val description: String
) : TabItem {

    Overview(
        title = "Overview",
        description = "Summary of total storage used and a breakdown across Gradle, IDE, AVD, SDK, Kotlin/Native & JDK, and libraries."
    ),

    IdeData(
        title = "IDE",
        description = "Details for installed IDEs (Android Studio / IntelliJ), their caches, logs, support files and total installations."
    ),

    AvdAndSystemImages(
        title = "AVD & System Images",
        description = "Lists configured AVD devices and downloaded system images with their allocated/configured sizes."
    ),

    AndroidSdk(
        title = "Android SDK",
        description = "Shows SDK Platforms, Build Tools, Sources, NDK versions, CMake, and Extras with paths and sizes."
    ),

    KotlinNativeJdk(
        title = "Kotlin/Native & JDK",
        description = "Displays installed JDK versions, Kotlin/Native toolchains, and LLVM/LLDB dependencies with sizes and paths."
    ),

    Gradle(
        title = "Gradle",
        description = "Analyze Gradle wrappers, daemon instances, caches, and other Gradle folders (build-cache, transforms, jars) and their sizes."
    ),

    Libraries(
        title = "Libraries",
        description = "Browse Gradle-cached libraries: groups, versions, available versions count and total storage used by each library."
    )
}
