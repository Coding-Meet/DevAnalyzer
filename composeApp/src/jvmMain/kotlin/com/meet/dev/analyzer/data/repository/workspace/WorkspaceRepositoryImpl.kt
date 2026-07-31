package com.meet.dev.analyzer.data.repository.workspace

import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.data.models.project.ProjectOverviewInfo
import com.meet.dev.analyzer.data.models.workspace.ResourceCategory
import com.meet.dev.analyzer.data.models.workspace.UnusedResourceItem
import com.meet.dev.analyzer.data.models.workspace.WorkspaceAnalysisResult
import com.meet.dev.analyzer.data.repository.project.helpers.*
import com.meet.dev.analyzer.data.repository.storage.helpers.*
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WorkspaceRepositoryImpl(
    private val pathPreferenceManger: PathPreferenceManger,
    private val projectFileScanner: ProjectFileScanner,
    private val versionCatalogParser: VersionCatalogParser,
    private val projectOverviewAnalyzer: ProjectOverviewAnalyzer,
    private val androidSdkAnalyzer: AndroidSdkAnalyzer,
    private val gradleAnalyzer: GradleAnalyzer,
    private val konanAnalyzer: KonanAnalyzer
) : WorkspaceRepository {

    private val TAG = tagName(javaClass)

    override suspend fun analyzeWorkspace(
        workspacePaths: List<String>,
        sdkPath: String,
        gradleHomePath: String,
        konanPath: String,
        onProgress: (progress: Float, status: String) -> Unit
    ): WorkspaceAnalysisResult = withContext(Dispatchers.IO) {
        AppLogger.i(TAG) { "Starting workspace analysis.\nWorkspaces: $workspacePaths\nSDK: $sdkPath\nGradle: $gradleHomePath\nKonan: $konanPath" }

        try {
            // 1. Scan Projects across all workspace directories
            onProgress(0.1f, "Scanning project folders...")
            val projectDirs = mutableListOf<File>()
            workspacePaths.forEach { path ->
                val workspaceDir = File(path)
                if (workspaceDir.exists() && workspaceDir.isDirectory) {
                    projectDirs.addAll(projectFileScanner.findGradleProjects(workspaceDir))
                }
            }
            val totalProjects = projectDirs.size.coerceAtLeast(1)

            val projects = mutableListOf<ProjectOverviewInfo>()
            val usedCompileSdks = mutableSetOf<String>()
            val usedMinSdks = mutableSetOf<String>()
            val usedTargetSdks = mutableSetOf<String>()
            val usedBuildTools = mutableSetOf<String>()
            val usedGradleVersions = mutableSetOf<String>()
            val usedKotlinVersions = mutableSetOf<String>()
            val usedNdkVersions = mutableSetOf<String>()
            val usedCmakeVersions = mutableSetOf<String>()

            projectDirs.forEachIndexed { index, projectDir ->
                val progressVal = 0.1f + (0.5f * (index.toFloat() / totalProjects))
                onProgress(progressVal, "Analyzing project: ${projectDir.name}")

                val moduleBuildFileInfos = projectFileScanner.findModuleBuildFiles(projectDir)
                val settingsGradleFileInfo = projectFileScanner.findSettingsGradleFiles(projectDir)
                val gradleWrapperPropertiesFileInfo = projectFileScanner.findGradleWrapperProFile(projectDir)
                val versionCatalogFileInfo = projectFileScanner.findVersionCatalogFile(projectDir)

                val versionCatalog = versionCatalogParser.findVersionCatalog(versionCatalogFileInfo)

                val overview = projectOverviewAnalyzer.findProjectOverviewInfo(
                    projectDir = projectDir,
                    settingsGradleFileInfo = settingsGradleFileInfo,
                    gradleWrapperPropertiesFileInfo = gradleWrapperPropertiesFileInfo,
                    versionCatalog = versionCatalog,
                    moduleBuildFileInfos = moduleBuildFileInfos
                )

                projects.add(overview)

                overview.compileSdkVersion?.let { 
                    usedCompileSdks.add(it)
                    // Auto-infer primary build-tools version matching compileSdk
                    usedBuildTools.add("$it.0.0")
                }
                overview.minSdkVersion?.let { usedMinSdks.add(it) }
                overview.targetSdkVersion?.let { usedTargetSdks.add(it) }
                overview.buildToolsSdk?.let { usedBuildTools.add(it) }
                overview.gradleVersion?.let { usedGradleVersions.add(it) }
                overview.kotlinVersion?.let { usedKotlinVersions.add(it) }
                overview.ndkVersion?.let { usedNdkVersions.add(it) }
                overview.cmakeVersion?.let { usedCmakeVersions.add(it) }
            }

            AppLogger.i(TAG) {
                "Active project stats:\n" +
                        "Compile SDKs: $usedCompileSdks\n" +
                        "Build Tools: $usedBuildTools\n" +
                        "Gradle: $usedGradleVersions\n" +
                        "Kotlin: $usedKotlinVersions\n" +
                        "NDK: $usedNdkVersions\n" +
                        "CMake: $usedCmakeVersions"
            }

            // 2. Scan Local System Resources
            val unusedResources = mutableListOf<UnusedResourceItem>()
            val activeResources = mutableListOf<UnusedResourceItem>()

            // 2.a Android SDK Platforms
            onProgress(0.6f, "Scanning installed Android SDK Platforms...")
            val sdkInfo = androidSdkAnalyzer.analyzeAndroidSdkData()
            val apiRegex = Regex("""android-(\d+)""")
            sdkInfo.platformInfo.platforms.forEach { item ->
                val apiVal = apiRegex.find(item.name)?.groupValues?.get(1) ?: item.name.substringAfter("android-")
                val isUsed =
                    usedCompileSdks.contains(apiVal) || usedCompileSdks.any { it.contains(apiVal) } ||
                            usedTargetSdks.contains(apiVal) || usedTargetSdks.any { it.contains(apiVal) } ||
                            usedMinSdks.contains(apiVal) || usedMinSdks.any { it.contains(apiVal) }
                val usingProjects = if (isUsed) {
                    projects.filter {
                        it.compileSdkVersion == apiVal || it.compileSdkVersion?.contains(apiVal) == true ||
                                it.targetSdkVersion == apiVal || it.targetSdkVersion?.contains(apiVal) == true ||
                                it.minSdkVersion == apiVal || it.minSdkVersion?.contains(apiVal) == true
                    }.map { it.projectName }
                } else emptyList()
                val resource = UnusedResourceItem(
                    name = "Android Platform API $apiVal",
                    version = apiVal,
                    category = ResourceCategory.ANDROID_SDK_PLATFORM,
                    path = item.path,
                    sizeBytes = item.sizeBytes,
                    sizeFormatted = item.sizeReadable,
                    usedByProjects = usingProjects
                )
                if (isUsed) activeResources.add(resource) else unusedResources.add(resource)
            }

            // 2.b Android SDK Build Tools
            onProgress(0.7f, "Scanning installed Android SDK Build Tools...")
            sdkInfo.buildToolInfo.buildTools.forEach { item ->
                val buildToolsVersion = item.name
                val isUsed = usedBuildTools.contains(buildToolsVersion) || usedCompileSdks.any { sdk -> buildToolsVersion.startsWith("$sdk.") }
                val usingProjects = if (isUsed) {
                    projects.filter {
                        it.buildToolsSdk == buildToolsVersion ||
                        it.compileSdkVersion?.let { sdk -> buildToolsVersion.startsWith("$sdk.") } == true
                    }.map { it.projectName }
                } else emptyList()
                val resource = UnusedResourceItem(
                    name = "Build Tools $buildToolsVersion",
                    version = buildToolsVersion,
                    category = ResourceCategory.ANDROID_BUILD_TOOLS,
                    path = item.path,
                    sizeBytes = item.sizeBytes,
                    sizeFormatted = item.sizeReadable,
                    usedByProjects = usingProjects
                )
                if (isUsed) activeResources.add(resource) else unusedResources.add(resource)
            }

            // 2.c Gradle Wrappers & Version-specific Caches
            onProgress(0.8f, "Scanning Gradle wrapper cache...")
            val gradleInfo = gradleAnalyzer.analyzeGradleData()
            gradleInfo.wrapperInfo.wrapperItems.forEach { item ->
                val version = item.version
                val isUsed = usedGradleVersions.contains(version)
                val usingProjects = if (isUsed) {
                    projects.filter { it.gradleVersion == version }.map { it.projectName }
                } else emptyList()
                val resource = UnusedResourceItem(
                    name = "Gradle Wrapper $version",
                    version = version,
                    category = ResourceCategory.GRADLE_WRAPPER,
                    path = item.path,
                    sizeBytes = item.sizeBytes,
                    sizeFormatted = item.sizeReadable,
                    usedByProjects = usingProjects
                )
                if (isUsed) activeResources.add(resource) else unusedResources.add(resource)

                // Check for matching version cache folder
                val cacheItem = gradleInfo.cachesGradleWrapperInfo.cachesGradleWrapperItems.find { it.version == version }
                if (cacheItem != null) {
                    val cacheResource = UnusedResourceItem(
                        name = "Gradle Cache $version",
                        version = version,
                        category = ResourceCategory.GRADLE_WRAPPER,
                        path = cacheItem.path,
                        sizeBytes = cacheItem.sizeBytes,
                        sizeFormatted = cacheItem.sizeReadable,
                        usedByProjects = usingProjects
                    )
                    if (isUsed) activeResources.add(cacheResource) else unusedResources.add(cacheResource)
                }
            }

            // 2.d Gradle Dependency Library Cache (modules-2)
            onProgress(0.85f, "Scanning Gradle global dependency cache...")
            val gradleDependencyCacheDir = File(gradleHomePath, "caches/modules-2")
            if (gradleDependencyCacheDir.exists() && gradleDependencyCacheDir.isDirectory) {
                val size = FolderFileUtils.calculateFolderSize(gradleDependencyCacheDir, false)
                if (size > 0) {
                    // Global caches are cleanable on request, show as unused
                    unusedResources.add(
                        UnusedResourceItem(
                            name = "Gradle Library Dependency Cache",
                            version = "Global",
                            category = ResourceCategory.GRADLE_DEPENDENCY_CACHE,
                            path = gradleDependencyCacheDir.absolutePath,
                            sizeBytes = size,
                            sizeFormatted = FolderFileUtils.formatSize(size)
                        )
                    )
                }
            }

            // 2.e Kotlin Native Compilers
            onProgress(0.9f, "Scanning Kotlin Native cache...")
            val konanInfo = konanAnalyzer.analyzeKonanData()
            konanInfo.kotlinNativeInfo.kotlinNativeItems.forEach { item ->
                val version = item.version ?: "Unknown"
                val isUsed = usedKotlinVersions.contains(version)
                val usingProjects = if (isUsed) {
                    projects.filter { it.kotlinVersion == version }.map { it.projectName }
                } else emptyList()
                val resource = UnusedResourceItem(
                    name = "Kotlin Native Compiler $version",
                    version = version,
                    category = ResourceCategory.KOTLIN_NATIVE,
                    path = item.path,
                    sizeBytes = item.sizeBytes,
                    sizeFormatted = item.sizeReadable,
                    usedByProjects = usingProjects
                )
                if (isUsed) activeResources.add(resource) else unusedResources.add(resource)
            }

            // 2.f Android SDK NDK Versions
            onProgress(0.92f, "Scanning Android SDK NDK versions...")
            sdkInfo.ndkInfo.ndkItems.forEach { item ->
                val version = item.name
                val isUsed = usedNdkVersions.contains(version)
                val usingProjects = if (isUsed) {
                    projects.filter { it.ndkVersion == version }.map { it.projectName }
                } else emptyList()
                val resource = UnusedResourceItem(
                    name = "Android NDK $version",
                    version = version,
                    category = ResourceCategory.ANDROID_NDK,
                    path = item.path,
                    sizeBytes = item.sizeBytes,
                    sizeFormatted = item.sizeReadable,
                    usedByProjects = usingProjects
                )
                if (isUsed) activeResources.add(resource) else unusedResources.add(resource)
            }

            // 2.g Android SDK CMake Versions
            onProgress(0.94f, "Scanning Android SDK CMake versions...")
            sdkInfo.cmakeInfo.cmakeItems.forEach { item ->
                val version = item.name
                val isUsed = usedCmakeVersions.contains(version)
                val usingProjects = if (isUsed) {
                    projects.filter { it.cmakeVersion == version }.map { it.projectName }
                } else emptyList()
                val resource = UnusedResourceItem(
                    name = "Android CMake $version",
                    version = version,
                    category = ResourceCategory.ANDROID_CMAKE,
                    path = item.path,
                    sizeBytes = item.sizeBytes,
                    sizeFormatted = item.sizeReadable,
                    usedByProjects = usingProjects
                )
                if (isUsed) activeResources.add(resource) else unusedResources.add(resource)
            }

            // 2.h Android SDK Sources
            onProgress(0.96f, "Scanning Android SDK Sources...")
            sdkInfo.sourcesInfo.sources.forEach { item ->
                val version = item.name
                val isUsed = usedCompileSdks.contains(version) || usedCompileSdks.any { it.contains(version) }
                val usingProjects = if (isUsed) {
                    projects.filter { it.compileSdkVersion == version || it.compileSdkVersion?.contains(version) == true }.map { it.projectName }
                } else emptyList()
                val resource = UnusedResourceItem(
                    name = "Android SDK Sources $version",
                    version = version,
                    category = ResourceCategory.ANDROID_SDK_SOURCES,
                    path = item.path,
                    sizeBytes = item.sizeBytes,
                    sizeFormatted = item.sizeReadable,
                    usedByProjects = usingProjects
                )
                if (isUsed) activeResources.add(resource) else unusedResources.add(resource)
            }

            onProgress(1f, "Analysis Complete")
            WorkspaceAnalysisResult(
                projects = projects,
                unusedResources = unusedResources.sortedByDescending { it.sizeBytes },
                activeResources = activeResources.sortedByDescending { it.sizeBytes }
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Error analyzing workspace" }
            WorkspaceAnalysisResult(emptyList(), emptyList(), emptyList())
        }
    }

    override suspend fun deleteResource(path: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(path)
            if (!file.exists()) {
                Pair(false, "Path does not exist: $path")
            } else {
                val success = file.deleteRecursively()
                if (success) {
                    Pair(true, null)
                } else {
                    Pair(false, "Failed to delete files recursively. Check permissions or file usage lock.")
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Exception deleting path: $path" }
            Pair(false, e.message)
        }
    }
}
