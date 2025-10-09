package com.meet.project.analyzer.core.utility

interface TabItem {
    val title: String
    val description: String
}

enum class ProjectScreenTabs(
    override val title: String,
    override val description: String
) : TabItem {
    Overview(title = "Overview", description = "Overview of the project"),
    Modules(title = "Modules", description = "Modules of the project"),
    Plugins(title = "Plugins", description = "Plugins of the project"),
    Dependencies(title = "Dependencies", description = "Dependencies of the project"),
    BuildFiles(title = "Build Files", description = "Build files of the project"),
    ProjectFiles(title = "Project Files", description = "Project files of the project")
}

enum class StorageAnalyzerTabs(
    override val title: String,
    override val description: String
) : TabItem {
    Overview(title = "Overview", description = "Overview of the storage"),
    AVDs(title = "AVDs", description = "AVDs of the storage"),
    SDK(title = "SDK", description = "SDK of the storage"),
    Environment(title = "Environment", description = "Environment of the storage"),
    Caches(title = "Caches", description = "Caches of the storage"),
    Libraries(title = "Libraries", description = "Libraries of the storage"),
    Charts(title = "Charts", description = "Charts of the storage")
}
