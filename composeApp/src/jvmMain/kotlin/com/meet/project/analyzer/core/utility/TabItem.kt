package com.meet.project.analyzer.core.utility

interface TabItem {
    val title: String
}

enum class ProjectScreenTabs(override val title: String) : TabItem {
    Overview("Overview"),
    Modules("Modules"),
    Plugins("Plugins"),
    Dependencies("Dependencies"),
    BuildFiles("Build Files"),
    ProjectFiles("Project Files")
}

enum class StorageAnalyzerTabs(override val title: String) : TabItem {
    Overview("Overview"),
    AVDs("AVDs"),
    SDK("SDK"),
    Environment("Environment"),
    Caches("Caches"),
    Libraries("Libraries"),
    Charts("Charts")
}
