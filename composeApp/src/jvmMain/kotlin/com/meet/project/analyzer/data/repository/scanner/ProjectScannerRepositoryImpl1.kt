package com.meet.project.analyzer.data.repository.scanner

import com.meet.project.analyzer.core.utility.AppLogger
import com.meet.project.analyzer.core.utility.StorageUtils
import com.meet.project.analyzer.data.models.scanner.BuildFileInfo
import com.meet.project.analyzer.data.models.scanner.BuildFileType
import com.meet.project.analyzer.data.models.scanner.DependencyInfo
import com.meet.project.analyzer.data.models.scanner.DependencyType
import com.meet.project.analyzer.data.models.scanner.FileType
import com.meet.project.analyzer.data.models.scanner.LibraryInfo
import com.meet.project.analyzer.data.models.scanner.ModuleInfo
import com.meet.project.analyzer.data.models.scanner.ModuleType
import com.meet.project.analyzer.data.models.scanner.ProjectFileInfo
import com.meet.project.analyzer.data.models.scanner.ProjectInfo
import com.meet.project.analyzer.data.models.scanner.ProjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProjectScannerRepositoryImpl1 : ProjectScannerRepository {
    private val TAG: String
        get() {
            return if (!javaClass.isAnonymousClass) {
                val name = javaClass.simpleName
                if (name.length <= 23) name else name.substring(0, 23)// first 23 chars
            } else {
                val name = javaClass.name
                if (name.length <= 23) name else name.substring(
                    name.length - 23,
                    name.length
                )// last 23 chars
            }
        }

    override suspend fun analyzeProject(
        projectPath: String,
        progressCallback: ProgressCallback
    ): ProjectInfo = withContext(Dispatchers.IO) {
        AppLogger.i(TAG, "Starting project analysis for: $projectPath")

        val projectDir = File(projectPath)
        if (!projectDir.exists() || !projectDir.isDirectory) {
            throw IllegalArgumentException("Invalid project directory: $projectPath")
        }

        val projectName = projectDir.name

        progressCallback.updateProgress(0.1f, "Finding build files...")
        val buildFiles = findBuildFiles(projectDir)

        progressCallback.updateProgress(0.3f, "Analyzing modules...")
        val modules = findModules(projectDir, progressCallback)

        progressCallback.updateProgress(0.5f, "Extracting version catalog...")
        val versionCatalog = extractVersionCatalog(projectDir)

        progressCallback.updateProgress(0.7f, "Resolving dependency versions...")
        val resolvedModules = resolveVersions(modules, versionCatalog)

        progressCallback.updateProgress(0.8f, "Scanning project files...")
        val projectFiles = findProjectFiles(projectDir)

        progressCallback.updateProgress(0.9f, "Creating library info...")
        val allDependencies = resolvedModules.flatMap { it.dependencies }
        val libraryInfo = createLibraryInfo(allDependencies, versionCatalog)

        progressCallback.updateProgress(0.95f, "Calculating total size...")
        val totalSizeBytes = StorageUtils.calculateFolderSize(projectDir)

        progressCallback.updateProgress(0.98f, "Extracting metadata...")
        val metadata = extractProjectMetadata(projectDir, buildFiles)

        val projectType = determineProjectType(buildFiles, resolvedModules, metadata)
        val isMultiModule = resolvedModules.size > 1

        progressCallback.updateProgress(1.0f, "Analysis complete")

        AppLogger.i(
            TAG,
            "Project analysis completed. Type: $projectType, Modules: ${resolvedModules.size}, Dependencies: ${allDependencies.size}, Libraries: ${libraryInfo.size}"
        )

        ProjectInfo(
            projectPath = projectPath,
            projectName = projectName,
            projectType = projectType,
            modules = resolvedModules,
            buildFiles = buildFiles,
            dependencies = allDependencies,
            allLibraries = libraryInfo,
            projectFiles = projectFiles,
            totalSize = StorageUtils.formatSize(totalSizeBytes),
            totalSizeBytes = totalSizeBytes,
            gradleVersion = metadata["gradleVersion"],
            kotlinVersion = metadata["kotlinVersion"],
            androidGradlePluginVersion = metadata["agpVersion"],
            targetSdkVersion = metadata["targetSdk"],
            minSdkVersion = metadata["minSdk"],
            isMultiModule = isMultiModule
        )
    }

    private suspend fun extractVersionCatalog(projectDir: File): Map<String, String> =
        withContext(Dispatchers.IO) {
            val versionCatalog = mutableMapOf<String, String>()

            val catalogFile = File(projectDir, "gradle/libs.versions.toml")
            if (catalogFile.exists()) {
                try {
                    val content = catalogFile.readText()
                    AppLogger.d(TAG, "Parsing version catalog: ${catalogFile.absolutePath}")

                    // Parse [versions] section
                    val versionsSection =
                        Regex("\\[versions\\]([\\s\\S]*?)(?=\\n\\s*\\[|$)", RegexOption.MULTILINE)
                            .find(content)?.groupValues?.get(1)

                    versionsSection?.let { section ->
                        // Handle both quoted and unquoted versions
                        val versionRegex = Regex(
                            "^\\s*([a-zA-Z0-9_-]+)\\s*=\\s*[\"']?([^\"'\\n]+?)[\"']?\\s*$",
                            RegexOption.MULTILINE
                        )
                        versionRegex.findAll(section).forEach { match ->
                            val key = match.groupValues[1].trim()
                            val version = match.groupValues[2].trim()
                            versionCatalog[key] = version
                            AppLogger.d(TAG, "Version: $key = $version")
                        }
                    }

                    // Parse [libraries] section - improved parsing
                    val librariesSection =
                        Regex("\\[libraries\\]([\\s\\S]*?)(?=\\n\\s*\\[|$)", RegexOption.MULTILINE)
                            .find(content)?.groupValues?.get(1)

                    librariesSection?.let { section ->
                        // Parse multiline library definitions
                        val libraryBlocks =
                            Regex("([a-zA-Z0-9_-]+)\\s*=\\s*\\{([^}]+)\\}", RegexOption.MULTILINE)
                                .findAll(section)

                        libraryBlocks.forEach { match ->
                            val libKey = match.groupValues[1].trim()
                            val block = match.groupValues[2]

                            // Extract version from block
                            val versionMatch =
                                Regex("version\\s*=\\s*[\"']([^\"']+)[\"']").find(block)
                            val versionRefMatch =
                                Regex("version\\.ref\\s*=\\s*[\"']([^\"']+)[\"']").find(block)

                            when {
                                versionMatch != null -> {
                                    val version = versionMatch.groupValues[1]
                                    versionCatalog["libs.$libKey"] = version
                                }

                                versionRefMatch != null -> {
                                    val versionRef = versionRefMatch.groupValues[1]
                                    val resolvedVersion = versionCatalog[versionRef] ?: versionRef
                                    versionCatalog["libs.$libKey"] = resolvedVersion
                                }
                            }
                        }

                        // Parse single-line library definitions
                        val singleLineRegex =
                            Regex("([a-zA-Z0-9_-]+)\\s*=\\s*[\"']([^\"']+:[^\"']+:[^\"']+)[\"']")
                        singleLineRegex.findAll(section).forEach { match ->
                            val libKey = match.groupValues[1].trim()
                            val dependency = match.groupValues[2]
                            val parts = dependency.split(":")
                            if (parts.size >= 3) {
                                versionCatalog["libs.$libKey"] = parts[2]
                            }
                        }
                    }

                    AppLogger.d(
                        TAG,
                        "Extracted ${versionCatalog.size} entries from version catalog"
                    )
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Error parsing version catalog", e)
                }
            }

            return@withContext versionCatalog
        }

    private suspend fun findModules(
        projectDir: File,
        progressCallback: ProgressCallback?
    ): List<ModuleInfo> = withContext(Dispatchers.IO) {
        AppLogger.d(TAG, "Finding modules in: ${projectDir.absolutePath}")

        val modules = mutableListOf<ModuleInfo>()

        // Find submodules
        val subDirs = projectDir.listFiles()?.filter { dir ->
            dir.isDirectory &&
                    !dir.name.startsWith(".") &&
                    !dir.name.equals("build", true) &&
                    !dir.name.equals("gradle", true) &&
                    !dir.name.equals("src", true) &&
                    (File(dir, "build.gradle.kts").exists() || File(dir, "build.gradle").exists())
        } ?: emptyList()

        subDirs.forEachIndexed { index, moduleDir ->
            progressCallback?.updateProgress(
                0.3f + (index.toFloat() / subDirs.size) * 0.1f,
                "Analyzing module: ${moduleDir.name}"
            )

            val buildFile = File(moduleDir, "build.gradle.kts").takeIf { it.exists() }
                ?: File(moduleDir, "build.gradle").takeIf { it.exists() }

            if (buildFile != null) {
                val sizeBytes = StorageUtils.calculateFolderSize(moduleDir)
                val (sourceFiles, resourceFiles) = countSourceFiles(moduleDir)
                val moduleDependencies = extractModuleDependencies(buildFile, moduleDir.name)

                modules.add(
                    ModuleInfo(
                        name = moduleDir.name,
                        path = moduleDir.absolutePath,
                        type = determineModuleType(moduleDir.name, buildFile),
                        buildFile = buildFile.name,
                        sourceFiles = sourceFiles,
                        resourceFiles = resourceFiles,
                        size = StorageUtils.formatSize(sizeBytes),
                        sizeBytes = sizeBytes,
                        dependencies = moduleDependencies
                    )
                )
            }
        }

        // If no modules found, check if root has build file (single module project)
        if (modules.isEmpty()) {
            val rootBuildFile = File(projectDir, "build.gradle.kts").takeIf { it.exists() }
                ?: File(projectDir, "build.gradle").takeIf { it.exists() }

            if (rootBuildFile != null) {
                val sizeBytes = StorageUtils.calculateFolderSize(projectDir)
                val (sourceFiles, resourceFiles) = countSourceFiles(projectDir)
                val moduleDependencies = extractModuleDependencies(rootBuildFile, "app")

                modules.add(
                    ModuleInfo(
                        name = "app",
                        path = projectDir.absolutePath,
                        type = determineModuleType("app", rootBuildFile),
                        buildFile = rootBuildFile.name,
                        sourceFiles = sourceFiles,
                        resourceFiles = resourceFiles,
                        size = StorageUtils.formatSize(sizeBytes),
                        sizeBytes = sizeBytes,
                        dependencies = moduleDependencies
                    )
                )
            }
        }

        AppLogger.d(TAG, "Found ${modules.size} modules")
        modules
    }

    private fun determineModuleType(name: String, buildFile: File): ModuleType {
        val content = try {
            buildFile.readText().lowercase()
        } catch (e: Exception) {
            ""
        }
        val lowerName = name.lowercase()

        return when {
            content.contains("com.android.application") || content.contains("id(\"com.android.application\")") -> ModuleType.APP
            content.contains("com.android.library") || content.contains("id(\"com.android.library\")") -> ModuleType.LIBRARY
            content.contains("java-library") || content.contains("kotlin-jvm") -> ModuleType.LIBRARY
            lowerName.contains("app") && !lowerName.contains("data") -> ModuleType.APP
            lowerName.contains("core") -> ModuleType.CORE
            lowerName.contains("data") -> ModuleType.DATA
            lowerName.contains("domain") -> ModuleType.DOMAIN
            lowerName.contains("feature") -> ModuleType.FEATURE
            lowerName.contains("presentation") || lowerName.contains("ui") -> ModuleType.PRESENTATION
            else -> ModuleType.UNKNOWN
        }
    }

    private fun countSourceFiles(dir: File): Pair<Int, Int> {
        var sourceFiles = 0
        var resourceFiles = 0

        try {
            dir.walkTopDown()
                .filter { !it.absolutePath.contains("/build/") && !it.absolutePath.contains("/.gradle/") }
                .forEach { file ->
                    if (file.isFile) {
                        when (file.extension.lowercase()) {
                            "kt", "java", "scala", "groovy" -> sourceFiles++
                            "xml", "json", "properties", "txt", "md", "png", "jpg", "jpeg", "svg" -> resourceFiles++
                        }
                    }
                }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error counting source files in ${dir.absolutePath}", e)
        }

        return sourceFiles to resourceFiles
    }

    private suspend fun resolveVersions(
        modules: List<ModuleInfo>,
        versionCatalog: Map<String, String>
    ): List<ModuleInfo> {
        return modules.map { module ->
            val resolvedDependencies = module.dependencies.map { dep ->
                val resolvedVersion = when {
                    dep.name.startsWith("libs.") -> {
                        val catalogKey = dep.name
                        versionCatalog[catalogKey] ?: dep.version
                    }

                    dep.version == "catalog" -> {
                        versionCatalog[dep.name] ?: "catalog"
                    }

                    dep.version.startsWith("\$") -> {
                        // Handle version variables like $kotlin_version
                        val varName = dep.version.removePrefix("\$").removeSurrounding("{", "}")
                        versionCatalog[varName] ?: dep.version
                    }

                    else -> dep.version
                }

                dep.copy(version = resolvedVersion)
            }

            module.copy(dependencies = resolvedDependencies)
        }
    }

    // Improved dependency extraction with better regex patterns
    private fun extractModuleDependencies(
        buildFile: File,
        moduleName: String
    ): List<DependencyInfo> {
        val dependencies = mutableListOf<DependencyInfo>()

        try {
            val content = buildFile.readText()
            AppLogger.d(TAG, "Extracting dependencies from: ${buildFile.absolutePath}")

            // Kotlin DSL patterns
            val kotlinDslRegex = Regex(
                """(implementation|api|compileOnly|runtimeOnly|testImplementation|androidTestImplementation|kapt|ksp|debugImplementation|releaseImplementation)\s*\(\s*["']([^"']+)["']\s*\)"""
            )

            // Groovy DSL patterns
            val groovyDslRegex = Regex(
                """(implementation|api|compileOnly|runtimeOnly|testImplementation|androidTestImplementation|kapt|ksp|debugImplementation|releaseImplementation)\s+["']([^"']+)["']"""
            )

            // Version catalog references
            val versionCatalogRegex = Regex(
                """(implementation|api|compileOnly|runtimeOnly|testImplementation|androidTestImplementation|kapt|ksp|debugImplementation|releaseImplementation)\s*\(\s*libs\.([a-zA-Z0-9._-]+)\s*\)"""
            )

            // Platform dependencies
            val platformRegex = Regex(
                """(implementation|api)\s*\(\s*platform\s*\(\s*["']([^"']+)["']\s*\)\s*\)"""
            )

            // BOM (Bill of Materials) dependencies
            val bomRegex = Regex(
                """(implementation|api)\s*\(\s*platform\s*\(\s*libs\.([a-zA-Z0-9._-]+)\s*\)\s*\)"""
            )

            // Dependencies block parsing
            val dependenciesBlock = Regex(
                """dependencies\s*\{([^{}]*(?:\{[^{}]*\}[^{}]*)*)\}""",
                RegexOption.DOT_MATCHES_ALL
            ).find(content)?.groupValues?.get(1)

            dependenciesBlock?.let { block ->
                // Process each pattern
                kotlinDslRegex.findAll(block).forEach { match ->
                    addDependency(
                        dependencies,
                        match.groupValues[1],
                        match.groupValues[2],
                        moduleName
                    )
                }

                groovyDslRegex.findAll(block).forEach { match ->
                    addDependency(
                        dependencies,
                        match.groupValues[1],
                        match.groupValues[2],
                        moduleName
                    )
                }

                platformRegex.findAll(block).forEach { match ->
                    addDependency(
                        dependencies,
                        match.groupValues[1],
                        match.groupValues[2],
                        moduleName,
                        isPlatform = true
                    )
                }

                bomRegex.findAll(block).forEach { match ->
                    val scope = match.groupValues[1]
                    val catalogRef = match.groupValues[2]
                    dependencies.add(
                        DependencyInfo(
                            name = "libs.$catalogRef",
                            version = "bom",
                            type = mapScopeToType(scope),
                            scope = scope,
                            module = moduleName
                        )
                    )
                }

                versionCatalogRegex.findAll(block).forEach { match ->
                    val scope = match.groupValues[1]
                    val catalogRef = match.groupValues[2]
                    dependencies.add(
                        DependencyInfo(
                            name = "libs.$catalogRef",
                            version = "catalog",
                            type = mapScopeToType(scope),
                            scope = scope,
                            module = moduleName
                        )
                    )
                }
            }

            AppLogger.d(TAG, "Found ${dependencies.size} dependencies in module: $moduleName")

        } catch (e: Exception) {
            AppLogger.e(TAG, "Error extracting dependencies from: ${buildFile.absolutePath}", e)
        }

        return dependencies
    }

    private fun mapScopeToType(scope: String): DependencyType {
        return when (scope.lowercase()) {
            "implementation" -> DependencyType.IMPLEMENTATION
            "api" -> DependencyType.API
            "compileonly" -> DependencyType.COMPILE_ONLY
            "runtimeonly" -> DependencyType.RUNTIME_ONLY
            "testimplementation" -> DependencyType.TEST_IMPLEMENTATION
            "androidtestimplementation" -> DependencyType.ANDROID_TEST_IMPLEMENTATION
            "kapt" -> DependencyType.KAPT
            "ksp" -> DependencyType.KSP
            "debugimplementation", "releaseimplementation" -> DependencyType.IMPLEMENTATION
            else -> DependencyType.IMPLEMENTATION
        }
    }

    private fun addDependency(
        dependencies: MutableList<DependencyInfo>,
        scope: String,
        dependencyString: String,
        moduleName: String,
        isPlatform: Boolean = false
    ) {
        try {
            val parts = dependencyString.split(":")
            if (parts.size >= 2) {
                val name = "${parts[0]}:${parts[1]}"
                val version = when {
                    isPlatform -> "platform"
                    parts.size >= 3 -> parts[2]
                    else -> "unspecified"
                }

                dependencies.add(
                    DependencyInfo(
                        name = name,
                        version = version,
                        type = mapScopeToType(scope),
                        scope = scope,
                        module = moduleName
                    )
                )

                AppLogger.d(TAG, "Added dependency: $name:$version ($scope) in $moduleName")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error parsing dependency: $dependencyString", e)
        }
    }

    private suspend fun createLibraryInfo(
        allDependencies: List<DependencyInfo>,
        versionCatalog: Map<String, String>
    ): List<LibraryInfo> {
        AppLogger.d(TAG, "Creating library info from ${allDependencies.size} dependencies")

        val libraryGroups = allDependencies
            .filter { !it.name.startsWith("libs.") && it.name.contains(":") }
            .groupBy { it.name }

        val libraries = libraryGroups.map { (libName, deps) ->
            val parts = libName.split(":")
            val group = parts.getOrNull(0) ?: ""
            val artifact = parts.getOrNull(1) ?: libName

            val allVersions = deps.map { it.version }
                .distinct()
                .filter { it != "unspecified" && it != "catalog" && it != "platform" && it != "bom" }
                .sorted()

            val usedInModules = deps.map { it.module }.distinct()
            val mostCommonType = deps.groupBy { it.type }.maxByOrNull { it.value.size }?.key

            val hasVersionConflict = allVersions.size > 1
            val latestVersion = allVersions.lastOrNull() ?: "unknown"

            LibraryInfo(
                name = libName,
                group = group,
                artifact = artifact,
                allVersions = allVersions,
                usedInModules = usedInModules,
                isUsed = true,
                dependencyType = mostCommonType,
                latestVersion = latestVersion,
                hasVersionConflict = hasVersionConflict
            )
        }.sortedBy { it.name }

        // Add version catalog libraries
        val catalogLibraries = allDependencies
            .filter { it.name.startsWith("libs.") }
            .map { dep ->
                val catalogKey = dep.name
                val resolvedVersion = versionCatalog[catalogKey] ?: "unknown"
                val displayName = catalogKey.removePrefix("libs.").replace("_", "-")

                LibraryInfo(
                    name = displayName,
                    group = "version-catalog",
                    artifact = displayName,
                    allVersions = listOf(resolvedVersion),
                    usedInModules = listOf(dep.module),
                    isUsed = true,
                    dependencyType = dep.type,
                    latestVersion = resolvedVersion,
                    hasVersionConflict = false
                )
            }

        val allLibraries =
            (libraries + catalogLibraries).distinctBy { it.name }.sortedBy { it.name }

        AppLogger.d(TAG, "Created ${allLibraries.size} library entries")
        return allLibraries
    }

    private suspend fun extractProjectMetadata(
        projectDir: File,
        buildFiles: List<BuildFileInfo>
    ): Map<String, String> = withContext(Dispatchers.IO) {
        AppLogger.d(TAG, "Extracting project metadata")

        val metadata = mutableMapOf<String, String>()

        // Extract from gradle.properties
        val gradleProperties = File(projectDir, "gradle.properties")
        if (gradleProperties.exists()) {
            try {
                gradleProperties.readLines().forEach { line ->
                    if (line.contains("=") && !line.trim().startsWith("#")) {
                        val parts = line.split("=", limit = 2)
                        if (parts.size == 2) {
                            val key = parts[0].trim()
                            val value = parts[1].trim()
                            when (key) {
                                "kotlin.version", "kotlin_version" -> metadata["kotlinVersion"] =
                                    value

                                "android.compileSdk", "compileSdk" -> metadata["targetSdk"] = value
                                "android.minSdk", "minSdk" -> metadata["minSdk"] = value
                                "agp.version", "android_gradle_plugin_version" -> metadata["agpVersion"] =
                                    value
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error reading gradle.properties", e)
            }
        }

        // Extract from build files
        buildFiles.forEach { buildFile ->
            buildFile.content?.let { content ->
                // Extract AGP version - multiple patterns
                listOf(
                    Regex("""com\.android\.tools\.build:gradle['"]\s*:\s*['"]([^'"]+)['"]"""),
                    Regex("""id\s*\(\s*['"]com\.android\.application['"]\s*\)\s*version\s*['"]([^'"]+)['"]"""),
                    Regex("""id\s*\(\s*['"]com\.android\.library['"]\s*\)\s*version\s*['"]([^'"]+)['"]""")
                ).forEach { regex ->
                    regex.find(content)?.groupValues?.get(1)?.let {
                        metadata["agpVersion"] = it
                    }
                }

                // Extract Kotlin version - multiple patterns
                listOf(
                    Regex("""kotlin\s*\(\s*['"]jvm['"].*version\s*=?\s*['"]([^'"]+)['"]"""),
                    Regex("""id\s*\(\s*['"]org\.jetbrains\.kotlin\.android['"]\s*\)\s*version\s*['"]([^'"]+)['"]"""),
                    Regex("""kotlin_version\s*=\s*['"]([^'"]+)['"]""")
                ).forEach { regex ->
                    regex.find(content)?.groupValues?.get(1)?.let {
                        metadata["kotlinVersion"] = it
                    }
                }

                // Extract SDK versions
                listOf(
                    Regex("""targetSdk\s*=?\s*(\d+)"""),
                    Regex("""targetSdkVersion\s*=?\s*(\d+)""")
                ).forEach { regex ->
                    regex.find(content)?.groupValues?.get(1)?.let {
                        metadata["targetSdk"] = it
                    }
                }

                listOf(
                    Regex("""minSdk\s*=?\s*(\d+)"""),
                    Regex("""minSdkVersion\s*=?\s*(\d+)""")
                ).forEach { regex ->
                    regex.find(content)?.groupValues?.get(1)?.let {
                        metadata["minSdk"] = it
                    }
                }
            }
        }

        // Extract Gradle version from wrapper
        val gradleWrapperProperties = File(projectDir, "gradle/wrapper/gradle-wrapper.properties")
        if (gradleWrapperProperties.exists()) {
            try {
                gradleWrapperProperties.readLines().forEach { line ->
                    if (line.contains("distributionUrl")) {
                        Regex("""gradle-([0-9.]+)""").find(line)?.groupValues?.get(1)?.let {
                            metadata["gradleVersion"] = it
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error reading gradle wrapper properties", e)
            }
        }

        AppLogger.d(TAG, "Extracted metadata: $metadata")
        metadata
    }

    private fun determineProjectType(
        buildFiles: List<BuildFileInfo>,
        modules: List<ModuleInfo>,
        metadata: Map<String, String>
    ): ProjectType {
        val allContent = buildFiles.mapNotNull { it.content }.joinToString(" ").lowercase()

        // Check for specific project types based on plugins and dependencies
        return when {
            // Android project detection
            allContent.contains("com.android.application") ||
                    allContent.contains("com.android.library") ||
                    allContent.contains("id(\"com.android.application\")") ||
                    allContent.contains("id(\"com.android.library\")") -> ProjectType.ANDROID

            // Kotlin Multiplatform detection
            allContent.contains("kotlin(\"multiplatform\")") ||
                    allContent.contains("kotlin-multiplatform") ||
                    allContent.contains("id(\"org.jetbrains.kotlin.multiplatform\")") -> ProjectType.KOTLIN_MULTIPLATFORM

            // Compose Multiplatform detection
            allContent.contains("org.jetbrains.compose") ||
                    allContent.contains("compose-multiplatform") ||
                    allContent.contains("id(\"org.jetbrains.compose\")") -> ProjectType.COMPOSE_MULTIPLATFORM

            // Spring Boot detection
            allContent.contains("spring-boot") ||
                    allContent.contains("org.springframework.boot") ||
                    allContent.contains("id(\"org.springframework.boot\")") -> ProjectType.SPRING_BOOT

            // Java project detection
            allContent.contains("java-library") ||
                    allContent.contains("java-application") ||
                    allContent.contains("id(\"java\")") ||
                    allContent.contains("id(\"java-library\")") -> ProjectType.JAVA

            // Generic Kotlin project
            allContent.contains("kotlin") ||
                    allContent.contains("org.jetbrains.kotlin") -> ProjectType.KOTLIN_MULTIPLATFORM

            else -> ProjectType.UNKNOWN
        }
    }

    private suspend fun findBuildFiles(projectDir: File): List<BuildFileInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG, "Finding build files in: ${projectDir.absolutePath}")

            val buildFiles = mutableListOf<BuildFileInfo>()
            val buildFileNames = mapOf(
                "build.gradle.kts" to BuildFileType.BUILD_GRADLE_KTS,
                "build.gradle" to BuildFileType.BUILD_GRADLE,
                "settings.gradle.kts" to BuildFileType.SETTINGS_GRADLE_KTS,
                "settings.gradle" to BuildFileType.SETTINGS_GRADLE,
                "gradle.properties" to BuildFileType.GRADLE_PROPERTIES,
                "local.properties" to BuildFileType.LOCAL_PROPERTIES
            )

            // Find root build files
            buildFileNames.forEach { (fileName, type) ->
                val file = File(projectDir, fileName)
                if (file.exists()) {
                    val sizeBytes = file.length()
                    buildFiles.add(
                        BuildFileInfo(
                            name = fileName,
                            path = file.absolutePath,
                            type = type,
                            size = StorageUtils.formatSize(sizeBytes),
                            sizeBytes = sizeBytes,
                            content = try {
                                file.readText()
                            } catch (e: Exception) {
                                null
                            }
                        )
                    )
                }
            }

            // Find version catalogs
            val gradleDir = File(projectDir, "gradle")
            if (gradleDir.exists()) {
                val versionCatalog = File(gradleDir, "libs.versions.toml")
                if (versionCatalog.exists()) {
                    val sizeBytes = versionCatalog.length()
                    buildFiles.add(
                        BuildFileInfo(
                            name = "libs.versions.toml",
                            path = versionCatalog.absolutePath,
                            type = BuildFileType.VERSION_CATALOG,
                            size = StorageUtils.formatSize(sizeBytes),
                            sizeBytes = sizeBytes,
                            content = try {
                                versionCatalog.readText()
                            } catch (e: Exception) {
                                null
                            }
                        )
                    )
                }
            }

            // Find module build files
            projectDir.listFiles()?.filter { it.isDirectory && !it.name.startsWith(".") }
                ?.forEach { moduleDir ->
                    buildFileNames.keys.forEach { fileName ->
                        val file = File(moduleDir, fileName)
                        if (file.exists()) {
                            val sizeBytes = file.length()
                            buildFiles.add(
                                BuildFileInfo(
                                    name = "${moduleDir.name}/$fileName",
                                    path = file.absolutePath,
                                    type = buildFileNames[fileName]!!,
                                    size = StorageUtils.formatSize(sizeBytes),
                                    sizeBytes = sizeBytes,
                                    content = try {
                                        file.readText()
                                    } catch (e: Exception) {
                                        null
                                    }
                                )
                            )
                        }
                    }
                }

            AppLogger.d(TAG, "Found ${buildFiles.size} build files")
            buildFiles
        }

    private suspend fun findProjectFiles(projectDir: File): List<ProjectFileInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG, "Finding project files in: ${projectDir.absolutePath}")

            val projectFiles = mutableListOf<ProjectFileInfo>()

            try {
                projectDir.walkTopDown()
                    .filter { file ->
                        file.isFile &&
                                !file.absolutePath.contains("/.gradle/") &&
                                !file.absolutePath.contains("\\.gradle\\") &&
                                !file.absolutePath.contains("/build/") &&
                                !file.absolutePath.contains("\\build\\") &&
                                !file.name.startsWith(".") &&
                                !file.path.contains("/.") && // Exclude hidden folders
                                file.length() < 10 * 1024 * 1024 // Skip files larger than 10MB
                    }
                    .take(2000) // Limit to first 2000 files
                    .forEach { file ->
                        try {
                            val relativePath = file.relativeTo(projectDir).path
                            val fileType = determineFileType(file)
                            val sizeBytes = file.length()
                            val isReadable = isTextFile(file)

                            projectFiles.add(
                                ProjectFileInfo(
                                    name = file.name,
                                    path = file.absolutePath,
                                    relativePath = relativePath,
                                    type = fileType,
                                    size = StorageUtils.formatSize(sizeBytes),
                                    sizeBytes = sizeBytes,
                                    extension = file.extension.lowercase(),
                                    content = if (isReadable && sizeBytes < 100 * 1024) {
                                        try {
                                            file.readText().take(10000)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    } else null,
                                    isReadable = isReadable
                                )
                            )
                        } catch (e: Exception) {
                            AppLogger.e(TAG, "Error processing file: ${file.absolutePath}")
                        }
                    }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error walking project directory", e)
            }

            AppLogger.d(TAG, "Found ${projectFiles.size} project files")
            projectFiles.sortedBy { it.relativePath }
        }

    private fun determineFileType(file: File): FileType {
        val extension = file.extension.lowercase()
        val path = file.absolutePath.lowercase()

        return when {
            extension in listOf("kt", "kts") -> FileType.SOURCE_KOTLIN
            extension == "java" -> FileType.SOURCE_JAVA
            file.name in listOf(
                "build.gradle",
                "build.gradle.kts",
                "settings.gradle",
                "settings.gradle.kts"
            ) -> FileType.BUILD_SCRIPT

            extension == "properties" -> FileType.PROPERTIES
            extension == "json" -> FileType.JSON
            extension == "xml" && path.contains("androidmanifest") -> FileType.MANIFEST
            extension == "xml" && path.contains("layout") -> FileType.LAYOUT
            extension == "xml" && path.contains("values") -> FileType.VALUES
            extension == "xml" -> FileType.XML
            extension == "md" -> FileType.MARKDOWN
            extension == "txt" -> FileType.TEXT
            path.contains("drawable") -> FileType.DRAWABLE
            path.contains("assets") -> FileType.ASSETS
            path.contains("res/") -> FileType.RESOURCE
            extension in listOf("toml", "yaml", "yml", "conf", "config") -> FileType.CONFIGURATION
            else -> FileType.OTHER
        }
    }

    private fun isTextFile(file: File): Boolean {
        val textExtensions = setOf(
            "kt", "kts", "java", "xml", "json", "properties", "toml", "yaml", "yml",
            "txt", "md", "gradle", "gitignore", "pro", "conf", "config", "sh", "bat"
        )
        return file.extension.lowercase() in textExtensions ||
                file.name.lowercase() in setOf("dockerfile", "makefile", "readme")
    }

}