package com.meet.project.analyzer.data.models.scanner


data class ProjectInfo(
    val projectOverviewInfo: ProjectOverviewInfo,
    val plugins: List<Plugin>,
    val dependencies: List<Dependency>,
    val moduleBuildFileInfos: List<ModuleBuildFileInfo>,
    val settingsGradleFileInfo: SettingsGradleFileInfo?,
    val propertiesFileInfo: PropertiesFileInfo?,
    val gradleWrapperPropertiesFileInfo: GradleWrapperPropertiesFileInfo?,
    val versionCatalogFileInfo: VersionCatalogFileInfo?,
    val versionCatalog: VersionCatalog?,
    val projectFiles: List<ProjectFileInfo>,
)

//
//enum class ProjectType {
//    ANDROID,
//    KOTLIN_MULTIPLATFORM,
//    JAVA,
//    COMPOSE_MULTIPLATFORM,
//    SPRING_BOOT,
//    UNKNOWN
//}