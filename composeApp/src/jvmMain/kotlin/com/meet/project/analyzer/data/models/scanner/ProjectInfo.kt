package com.meet.project.analyzer.data.models.scanner


data class ProjectInfo(
    val projectPath: String,
    val projectName: String,
    val projectType: ProjectType,
    val modules: List<ModuleInfo>,
    val buildFiles: List<BuildFileInfo>,
    val dependencies: List<DependencyInfo>,
    val allLibraries: List<LibraryInfo>,
    val projectFiles: List<ProjectFileInfo>,
    val totalSize: String,
    val totalSizeBytes: Long,
    val gradleVersion: String?,
    val kotlinVersion: String?,
    val androidGradlePluginVersion: String?,
    val targetSdkVersion: String?,
    val minSdkVersion: String?,
    val isMultiModule: Boolean
)

data class ModuleInfo(
    val name: String,
    val path: String,
    val type: ModuleType,
    val buildFile: String?,
    val sourceFiles: Int,
    val resourceFiles: Int,
    val size: String,
    val sizeBytes: Long,
    val dependencies: List<DependencyInfo>
)

data class BuildFileInfo(
    val name: String,
    val path: String,
    val type: BuildFileType,
    val size: String,
    val sizeBytes: Long,
    val content: String? = null
)

data class DependencyInfo(
    val name: String,
    val version: String,
    val type: DependencyType,
    val scope: String,
    val module: String
)

data class LibraryInfo(
    val name: String,
    val group: String,
    val artifact: String,
    val allVersions: List<String>,
    val usedInModules: List<String>,
    val isUsed: Boolean,
    val dependencyType: DependencyType?,
    val latestVersion: String,
    val hasVersionConflict: Boolean
)

data class ProjectFileInfo(
    val name: String,
    val path: String,
    val relativePath: String,
    val type: FileType,
    val size: String,
    val sizeBytes: Long,
    val extension: String,
    val content: String? = null,
    val isReadable: Boolean = true
)

enum class FileType {
    SOURCE_KOTLIN,
    SOURCE_JAVA,
    BUILD_SCRIPT,
    CONFIGURATION,
    RESOURCE,
    MANIFEST,
    LAYOUT,
    DRAWABLE,
    VALUES,
    ASSETS,
    PROPERTIES,
    JSON,
    XML,
    TEXT,
    MARKDOWN,
    IMAGE,
    OTHER
}

enum class ProjectType {
    ANDROID,
    KOTLIN_MULTIPLATFORM,
    JAVA,
    COMPOSE_MULTIPLATFORM,
    SPRING_BOOT,
    UNKNOWN
}

enum class ModuleType {
    APP,
    LIBRARY,
    FEATURE,
    CORE,
    DATA,
    DOMAIN,
    PRESENTATION,
    UNKNOWN
}

enum class BuildFileType {
    BUILD_GRADLE_KTS,
    BUILD_GRADLE,
    SETTINGS_GRADLE_KTS,
    SETTINGS_GRADLE,
    VERSION_CATALOG,
    GRADLE_PROPERTIES,
    LOCAL_PROPERTIES
}

enum class DependencyType {
    IMPLEMENTATION,
    API,
    COMPILE_ONLY,
    RUNTIME_ONLY,
    TEST_IMPLEMENTATION,
    ANDROID_TEST_IMPLEMENTATION,
    KAPT,
    KSP,
    PLUGIN
}

data class Dependency(
    val group: String,
    val name: String,
    val currentVersion: String,
    val latestVersion: String?,
    val availableVersions: List<String>, // All available versions
    val configuration: String,
    val module: String,
    val fileType: String
)

data class Plugin(
    val id: String,
    val currentVersion: String?,
    val latestVersion: String?,
    val availableVersions: List<String>, // All available versions
    val module: String,
    val fileType: String
)

data class ProjectAnalysis(
    val dependencies: List<Dependency>,
    val plugins: List<Plugin>,
    val modules: List<String>
)
