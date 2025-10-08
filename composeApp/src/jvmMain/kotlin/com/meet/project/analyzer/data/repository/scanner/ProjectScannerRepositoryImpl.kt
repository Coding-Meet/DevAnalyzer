package com.meet.project.analyzer.data.repository.scanner

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.meet.project.analyzer.core.utility.AppLogger
import com.meet.project.analyzer.core.utility.Utils
import com.meet.project.analyzer.data.models.GradleLibraryInfo
import com.meet.project.analyzer.data.models.GradleModulesInfo
import com.meet.project.analyzer.data.models.scanner.BuildFileType
import com.meet.project.analyzer.data.models.scanner.Bundle
import com.meet.project.analyzer.data.models.scanner.Dependency
import com.meet.project.analyzer.data.models.scanner.FileType
import com.meet.project.analyzer.data.models.scanner.GradleWrapperPropertiesFileInfo
import com.meet.project.analyzer.data.models.scanner.Library
import com.meet.project.analyzer.data.models.scanner.Plugin
import com.meet.project.analyzer.data.models.scanner.ProjectFileInfo
import com.meet.project.analyzer.data.models.scanner.ProjectInfo
import com.meet.project.analyzer.data.models.scanner.ProjectOverviewInfo
import com.meet.project.analyzer.data.models.scanner.PropertiesFileInfo
import com.meet.project.analyzer.data.models.scanner.PropertiesFileType
import com.meet.project.analyzer.data.models.scanner.RootModuleBuildFileInfo
import com.meet.project.analyzer.data.models.scanner.SettingsGradleFileInfo
import com.meet.project.analyzer.data.models.scanner.SettingsGradleFileType
import com.meet.project.analyzer.data.models.scanner.SubModuleBuildFileInfo
import com.meet.project.analyzer.data.models.scanner.Version
import com.meet.project.analyzer.data.models.scanner.VersionCatalog
import com.meet.project.analyzer.data.models.scanner.VersionCatalogFileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class ProjectScannerRepositoryImpl : ProjectScannerRepository {
    private val TAG: String
        get() {
            return if (!javaClass.isAnonymousClass) {
                val name = javaClass.simpleName
                if (name.length <= 23) name else name.substring(0, 23)// first 23 chars
            } else {
                val name = javaClass.name
                if (name.length <= 23) name else name.substring(
                    name.length - 23, name.length
                )// last 23 chars
            }
        }

    override suspend fun analyzeProject(
        projectPath: String, updateProgress: (progress: Float, status: String) -> Unit
    ): ProjectInfo = withContext(Dispatchers.IO) {
        AppLogger.i(TAG) { "Starting project analysis for: $projectPath" }

        val projectDir = File(projectPath)

        updateProgress(0.1f, "Finding build files...")
        val rootModuleBuildFileInfo =
            findRootModuleBuildFiles(projectDir = projectDir)

        val subModuleBuildFileInfos =
            findSubModuleBuildFiles(projectDir = projectDir)

        val settingsGradleFileInfo =
            findSettingsGradleFiles(projectDir = projectDir)

        val propertiesFileInfo =
            findPropertiesFiles(projectDir = projectDir)

        val gradleWrapperPropertiesFileInfo =
            findGradleWrapperProFile(projectDir = projectDir)

        val versionCatalogFileInfo =
            findVersionCatalogFile(projectDir = projectDir)

        updateProgress(0.3f, "Analyzing modules...")
        val versionCatalog =
            findVersionCatalog(versionCatalogFileInfo = versionCatalogFileInfo)

        val projectOverviewInfo =
            findProjectOverviewInfo(
                projectDir = projectDir,
                settingsGradleFileInfo = settingsGradleFileInfo,
                gradleWrapperPropertiesFileInfo = gradleWrapperPropertiesFileInfo,
                versionCatalog = versionCatalog,
                subModuleBuildFileInfos = subModuleBuildFileInfos,
                rootModuleBuildFileInfo = rootModuleBuildFileInfo,
                isMultiModule = subModuleBuildFileInfos.size > 1
            )

        updateProgress(0.5f, "Analyzing plugins...")
        val gradleModulesInfo = Utils.getGradleModulesInfo()
        val plugins =
            findPlugin(
                rootModuleBuildFileInfo = rootModuleBuildFileInfo,
                subModuleBuildFileInfos = subModuleBuildFileInfos,
                versionCatalog = versionCatalog,
                gradleModulesInfo = gradleModulesInfo,
            )

        updateProgress(0.7f, "Analyzing dependencies...")
        val dependencies =
            findDependencies(
                rootModuleBuildFileInfo = rootModuleBuildFileInfo,
                subModuleBuildFileInfos = subModuleBuildFileInfos,
                versionCatalog = versionCatalog,
                gradleModulesInfo = gradleModulesInfo
            )
        val subModuleWithDependency =
            addDependencyEachModule(
                subModuleBuildFileInfos = subModuleBuildFileInfos,
                plugins = plugins,
                dependencies = dependencies
            )
        updateProgress(0.8f, "Building project info...")

        val projectFiles = findProjectFiles(projectDir)

        val projectInfo = ProjectInfo(
            projectOverviewInfo = projectOverviewInfo,
            plugins = plugins,
            dependencies = dependencies,
            rootModuleBuildFileInfo = rootModuleBuildFileInfo?.copy(
                plugins = plugins.filter { plugin ->
                    plugin.module == rootModuleBuildFileInfo.moduleName
                }
            ),
            subModuleBuildFileInfos = subModuleWithDependency,
            settingsGradleFileInfo = settingsGradleFileInfo,
            propertiesFileInfo = propertiesFileInfo,
            gradleWrapperPropertiesFileInfo = gradleWrapperPropertiesFileInfo,
            versionCatalogFileInfo = versionCatalogFileInfo,
            versionCatalog = versionCatalog,
            projectFiles = projectFiles,
        )
        updateProgress(1f, "Analysis complete")
        projectInfo
    }

    private fun addDependencyEachModule(
        subModuleBuildFileInfos: List<SubModuleBuildFileInfo>,
        plugins: List<Plugin>,
        dependencies: List<Dependency>
    ) = subModuleBuildFileInfos.map { subModuleBuildFileInfo ->
        subModuleBuildFileInfo.copy(
            plugins = plugins.filter { plugin ->
                plugin.module == subModuleBuildFileInfo.moduleName
            },
            dependencies = dependencies.filter { dependency ->
                dependency.module == subModuleBuildFileInfo.moduleName
            }
        )
    }

    private fun findAvailableVersionsInGradleCache(
        groupId: String?,
        artifactId: String?,
        gradleModulesInfo: GradleModulesInfo?
    ): GradleLibraryInfo? {
        AppLogger.d(TAG) { "Finding available versions for: $groupId:$artifactId" }
        if (gradleModulesInfo == null) return null
        val lib = gradleModulesInfo.libraries.find {
            it.groupId == groupId && it.artifactId == artifactId
        }
        AppLogger.d(TAG) { "Found $groupId:$artifactId ${lib?.versions?.size} available versions" }
        lib?.versions?.forEach {
            AppLogger.d(TAG) { "Available version: ${it.version} Size: ${it.sizeReadable}" }
        }
        return lib
    }

    private fun findDependencies(
        rootModuleBuildFileInfo: RootModuleBuildFileInfo?,
        subModuleBuildFileInfos: List<SubModuleBuildFileInfo>,
        versionCatalog: VersionCatalog?,
        gradleModulesInfo: GradleModulesInfo?,
    ): List<Dependency> {


        AppLogger.d(TAG) { "Finding dependencies" }

        // normal dependencies → implementation("group:artifact:version") , implementation "group:artifact:version" , implementation 'group:artifact:version'
        val normalDepRegex = Regex(
            """(implementation|api|ksp|kapt|compileOnly|runtimeOnly|testImplementation|androidTestImplementation)\s*\(?["']([^"':]+):([^"':]+):([^"']+)["']\)?"""
        )
        // alias style → implementation(libs.xyz.abc)
        val aliasDepRegex =
            Regex("""(implementation|api|ksp|kapt|compileOnly|runtimeOnly|testImplementation|androidTestImplementation)\((libs\.[^)]+)\)""")

        // bundle dependencies → implementation(libs.bundles.xxx)
        val bundleDepRegex =
            Regex("""(implementation|api|ksp|kapt|compileOnly|runtimeOnly|testImplementation|androidTestImplementation)\((libs\.bundles\.[^)]+)\)""")

        val dependencies = arrayListOf<Dependency>()

        fun findDependencies(mainContent: String, module: String) {
            mainContent.lineSequence().forEach inner@{ rawLine ->
                val content = rawLine.trim()

                // Skip comments
                if (content.startsWith("//") || content.startsWith("/*") || content.startsWith("*")) {
                    return@inner
                }

                // Normal style dependencies
                // ex implementation("com.google.android.material:material:1.11.0")
                normalDepRegex.findAll(content).forEach { match ->

                    val type = match.groupValues[1] // implementation
                    val group = match.groupValues[2] // com.google.android.material
                    val artifact = match.groupValues[3] // material
                    val version = match.groupValues[4] // 1.11.0

                    val availableVersions = findAvailableVersionsInGradleCache(
                        groupId = group,
                        artifactId = artifact,
                        gradleModulesInfo = gradleModulesInfo
                    )
                    val normalDependency = Dependency(
                        versionName = artifact,
                        name = artifact,
                        id = "$group:$artifact",
                        group = group,
                        version = version,
                        configuration = type,
                        module = module,
                        availableVersions = availableVersions,
                        isAvailable = availableVersions?.versions?.any {
                            it.version == version
                        } == true
                    )
                    AppLogger.d(TAG) { "Found normalDependency: $normalDependency" }
                    dependencies.add(
                        normalDependency
                    )
                }

                // Version catalog alias style
                // ex implementation(libs.lifecycle.runtime.ktx)
                aliasDepRegex.findAll(content).forEach { match ->

                    val aliasPath =
                        match.groupValues[2].removePrefix("libs.") // ex lifecycle.runtime.ktx
                    if (!aliasPath.contains("bundles")) {
                        val alias = aliasPath.replace('.', '-') // ex lifecycle-runtime-ktx

                        val lib = versionCatalog?.libraries?.find {
                            it.name == alias
                        }

                        if (lib != null) {
                            val availableVersions = findAvailableVersionsInGradleCache(
                                groupId = lib.group,
                                artifactId = lib.libName,
                                gradleModulesInfo = gradleModulesInfo
                            )
                            val versionCatalogDependency = Dependency(
                                versionName = lib.name,
                                name = lib.libName ?: lib.name,
                                id = lib.id,
                                group = lib.group ?: "",
                                version = lib.version,
                                configuration = match.groupValues[1],
                                module = module,
                                availableVersions = availableVersions,
                                isAvailable = availableVersions?.versions?.any {
                                    it.version == lib.version
                                } == true
                            )
                            AppLogger.d(TAG) { "Found versionCatalogDependency: $versionCatalogDependency" }
                            dependencies.add(
                                versionCatalogDependency
                            )
                        }
                    }
                }

                // --- Bundle style (multiple libs) ---
                // ex implementation(libs.bundles.koin.common)
                bundleDepRegex.findAll(content).forEach { match ->
                    val bundlePath =
                        match.groupValues[2].removePrefix("libs.bundles.") // ex koin.common
                    val bundleKey = bundlePath.replace('.', '-') // ex koin-common
                    val bundle = versionCatalog?.bundles?.find {
                        it.name == bundleKey
                    }
                    bundle?.artifacts?.forEach { artifact ->
                        val library = versionCatalog.libraries.find { lib ->
                            lib.name == artifact
                        }
                        if (library != null) {
                            val id = library.id.split(":")
                            val groupId = id[0]
                            val artifactId = id[1]
                            val availableVersions = findAvailableVersionsInGradleCache(
                                groupId = groupId,
                                artifactId = artifactId,
                                gradleModulesInfo = gradleModulesInfo
                            )
                            val bundleDependency = Dependency(
                                versionName = library.name,
                                name = library.libName ?: library.name,
                                id = library.id,
                                group = library.group ?: "",
                                version = library.version,
                                configuration = match.groupValues[1],
                                module = module,
                                availableVersions = availableVersions,
                                isAvailable = availableVersions?.versions?.any {
                                    it.version == library.version
                                } == true
                            )
                            AppLogger.d(TAG) { "Found bundleDependency: $bundleDependency" }
                            dependencies.add(bundleDependency)
                        }
                    }
                }
            }
        }


        // Root module
        rootModuleBuildFileInfo?.let {
            findDependencies(
                mainContent = it.content,
                module = it.moduleName
            )
        }

        // Submodules
        subModuleBuildFileInfos.forEach { subModule ->
            findDependencies(
                mainContent = subModule.content,
                module = subModule.moduleName
            )
        }

        AppLogger.d(TAG) { "Found ${dependencies.size} dependencies" }
        dependencies.forEach {
            AppLogger.i(TAG) { "Dependency name: ${it.name} id: ${it.id} version: ${it.version} module: ${it.module} type: ${it.configuration} isAvailable: ${it.isAvailable} availableVersions: ${it.availableVersions}" }
        }

        return dependencies
    }

    private fun findPlugin(
        rootModuleBuildFileInfo: RootModuleBuildFileInfo?,
        subModuleBuildFileInfos: List<SubModuleBuildFileInfo>,
        versionCatalog: VersionCatalog? = null,
        gradleModulesInfo: GradleModulesInfo?,
    ): List<Plugin> {
        AppLogger.d(TAG) { "Finding plugins" }

        val plugins = arrayListOf<Plugin>()

        // Regex list
        val regexList = listOf(
            // Kotlin DSL (id("x") version "y")
            Regex("""id\("([^"]+)"\)\s+version\s+"([^"]+)"""),
            // Groovy DSL (id 'x' version 'y')
            Regex("""id\s+['"]([^'"]+)['"]\s+version\s+['"]([^'"]+)['"]"""),
            // Classpath (classpath "group:artifact:version")
            Regex("""classpath\s+['"]([^:'"]+:[^:'"]+):([^'"]+)['"]"""),
            // Alias (alias(libs.plugins.xxx))
            Regex("""alias\(libs\.plugins\.([^)]+)\)""")
        )

        fun extractPlugins(content: String, module: String) {
            regexList.forEach { regex ->
                regex.findAll(content).forEach { match ->
                    when (regex.pattern) {
                        // Case: id(...) version "..."
                        regexList[0].pattern, regexList[1].pattern -> {

                            // [id("com.google.gms.google-services") version "4.4.2, com.google.gms.google-services, 4.4.2]
                            val id =
                                match.groupValues[1]       // ex: com.google.gms.google-services
                            val groupId = id.substringBeforeLast(".")  // ex com.google.gms
                            val artifactId = id.substringAfterLast('.') // ex google-services
                            val version = match.groupValues[2]  // ex: 8.3.2

                            val availableVersions = findAvailableVersionsInGradleCache(
                                groupId = groupId,
                                artifactId = artifactId,
                                gradleModulesInfo = gradleModulesInfo
                            )
                            val normalPlugin = Plugin(
                                id = id,
                                name = artifactId,
                                group = groupId,
                                version = version,
                                module = module,
                                configuration = "normal",
                                availableVersions = availableVersions,
                                isAvailable = availableVersions?.versions?.any {
                                    it.version == version
                                } == true
                            )
                            AppLogger.d(TAG) { "Found normalPlugin: $normalPlugin" }
                            plugins.add(
                                normalPlugin
                            )
                        }

                        // Library: com.android.tools.build:gradle groupId: com.android.tools.build artifactId: gradle versions: [7.2.2, 8.0.0, 8.1.4, 8.10.0, 8.10.1, 8.11.1, 8.12.0, 8.12.2, 8.12.3, 8.13.0, 8.5.2, 8.7.3, 8.8.0] totalSize: 142.89 MB totalSizeBytes: 149827947
                        // Case: classpath 'com.android.tools.build:gradle:8.0.2'
                        regexList[2].pattern -> {
                            val id = match.groupValues[1] // ex: com.android.tools.build:gradle
                            val version = match.groupValues[2] // ex: 8.3.2
                            val groupId = id.substringBeforeLast(":")  // ex com.android.tools.build
                            val artifactId = id.substringAfterLast(':') // ex gradle

                            val availableVersions = findAvailableVersionsInGradleCache(
                                groupId = groupId,
                                artifactId = artifactId,
                                gradleModulesInfo = gradleModulesInfo
                            )
                            val classPathPlugin = Plugin(
                                group = groupId,
                                name = artifactId,
                                id = id,
                                version = version,
                                module = module,
                                configuration = "classpath",
                                availableVersions = availableVersions,
                                isAvailable = availableVersions?.versions?.any {
                                    it.version == version
                                } == true
                            )
                            AppLogger.d(TAG) { "Found classPathPlugin: $classPathPlugin" }
                            plugins.add(
                                classPathPlugin
                            )
                        }

                        // Case: alias(libs.plugins.xxx)
                        // Library: org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin groupId: org.jetbrains.kotlin.plugin.serialization artifactId: org.jetbrains.kotlin.plugin.serialization.gradle.plugin versions: [2.1.21, 2.2.10, 2.2.20] totalSize: 4.40 KB totalSizeBytes: 4506
                        // alias(libs.plugins.kotlinSerialization) apply false
                        regexList[3].pattern -> {
                            val id = match.groupValues[1] // ex: libs.plugins.kotlinSerialization

                            val catalogPlugin = versionCatalog?.plugins?.find {
                                it.name == id.substringAfter("libs.plugins.").replace(".", "-")
                            }
                            if (catalogPlugin != null) {
                                val mainId = catalogPlugin.id + ".gradle.plugin"
                                val groupId = catalogPlugin.id
                                AppLogger.d(TAG) { "Found catalogPlugin: $catalogPlugin" }
                                val availableVersions = findAvailableVersionsInGradleCache(
                                    groupId = groupId,
                                    artifactId = mainId,
                                    gradleModulesInfo = gradleModulesInfo
                                )
                                val versionCatalogPlugin = Plugin(
                                    name = catalogPlugin.name,
                                    id = mainId,
                                    version = catalogPlugin.version,
                                    module = module,
                                    configuration = "versionCatalog",
                                    availableVersions = availableVersions,
                                    isAvailable = availableVersions?.versions?.any {
                                        it.version == catalogPlugin.version
                                    } == true,
                                    group = groupId,
                                )
                                AppLogger.d(TAG) { "Found versionCatalogPlugin: $versionCatalogPlugin" }
                                plugins.add(
                                    versionCatalogPlugin
                                )
                            }
                        }
                    }
                }
            }
        }

        // Root module
        rootModuleBuildFileInfo?.let { extractPlugins(it.content, module = "root") }

        // Sub-modules
        subModuleBuildFileInfos.forEach { extractPlugins(it.content, module = it.moduleName) }

        AppLogger.d(TAG) { "Found ${plugins.size} plugins" }
        plugins.forEach {
            AppLogger.i(TAG) { "Plugin name: ${it.name} id: ${it.id} version: ${it.version} module: ${it.module}" }
        }

        return plugins
    }


    private fun findProjectOverviewInfo(
        projectDir: File,
        settingsGradleFileInfo: SettingsGradleFileInfo?,
        gradleWrapperPropertiesFileInfo: GradleWrapperPropertiesFileInfo?,
        versionCatalog: VersionCatalog?,
        rootModuleBuildFileInfo: RootModuleBuildFileInfo?,
        subModuleBuildFileInfos: List<SubModuleBuildFileInfo>,
        isMultiModule: Boolean
    ): ProjectOverviewInfo {
        AppLogger.d(TAG) { "Finding project info" }

        fun findProjectName(): String {
            if (settingsGradleFileInfo == null) return projectDir.name
            val readLines = settingsGradleFileInfo.readLines
            val projectNameLine = readLines.find { it.startsWith("rootProject.name") }
            if (projectNameLine == null) return projectDir.name
            val projectName = projectNameLine.substringAfter("=").replace("\"", "").trim()
            return projectName
        }

        fun findGradleVersion(): String? {
            if (gradleWrapperPropertiesFileInfo == null) return null
            val readLines = gradleWrapperPropertiesFileInfo.readLines
            // distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
            val gradleVersionLine = readLines.find { it.startsWith("distributionUrl") }
            if (gradleVersionLine == null) return null

            val gradleVersion = gradleVersionLine.substringAfter("gradle-").substringBefore(".")
            return gradleVersion
        }

        fun extractAgpVersion(): String? {

            val agpVersion = versionCatalog?.versions?.find { it.name == "agp" }?.version
            if (agpVersion != null) return agpVersion

            if (rootModuleBuildFileInfo == null) return null
            val content = rootModuleBuildFileInfo.content

            // Case 1: plugins DSL
            val pluginsRegex = Regex("""id\("com\.android\.application"\)\s+version\s+"([\d.]+)"""")
            pluginsRegex.find(content)?.let { return it.groupValues[1] }

            // Case 2: classpath dependency (KTS style)
            val ktsClasspathRegex =
                Regex("""classpath\("com\.android\.tools\.build:gradle:([\d.]+)"\)""")
            ktsClasspathRegex.find(content)?.let { return it.groupValues[1] }

            // Case 3: classpath dependency (Groovy style)
            val groovyClasspathRegex =
                Regex("""classpath\s+['"]com\.android\.tools\.build:gradle:([\d.]+)['"]""")
            groovyClasspathRegex.find(content)?.let { return it.groupValues[1] }

            return null
        }


        fun extractKotlinVersion(): String? {
            val kotlinVersion = versionCatalog?.versions?.find { it.name == "kotlin" }?.version
            if (kotlinVersion != null) return kotlinVersion
            if (rootModuleBuildFileInfo == null) return null
            val content = rootModuleBuildFileInfo.content

            // Case 1: plugins DSL
            val pluginsRegex =
                Regex("""id\("org\.jetbrains\.kotlin\.android"\)\s+version\s+"([\d.]+)"""")
            pluginsRegex.find(content)?.let { return it.groupValues[1] }

            // Case 2: classpath dependency (KTS style)
            val ktsClasspathRegex =
                Regex("""classpath\("org\.jetbrains\.kotlin:kotlin-gradle-plugin:([\d.]+)"\)""")
            ktsClasspathRegex.find(content)?.let { return it.groupValues[1] }

            // Case 3: classpath dependency (Groovy style)
            val groovyClasspathRegex =
                Regex("""classpath\s+['"]org\.jetbrains\.kotlin:kotlin-gradle-plugin:([\d.]+)['"]""")
            groovyClasspathRegex.find(content)?.let { return it.groupValues[1] }

            return null
        }

        fun extractCompileSdk(): String? {
            val fromCatalog =
                versionCatalog?.versions?.find { it.name == "android-compileSdk" }?.version
            if (fromCatalog != null) return fromCatalog

            val regex = Regex("""compileSdk(?:Version)?\s*=?\s*(\d+)""")
            val subModuleBuildFileInfo =
                subModuleBuildFileInfos.find { regex.containsMatchIn(it.content) }
            return subModuleBuildFileInfo?.let { regex.find(it.content)?.groupValues?.get(1) }
        }

        fun extractMinSdk(): String? {
            val fromCatalog =
                versionCatalog?.versions?.find { it.name == "android-minSdk" }?.version
            if (fromCatalog != null) return fromCatalog

            val regex = Regex("""minSdk(?:Version)?\s*=?\s*(\d+)""")
            val subModuleBuildFileInfo =
                subModuleBuildFileInfos.find { regex.containsMatchIn(it.content) }
            return subModuleBuildFileInfo?.let { regex.find(it.content)?.groupValues?.get(1) }
        }

        fun extractTargetSdk(): String? {
            val fromCatalog =
                versionCatalog?.versions?.find { it.name == "android-targetSdk" }?.version
            if (fromCatalog != null) return fromCatalog

            val regex = Regex("""targetSdk(?:Version)?\s*=?\s*(\d+)""")
            val subModuleBuildFileInfo =
                subModuleBuildFileInfos.find { regex.containsMatchIn(it.content) }
            return subModuleBuildFileInfo?.let { regex.find(it.content)?.groupValues?.get(1) }
        }

        val sizeBytes = Utils.calculateFolderSize(projectDir)

        val projectOverviewInfo = ProjectOverviewInfo(
            projectPath = projectDir.absolutePath,
            projectName = findProjectName(),
            totalSize = Utils.formatSize(sizeBytes),
            totalSizeBytes = sizeBytes,
            isMultiModule = isMultiModule,
            gradleVersion = findGradleVersion(),
            kotlinVersion = extractKotlinVersion(),
            androidGradlePluginVersion = extractAgpVersion(),
            targetSdkVersion = extractTargetSdk(),
            minSdkVersion = extractMinSdk(),
            compileSdkVersion = extractCompileSdk()
        )
        AppLogger.d(TAG) { "Found project info." }
        AppLogger.i(TAG) {
            """ 
                Project Info:
                Name: ${projectOverviewInfo.projectName} Path: ${projectOverviewInfo.projectPath}
                Total Size: ${projectOverviewInfo.totalSize} Total Size (bytes): ${projectOverviewInfo.totalSizeBytes} 
                Gradle Version: ${projectOverviewInfo.gradleVersion} Kotlin Version: ${projectOverviewInfo.kotlinVersion}
                isMultiModule: ${projectOverviewInfo.isMultiModule} Android Gradle Plugin Version: ${projectOverviewInfo.androidGradlePluginVersion}
                Compile SDK Version: ${projectOverviewInfo.compileSdkVersion} Target SDK Version: ${projectOverviewInfo.targetSdkVersion} Min SDK Version: ${projectOverviewInfo.minSdkVersion}
            """.trimIndent()
        }
        return projectOverviewInfo
    }


    private suspend fun findVersionCatalogFile(projectDir: File): VersionCatalogFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding version catalog" }

            // Find version catalogs
            val versionCatalogFile = File(projectDir, "gradle/libs.versions.toml")
            if (!versionCatalogFile.exists()) return@withContext null
            val sizeBytes = versionCatalogFile.length()

            val versionCatalogFileInfo = VersionCatalogFileInfo(
                name = versionCatalogFile.name,
                path = versionCatalogFile.absolutePath,
                size = Utils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
                content = versionCatalogFile.readText(),
                readLines = versionCatalogFile.readLines(),
                file = versionCatalogFile
            )
            AppLogger.d(TAG) { "Found version catalog." }
            AppLogger.i(TAG) {
                "Name: ${versionCatalogFileInfo.name} Path: ${versionCatalogFileInfo.path} Size: ${versionCatalogFileInfo.size} Size (bytes): ${versionCatalogFileInfo.sizeBytes} isContent: ${versionCatalogFileInfo.content.isNotEmpty()}"
            }
            versionCatalogFileInfo
        }

    private suspend fun findRootModuleBuildFiles(projectDir: File): RootModuleBuildFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding root build files" }


            // Find root build files
            val buildFileType = BuildFileType.entries.find { buildFileType ->
                val file = File(projectDir, buildFileType.fileName)
                file.exists()
            }
            if (buildFileType == null) return@withContext null
            val file = File(projectDir, buildFileType.fileName)
            if (!file.exists()) return@withContext null
            val sizeBytes = file.length()

            val rootModuleBuildFileInfo = RootModuleBuildFileInfo(
                moduleName = "root",
                path = file.absolutePath,
                type = buildFileType,
                size = Utils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
                content = file.readText(),
                readLines = file.readLines(),
                file = file,
            )
            AppLogger.d(TAG) { "Found root build files" }
            AppLogger.i(TAG) {
                "Name: ${rootModuleBuildFileInfo.moduleName} Path: ${rootModuleBuildFileInfo.path} Type: ${rootModuleBuildFileInfo.type} Size: ${rootModuleBuildFileInfo.size} Size (bytes): ${rootModuleBuildFileInfo.sizeBytes} isContent: ${rootModuleBuildFileInfo.content.isNotEmpty()}"
            }
            rootModuleBuildFileInfo
        }


    private suspend fun findSubModuleBuildFiles(projectDir: File): List<SubModuleBuildFileInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding build files" }

            val buildFiles = mutableListOf<SubModuleBuildFileInfo>()

            // Find module build files
            projectDir.listFiles()?.filter { it.isDirectory && !it.name.startsWith(".") }
                ?.forEach { moduleDir ->
                    BuildFileType.entries.forEach { buildFileType ->
                        val file = File(moduleDir, buildFileType.fileName)
                        if (file.exists()) {
                            val sizeBytes = file.length()
                            buildFiles.add(
                                SubModuleBuildFileInfo(
                                    path = file.absolutePath,
                                    type = buildFileType,
                                    size = Utils.formatSize(sizeBytes),
                                    sizeBytes = sizeBytes,
                                    content = file.readText(),
                                    readLines = file.readLines(),
                                    file = file,
                                    moduleName = moduleDir.name,
                                    modulePath = moduleDir.absolutePath
                                )
                            )
                        }
                    }
                }

            AppLogger.d(TAG) { "Found ${buildFiles.size} build files" }
            buildFiles.forEach {
                AppLogger.i(TAG) {
                    "name: ${it.type.fileName} Path: ${it.path} Type: ${it.type} Size: ${it.size} Size (bytes): ${it.sizeBytes} isContent: ${it.content.isNotEmpty()} moduleName = ${it.moduleName} modulePath = ${it.modulePath}"
                }
            }
            buildFiles
        }

    private suspend fun findSettingsGradleFiles(projectDir: File): SettingsGradleFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding settings gradle files" }

            // Find settings gradle files
            val settingsGradleFileType =
                SettingsGradleFileType.entries.find { settingsGradleFileType ->
                    val file = File(projectDir, settingsGradleFileType.fileName)
                    file.exists()
                }
            if (settingsGradleFileType == null) return@withContext null

            val file = File(projectDir, settingsGradleFileType.fileName)
            val sizeBytes = file.length()

            val settingsGradleFileInfo = SettingsGradleFileInfo(
                name = settingsGradleFileType.fileName,
                path = file.absolutePath,
                type = settingsGradleFileType,
                size = Utils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
                content = file.readText(),
                readLines = file.readLines(),
                file = file
            )

            AppLogger.d(TAG) { "Found settings.gradle files" }
            AppLogger.i(TAG) {
                """
                Settings Gradle File:
                Name: ${settingsGradleFileInfo.name}
                Path: ${settingsGradleFileInfo.path}
                Type: ${settingsGradleFileInfo.type}
                Size: ${settingsGradleFileInfo.size}
                Size (bytes): ${settingsGradleFileInfo.sizeBytes}
                isContent: ${settingsGradleFileInfo.content.isNotEmpty()}})}
            """.trimIndent()
            }
            settingsGradleFileInfo
        }

    private suspend fun findPropertiesFiles(projectDir: File): PropertiesFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding properties files" }
            // Find properties files
            val propertiesFileType = PropertiesFileType.entries.find { propertiesFileType ->
                val file = File(projectDir, propertiesFileType.fileName)
                file.exists()
            }
            if (propertiesFileType == null) return@withContext null
            val file = File(projectDir, propertiesFileType.fileName)
            val sizeBytes = file.length()
            val propertiesFileInfo = PropertiesFileInfo(
                name = propertiesFileType.fileName,
                path = file.absolutePath,
                type = propertiesFileType,
                size = Utils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
                content = file.readText(),
                readLines = file.readLines(),
                file = file
            )
            AppLogger.d(TAG) { "Found properties files" }
            AppLogger.i(TAG) {
                """
                Properties File:
                Name: ${propertiesFileInfo.name}
                Path: ${propertiesFileInfo.path}
                Type: ${propertiesFileInfo.type}
                Size: ${propertiesFileInfo.size}
                Size (bytes): ${propertiesFileInfo.sizeBytes}
                isContent: ${propertiesFileInfo.content.isNotEmpty()}
            """.trimIndent()
            }
            propertiesFileInfo
        }

    private suspend fun findGradleWrapperProFile(projectDir: File): GradleWrapperPropertiesFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding gradle wrapper properties file" }

            // Find gradle wrapper properties file
            val gradleWrapperPropertiesFile =
                File(projectDir, "gradle/wrapper/gradle-wrapper.properties")
            if (!gradleWrapperPropertiesFile.exists()) return@withContext null

            val sizeBytes = gradleWrapperPropertiesFile.length()

            val gradleWrapperPropertiesFileInfo = GradleWrapperPropertiesFileInfo(
                name = gradleWrapperPropertiesFile.name,
                path = gradleWrapperPropertiesFile.absolutePath,
                size = Utils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
                content = gradleWrapperPropertiesFile.readText(),
                readLines = gradleWrapperPropertiesFile.readLines(),
                file = gradleWrapperPropertiesFile,
            )

            AppLogger.d(TAG) { "Found version catalog." }
            AppLogger.i(TAG) {
                """
                Gradle Wrapper Properties File:
                Name: ${gradleWrapperPropertiesFileInfo.name}
                Path: ${gradleWrapperPropertiesFileInfo.path}
                Size: ${gradleWrapperPropertiesFileInfo.size}
                Size (bytes): ${gradleWrapperPropertiesFileInfo.sizeBytes}
                isContent: ${gradleWrapperPropertiesFileInfo.content.isNotEmpty()}
            """.trimIndent()
            }

            gradleWrapperPropertiesFileInfo
        }

    private suspend fun findVersionCatalog(
        versionCatalogFileInfo: VersionCatalogFileInfo?
    ): VersionCatalog? = withContext(Dispatchers.IO) {
        if (versionCatalogFileInfo == null) return@withContext null

        @Serializable
        data class VersionPartial(
            val version: String? = null, @SerialName("ref") val ref: String? = null
        )

        @Serializable
        data class PluginPartial(
            val id: String? = null, val version: VersionPartial? = null
        )

        @Serializable
        data class LibraryPartial(
            val group: String? = null,
            @SerialName("name") val libName: String? = null,
            val module: String? = null,
            val version: VersionPartial? = null
        )


        @Serializable
        data class VersionCatalogPartial(
            val versions: Map<String, String> = emptyMap(),
            val libraries: Map<String, LibraryPartial> = emptyMap(),
            val plugins: Map<String, PluginPartial> = emptyMap()
        )

        fun parseBundlesFromToml(toml: String): Map<String, List<String>> {
            val bundles = mutableMapOf<String, MutableList<String>>()
            var currentKey: String? = null
            var insideArray = false
            val buffer = mutableListOf<String>()

            for (line in toml.lines()) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

                if (!insideArray) {
                    val match = Regex("""^([\w-]+)\s*=\s*\[""").find(trimmed)
                    if (match != null) {
                        currentKey = match.groupValues[1]
                        bundles[currentKey] = mutableListOf()
                        insideArray = true
                        buffer.clear()
                        buffer += trimmed

                        // handle inline case where [ ... ] on the same line
                        if (trimmed.contains("]")) {
                            val values =
                                trimmed.substringAfter("[").substringBeforeLast("]").split(",")
                                    .mapNotNull { value ->
                                        value.trim().removeSurrounding("\"")
                                            .takeIf { it.isNotEmpty() }
                                    }
                            bundles[currentKey]?.addAll(values)
                            insideArray = false
                            currentKey = null
                            buffer.clear()
                        }
                        continue
                    }
                } else {
                    buffer += trimmed
                    if (trimmed.contains("]")) {
                        val joined = buffer.joinToString(" ")
                        val values =
                            joined.substringAfter("[").substringBeforeLast("]").split(",")
                                .mapNotNull { value ->
                                    value.trim().removeSurrounding("\"").takeIf { it.isNotEmpty() }
                                }
                        bundles[currentKey!!]?.addAll(values)
                        insideArray = false
                        currentKey = null
                        buffer.clear()
                    }
                }
            }

            return bundles
        }

        val tomlText = versionCatalogFileInfo.content

        val partial = Toml(
            inputConfig = TomlInputConfig(ignoreUnknownNames = true)
        ).decodeFromString<VersionCatalogPartial>(tomlText)

        val bundleMap = parseBundlesFromToml(versionCatalogFileInfo.content)
        val versionMap = partial.versions

        val versionCatalog = VersionCatalog(
            versions = versionMap.map { (k, v) ->
                Version(name = k, version = v)
            },

            libraries = partial.libraries.map { (k, v) ->

                val resolvedVersion = when {
                    v.version?.version != null -> v.version.version
                    v.version?.ref != null -> versionMap[v.version.ref]
                    else -> null
                }
                val notation = when {
                    v.module != null -> v.module
                    v.group != null && v.libName != null -> "${v.group}:${v.libName}"
                    else -> k
                }
                val (group, libName) = when {
                    v.module != null -> {
                        val parts = v.module.split(":")
                        parts[0] to parts[1]
                    }

                    v.group != null && v.libName != null -> v.group to v.libName
                    else -> null to k // fallback
                }
                Library(
                    name = k,
                    group = group,
                    libName = libName,
                    version = resolvedVersion,
                    id = notation
                )
            },

            plugins = partial.plugins.map { (k, v) ->
                val resolvedVersion = when {
                    v.version?.version != null -> v.version.version
                    v.version?.ref != null -> versionMap[v.version.ref]
                    else -> null
                }
                Plugin(
                    name = k,
                    id = v.id!!,
                    version = resolvedVersion,
                    module = "",
                    configuration = "versionCatalog",
                    group = ""
                )
            },

            bundles = bundleMap.map { (k, v) ->
                Bundle(name = k, artifacts = v)
            }
        )
        AppLogger.d(TAG) { "Found version catalog." }
        AppLogger.i(TAG) {
            """
                Version Catalog:
                Versions: ${versionCatalog.versions.size}
                Libraries: ${versionCatalog.libraries.size}
                Plugins: ${versionCatalog.plugins.size}
                Bundles: ${versionCatalog.bundles.size}
            """.trimIndent()
        }
        AppLogger.i(TAG) { "Version:" }
        versionCatalog.versions.forEach {
            AppLogger.i(TAG) {
                "Name: ${it.name} Version: ${it.version}"
            }
        }
        AppLogger.i(TAG) { "Library:" }
        versionCatalog.libraries.forEach {
            AppLogger.i(TAG) {
                "Name: ${it.name} Group: ${it.group} LibName: ${it.libName} Version: ${it.version} id: ${it.id}"
            }
        }
        AppLogger.i(TAG) { "Plugin:" }
        versionCatalog.plugins.forEach {
            AppLogger.i(TAG) {
                "Name: ${it.name} Id: ${it.id} Version: ${it.version} Module: ${it.module}"
            }
        }
        AppLogger.i(TAG) { "Bundle:" }
        versionCatalog.bundles.forEach {
            AppLogger.i(TAG) {
                "Name: ${it.name} Artifacts: ${it.artifacts}"
            }
        }
        versionCatalog
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun findProjectFiles(projectDir: File): List<ProjectFileInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding project files in: ${projectDir.absolutePath}" }

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
                    .forEach { file ->
                        try {
                            val relativePath = file.relativeTo(projectDir).path
                            val fileType = determineFileType(file)
                            val sizeBytes = file.length()
                            val isReadable = isTextFile(file)

                            projectFiles.add(
                                ProjectFileInfo(
                                    uniqueId = Uuid.random().toString(),
                                    name = file.name,
                                    path = file.absolutePath,
                                    relativePath = relativePath,
                                    type = fileType,
                                    size = Utils.formatSize(sizeBytes),
                                    sizeBytes = sizeBytes,
                                    extension = file.extension.lowercase(),
                                    content = file.readText(),
                                    file = file,
                                    isReadable = isReadable
                                )
                            )
                        } catch (e: Exception) {
                            AppLogger.e(TAG, e) { "Error processing file: ${file.absolutePath}" }
                        }
                    }
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error walking project directory" }
            }

            AppLogger.d(TAG) { "Found ${projectFiles.size} project files" }
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
            extension in listOf("png", "jpg", "jpeg", "gif", "bmp", "webp", "svg") -> FileType.IMAGE
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
            "txt", "md", "gradle", "gitignore", "pro", "conf", "config", "sh", "bat",
            "swift", "xcconfig", "plist"
        )
        return file.extension.lowercase() in textExtensions ||
                file.name.lowercase() in setOf("dockerfile", "makefile", "readme")
    }

}
