package com.meet.dev.analyzer.data.repository.workspace

import com.meet.dev.analyzer.data.models.workspace.ResourceCategory
import com.meet.dev.analyzer.data.models.workspace.UnusedResourceItem
import com.meet.dev.analyzer.data.models.workspace.WorkspaceAnalysisResult
import com.meet.dev.analyzer.data.models.workspace.WorkspaceProjectInfo
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WorkspaceRepositoryImpl : WorkspaceRepository {

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
                    projectDirs.addAll(findGradleProjects(workspaceDir))
                }
            }
            val totalProjects = projectDirs.size.coerceAtLeast(1)

            val projects = mutableListOf<WorkspaceProjectInfo>()
            val usedCompileSdks = mutableSetOf<String>()
            val usedMinSdks = mutableSetOf<String>()
            val usedTargetSdks = mutableSetOf<String>()
            val usedBuildTools = mutableSetOf<String>()
            val usedGradleVersions = mutableSetOf<String>()
            val usedKotlinVersions = mutableSetOf<String>()

            projectDirs.forEachIndexed { index, projectDir ->
                val progressVal = 0.1f + (0.5f * (index.toFloat() / totalProjects))
                onProgress(progressVal, "Analyzing project: ${projectDir.name}")

                val info = parseProjectMetadata(projectDir)
                projects.add(info)

                info.compileSdk?.let { 
                    usedCompileSdks.add(it)
                    // Auto-infer primary build-tools version matching compileSdk
                    usedBuildTools.add("$it.0.0")
                }
                info.minSdk?.let { usedMinSdks.add(it) }
                info.targetSdk?.let { usedTargetSdks.add(it) }
                info.buildToolsVersion?.let { usedBuildTools.add(it) }
                info.gradleVersion?.let { usedGradleVersions.add(it) }
                info.kotlinVersion?.let { usedKotlinVersions.add(it) }
            }

            AppLogger.i(TAG) {
                "Active project stats:\n" +
                        "Compile SDKs: $usedCompileSdks\n" +
                        "Build Tools: $usedBuildTools\n" +
                        "Gradle: $usedGradleVersions\n" +
                        "Kotlin: $usedKotlinVersions"
            }

            // 2. Scan Local System Resources
            onProgress(0.6f, "Scanning installed Android SDK Platforms...")
            val unusedResources = mutableListOf<UnusedResourceItem>()
            val activeResources = mutableListOf<UnusedResourceItem>()

            // 2.a Android SDK Platforms
            val sdkPlatformsDir = File(sdkPath, "platforms")
            if (sdkPlatformsDir.exists() && sdkPlatformsDir.isDirectory) {
                val apiRegex = Regex("""android-(\d+)""")
                sdkPlatformsDir.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
                    val apiVal = apiRegex.find(dir.name)?.groupValues?.get(1) ?: dir.name.substringAfter("android-")
                    val isUsed =
                        usedCompileSdks.contains(apiVal) || usedCompileSdks.any { it.contains(apiVal) } ||
                                usedTargetSdks.contains(apiVal) || usedTargetSdks.any {
                            it.contains(
                                apiVal
                            )
                        } ||
                                usedMinSdks.contains(apiVal) || usedMinSdks.any { it.contains(apiVal) }
                    val size = FolderFileUtils.calculateFolderSize(dir, false)
                    val usingProjects = if (isUsed) {
                        projects.filter {
                            it.compileSdk == apiVal || it.compileSdk?.contains(apiVal) == true ||
                                    it.targetSdk == apiVal || it.targetSdk?.contains(apiVal) == true ||
                                    it.minSdk == apiVal || it.minSdk?.contains(apiVal) == true
                        }.map { it.projectName }
                    } else emptyList()
                    val resource = UnusedResourceItem(
                        name = "Android Platform API $apiVal",
                        version = apiVal,
                        category = ResourceCategory.ANDROID_SDK_PLATFORM,
                        path = dir.absolutePath,
                        sizeBytes = size,
                        sizeFormatted = FolderFileUtils.formatSize(size),
                        usedByProjects = usingProjects
                    )
                    if (isUsed) activeResources.add(resource) else unusedResources.add(resource)
                }
            }

            // 2.b Android SDK Build Tools
            onProgress(0.7f, "Scanning installed Android SDK Build Tools...")
            val sdkBuildToolsDir = File(sdkPath, "build-tools")
            if (sdkBuildToolsDir.exists() && sdkBuildToolsDir.isDirectory) {
                sdkBuildToolsDir.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
                    val buildToolsVersion = dir.name
                    val isUsed = usedBuildTools.contains(buildToolsVersion) || usedCompileSdks.any { sdk -> buildToolsVersion.startsWith("$sdk.") }
                    val size = FolderFileUtils.calculateFolderSize(dir, false)
                    val usingProjects = if (isUsed) {
                        projects.filter {
                            it.buildToolsVersion == buildToolsVersion ||
                            it.compileSdk?.let { sdk -> buildToolsVersion.startsWith("$sdk.") } == true
                        }.map { it.projectName }
                    } else emptyList()
                    val resource = UnusedResourceItem(
                        name = "Build Tools $buildToolsVersion",
                        version = buildToolsVersion,
                        category = ResourceCategory.ANDROID_BUILD_TOOLS,
                        path = dir.absolutePath,
                        sizeBytes = size,
                        sizeFormatted = FolderFileUtils.formatSize(size),
                        usedByProjects = usingProjects
                    )
                    if (isUsed) activeResources.add(resource) else unusedResources.add(resource)
                }
            }

            // 2.c Gradle Wrappers & Version-specific Caches
            onProgress(0.8f, "Scanning Gradle wrapper cache...")
            val gradleWrapperDistsDir = File(gradleHomePath, "wrapper/dists")
            val versionRegex = Regex("""\d+\.\d+(\.\d+)?""")
            if (gradleWrapperDistsDir.exists() && gradleWrapperDistsDir.isDirectory) {
                gradleWrapperDistsDir.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
                    val version = versionRegex.find(dir.name)?.value ?: dir.name
                    val isUsed = usedGradleVersions.contains(version)
                    val wrapperSize = FolderFileUtils.calculateFolderSize(dir, false)
                    val usingProjects = if (isUsed) {
                        projects.filter { it.gradleVersion == version }.map { it.projectName }
                    } else emptyList()
                    val resource = UnusedResourceItem(
                        name = "Gradle Wrapper $version",
                        version = version,
                        category = ResourceCategory.GRADLE_WRAPPER,
                        path = dir.absolutePath,
                        sizeBytes = wrapperSize,
                        sizeFormatted = FolderFileUtils.formatSize(wrapperSize),
                        usedByProjects = usingProjects
                    )
                    if (isUsed) activeResources.add(resource) else unusedResources.add(resource)

                    // Check if there is a matching cache folder in ~/.gradle/caches/
                    val versionCacheDir = File(gradleHomePath, "caches/$version")
                    if (versionCacheDir.exists() && versionCacheDir.isDirectory) {
                        val cacheSize = FolderFileUtils.calculateFolderSize(versionCacheDir, false)
                        val cacheResource = UnusedResourceItem(
                            name = "Gradle Cache $version",
                            version = version,
                            category = ResourceCategory.GRADLE_WRAPPER,
                            path = versionCacheDir.absolutePath,
                            sizeBytes = cacheSize,
                            sizeFormatted = FolderFileUtils.formatSize(cacheSize),
                            usedByProjects = usingProjects
                        )
                        if (isUsed) activeResources.add(cacheResource) else unusedResources.add(cacheResource)
                    }
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
            val konanRootDir = File(konanPath)
            if (konanRootDir.exists() && konanRootDir.isDirectory) {
                konanRootDir.listFiles()
                    ?.filter { it.isDirectory && it.name.contains("kotlin-native-prebuilt") }
                    ?.forEach { dir ->
                        // Extract version suffix e.g., kotlin-native-prebuilt-macos-aarch64-2.0.21 -> 2.0.21
                        val version = dir.name.substringAfterLast("-")
                        val isUsed = usedKotlinVersions.contains(version)
                        val size = FolderFileUtils.calculateFolderSize(dir, false)
                        val usingProjects = if (isUsed) {
                            projects.filter { it.kotlinVersion == version }.map { it.projectName }
                        } else emptyList()
                        val resource = UnusedResourceItem(
                            name = "Kotlin Native Compiler $version",
                            version = version,
                            category = ResourceCategory.KOTLIN_NATIVE,
                            path = dir.absolutePath,
                            sizeBytes = size,
                            sizeFormatted = FolderFileUtils.formatSize(size),
                            usedByProjects = usingProjects
                        )
                        if (isUsed) activeResources.add(resource) else unusedResources.add(resource)
                    }
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

    private fun findGradleProjects(rootDir: File): List<File> {
        val projects = mutableListOf<File>()
        rootDir.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
            val hasBuildGradle = File(dir, "build.gradle").exists() || File(dir, "build.gradle.kts").exists()
            val hasSettingsGradle = File(dir, "settings.gradle").exists() || File(dir, "settings.gradle.kts").exists()
            if (hasBuildGradle || hasSettingsGradle) {
                projects.add(dir)
            } else {
                // Check one level down for nested multi-module projects
                dir.listFiles()?.filter { it.isDirectory }?.forEach { subDir ->
                    val subBuild = File(subDir, "build.gradle").exists() || File(subDir, "build.gradle.kts").exists()
                    if (subBuild) {
                        projects.add(dir) // parent dir is the root project
                        return@forEach
                    }
                }
            }
        }
        return projects.distinct()
    }

    private fun parseProjectMetadata(projectDir: File): WorkspaceProjectInfo {
        var compileSdk: String? = null
        var minSdk: String? = null
        var targetSdk: String? = null
        var buildToolsVersion: String? = null
        var gradleVersion: String? = null
        var agpVersion: String? = null
        var kotlinVersion: String? = null

        // 1. Check gradle-wrapper.properties
        val gradleWrapperProps = File(projectDir, "gradle/wrapper/gradle-wrapper.properties")
        if (gradleWrapperProps.exists()) {
            val lines = gradleWrapperProps.readLines()
            val urlLine = lines.find { it.contains("distributionUrl") }
            if (urlLine != null) {
                gradleVersion = urlLine.substringAfter("gradle-").substringBefore("-bin").substringBefore("-all")
            }
        }

        // 2. Scan for build.gradle or build.gradle.kts files recursively (limit to modules)
        val buildFiles = mutableListOf<File>()
        val rootBuild = File(projectDir, "build.gradle.kts").takeIf { it.exists() }
            ?: File(projectDir, "build.gradle").takeIf { it.exists() }
        rootBuild?.let { buildFiles.add(it) }

        projectDir.listFiles()?.filter { it.isDirectory && !it.name.startsWith(".") && it.name != "build" && it.name != "gradle" }?.forEach { subDir ->
            val subBuild = File(subDir, "build.gradle.kts").takeIf { it.exists() }
                ?: File(subDir, "build.gradle").takeIf { it.exists() }
            subBuild?.let { buildFiles.add(it) }
        }

        // 3. Scan for libs.versions.toml
        val versionCatalogFile = File(projectDir, "gradle/libs.versions.toml")
        var catalogContent = ""
        if (versionCatalogFile.exists()) {
            catalogContent = versionCatalogFile.readText()
        }

        // Regex definitions aligned with ProjectAnalyzerRepositoryImpl
        val compileSdkRegex = Regex("compileSdk(?:Version)?\\s*=?\\s*(\\d+)")
        val compileSdkNewFormatRegex = Regex(
            "compileSdk\\s*\\{\\s*(?:[^{}]*)\\bversion\\s*=\\s*release\\((\\d+)\\)",
            RegexOption.DOT_MATCHES_ALL
        )
        val minSdkRegex = Regex("minSdk(?:Version)?\\s*=?\\s*(\\d+)")
        val targetSdkRegex = Regex("targetSdk(?:Version)?\\s*=?\\s*(\\d+)")
        val buildToolsRegex = Regex("buildToolsVersion\\s*=?\\s*[\"']?([\\d.]+)[\"']?")

        // AGP matching
        val agpPluginRegex =
            Regex("id\\(\"com\\.android\\.(?:application|library)\"\\)\\s+version\\s+\"([\\d.]+)\"")
        val agpKtsClasspathRegex =
            Regex("classpath\\(\"com\\.android\\.tools\\.build:gradle:([\\d.]+)\"\\)")
        val agpGroovyClasspathRegex =
            Regex("classpath\\s+['\"]com\\.android\\.tools\\.build:gradle:([\\d.]+)['\"]")

        // Kotlin matching
        val kotlinPluginRegex =
            Regex("id\\(\"org\\.jetbrains\\.kotlin\\.(?:android|jvm|multiplatform)\"\\)\\s+version\\s+\"([\\d.]+)\"")
        val kotlinKtsClasspathRegex =
            Regex("classpath\\(\"org\\.jetbrains\\.kotlin:kotlin-gradle-plugin:([\\d.]+)\"\\)")
        val kotlinGroovyClasspathRegex =
            Regex("classpath\\s+['\"]org\\.jetbrains\\.kotlin:kotlin-gradle-plugin:([\\d.]+)['\"]")

        // Parse versions from Toml Version Catalog
        if (catalogContent.isNotEmpty()) {
            val versionSection = catalogContent.substringAfter("[versions]").substringBefore("[")
            val compileSdkToml =
                Regex("""(?:android[.-])?compileSdk\s*=\s*["']?(\d+)["']?""").find(versionSection)?.groupValues?.get(
                    1
                )
            val minSdkToml =
                Regex("""(?:android[.-])?minSdk\s*=\s*["']?(\d+)["']?""").find(versionSection)?.groupValues?.get(
                    1
                )
            val targetSdkToml =
                Regex("""(?:android[.-])?targetSdk\s*=\s*["']?(\d+)["']?""").find(versionSection)?.groupValues?.get(
                    1
                )
            val agpToml =
                Regex("""(?:agp|androidGradlePlugin|android[.-]gradle[.-]plugin)\s*=\s*["']?([\d.]+)["']?""").find(
                    versionSection
                )?.groupValues?.get(1)
            val kotlinToml =
                Regex("""(?:kotlin|kotlin[.-]version)\s*=\s*["']?([\d.]+)["']?""").find(
                    versionSection
                )?.groupValues?.get(1)

            compileSdkToml?.let { compileSdk = it }
            minSdkToml?.let { minSdk = it }
            targetSdkToml?.let { targetSdk = it }
            agpToml?.let { agpVersion = it }
            kotlinToml?.let { kotlinVersion = it }
        }

        buildFiles.forEach { file ->
            val content = file.readText()

            compileSdkRegex.find(content)?.let { compileSdk = it.groupValues[1] }
            compileSdkNewFormatRegex.find(content)?.let { compileSdk = it.groupValues[1] }
            minSdkRegex.find(content)?.let { minSdk = it.groupValues[1] }
            targetSdkRegex.find(content)?.let { targetSdk = it.groupValues[1] }
            buildToolsRegex.find(content)?.let { buildToolsVersion = it.groupValues[1] }

            // AGP versions lookup
            agpPluginRegex.find(content)?.let { agpVersion = it.groupValues[1] }
            agpKtsClasspathRegex.find(content)?.let { agpVersion = it.groupValues[1] }
            agpGroovyClasspathRegex.find(content)?.let { agpVersion = it.groupValues[1] }

            // Kotlin versions lookup
            kotlinPluginRegex.find(content)?.let { kotlinVersion = it.groupValues[1] }
            kotlinKtsClasspathRegex.find(content)?.let { kotlinVersion = it.groupValues[1] }
            kotlinGroovyClasspathRegex.find(content)?.let { kotlinVersion = it.groupValues[1] }
        }

        return WorkspaceProjectInfo(
            projectName = projectDir.name,
            projectPath = projectDir.absolutePath,
            compileSdk = compileSdk,
            minSdk = minSdk,
            targetSdk = targetSdk,
            buildToolsVersion = buildToolsVersion,
            gradleVersion = gradleVersion,
            agpVersion = agpVersion,
            kotlinVersion = kotlinVersion
        )
    }
}
