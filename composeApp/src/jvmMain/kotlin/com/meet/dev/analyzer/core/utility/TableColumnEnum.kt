package com.meet.dev.analyzer.core.utility


enum class PluginColumn(
    val title: String,
    val weight: Float,
    val description: String
) {
    NAME(
        title = "Name",
        weight = 0.34f,
        description = "The plugin name, group, and full ID applied in the project."
    ),
    CURRENT_VERSION(
        title = "Current",
        weight = 0.14f,
        description = "The version of the plugin currently used in your project."
    ),
    AVAILABLE_VERSIONS(
        title = "Versions",
        weight = 0.14f,
        description = "Versions of this plugin present locally in the Gradle cache on your system."
    ),
    CONFIGURATION(
        title = "Configuration",
        weight = 0.19f,
        description = "Specifies how the plugin is applied (e.g., via build.gradle, version catalog, etc)."
    ),
    MODULE(
        title = "Module",
        weight = 0.19f,
        description = "Indicates which sub-module of your project applies this plugin."
    )
}


enum class DependencyColumn(
    val title: String,
    val weight: Float,
    val description: String
) {
    NAME(
        title = "Name",
        weight = 0.34f,
        description = "The library name, group, and full ID used in the project."
    ),
    CURRENT_VERSION(
        title = "Current",
        weight = 0.14f,
        description = "The version of the library currently declared in your project build files."
    ),
    AVAILABLE_VERSIONS(
        title = "Available",
        weight = 0.14f,
        description = "Versions of this library that exist locally in the Gradle cache on your system."
    ),
    CONFIGURATION(
        title = "Configuration",
        weight = 0.19f,
        description = "Specifies how this dependency is added (implementation, api, compileOnly, etc.)."
    ),
    MODULE(
        title = "Module",
        weight = 0.19f,
        description = "Indicates which sub-module of your project uses this dependency."
    )
}

enum class StorageLibraryColumn(
    val title: String,
    val weight: Float,
    val description: String
) {
    NAME(
        title = "Name",
        weight = 0.34f,
        description = "The library name, group, and full ID used in the project."
    ),
    GROUP(
        title = "Group",
        weight = 0.20f,
        description = "The group of the library."
    ),
    AVAILABLE_VERSIONS(
        title = "Available",
        weight = 0.12f,
        description = "Versions of this library that exist locally in the Gradle cache on your system."
    ),
    TOTAL_SIZE_OPEN_FILE(
        title = "Total Size",
        weight = 0.16f,
        description = "The total size of the library in the Gradle cache. "
    ),
}