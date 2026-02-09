package com.meet.dev.analyzer.data.repository.project

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.meet.dev.analyzer.data.models.project.BuildFileType
import com.meet.dev.analyzer.data.models.project.Bundle
import com.meet.dev.analyzer.data.models.project.Dependency
import com.meet.dev.analyzer.data.models.project.FileType
import com.meet.dev.analyzer.data.models.project.GradleWrapperPropertiesFileInfo
import com.meet.dev.analyzer.data.models.project.Library
import com.meet.dev.analyzer.data.models.project.ModuleBuildFileInfo
import com.meet.dev.analyzer.data.models.project.Plugin
import com.meet.dev.analyzer.data.models.project.ProjectFileInfo
import com.meet.dev.analyzer.data.models.project.ProjectInfo
import com.meet.dev.analyzer.data.models.project.ProjectOverviewInfo
import com.meet.dev.analyzer.data.models.project.PropertiesFileInfo
import com.meet.dev.analyzer.data.models.project.PropertiesFileType
import com.meet.dev.analyzer.data.models.project.SettingsGradleFileInfo
import com.meet.dev.analyzer.data.models.project.SettingsGradleFileType
import com.meet.dev.analyzer.data.models.project.Version
import com.meet.dev.analyzer.data.models.project.VersionCatalog
import com.meet.dev.analyzer.data.models.project.VersionCatalogFileInfo
import com.meet.dev.analyzer.data.models.storage.GradleLibraryInfo
import com.meet.dev.analyzer.data.models.storage.GradleModulesInfo
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class ProjectAnalyzerRepositoryImpl : ProjectAnalyzerRepository {

    private val TAG = tagName(javaClass = javaClass)

    override suspend fun analyzeProject(
        projectPath: String, updateProgress: (progress: Float, status: String) -> Unit
    ): ProjectInfo = withContext(Dispatchers.IO) {
        AppLogger.i(tag = TAG) { "Starting project analysis for: $projectPath" }

        val projectDir = File(projectPath)

        updateProgress(0.1f, "Finding build files...")

        val moduleBuildFileInfos =
            findModuleBuildFiles(projectDir = projectDir)

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
                moduleBuildFileInfos = moduleBuildFileInfos
            )

        updateProgress(0.5f, "Analyzing plugins...")
        val gradleModulesInfo = FolderFileUtils.getGradleModulesInfo()
        val plugins =
            findPlugin(
                moduleBuildFileInfos = moduleBuildFileInfos,
                versionCatalog = versionCatalog,
                gradleModulesInfo = gradleModulesInfo,
            )

        updateProgress(0.7f, "Analyzing dependencies...")
        val dependencies =
            findDependencies(
                moduleBuildFileInfos = moduleBuildFileInfos,
                versionCatalog = versionCatalog,
                gradleModulesInfo = gradleModulesInfo
            )
        val modulesWithDependency =
            addDependencyEachModule(
                moduleBuildFileInfos = moduleBuildFileInfos,
                plugins = plugins,
                dependencies = dependencies
            )
        updateProgress(0.8f, "Building project info...")

        val projectFiles = findProjectFiles(projectDir)

        val projectInfo = ProjectInfo(
            projectOverviewInfo = projectOverviewInfo,
            plugins = plugins,
            dependencies = dependencies,
            moduleBuildFileInfos = modulesWithDependency,
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
        moduleBuildFileInfos: List<ModuleBuildFileInfo>,
        plugins: List<Plugin>,
        dependencies: List<Dependency>
    ) = moduleBuildFileInfos.map { moduleBuildFileInfo ->
        moduleBuildFileInfo.copy(
            plugins = plugins.filter { plugin ->
                plugin.module == moduleBuildFileInfo.moduleName
            },
            dependencies = dependencies.filter { dependency ->
                dependency.module == moduleBuildFileInfo.moduleName
            }
        )
    }

    private fun findAvailableVersionsInGradleCache(
        groupId: String?,
        artifactId: String?,
        gradleModulesInfo: GradleModulesInfo?
    ): GradleLibraryInfo? {
        AppLogger.d(tag = TAG) { "Finding available versions for: $groupId:$artifactId" }
        if (gradleModulesInfo == null) return null
        val lib = gradleModulesInfo.libraries.find {
            it.groupId == groupId && it.artifactId == artifactId
        }
        AppLogger.d(tag = TAG) { "Found $groupId:$artifactId ${lib?.versions?.size} available versions" }
        lib?.versions?.forEach {
            AppLogger.d(tag = TAG) { "Available version: ${it.version} Size: ${it.sizeReadable}" }
        }
        return lib
    }

    private fun findDependencies(
        moduleBuildFileInfos: List<ModuleBuildFileInfo>,
        versionCatalog: VersionCatalog?,
        gradleModulesInfo: GradleModulesInfo?,
    ): List<Dependency> {


        AppLogger.d(tag = TAG) { "Finding dependencies" }

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

        // alias-like without libs prefix → implementation(compose.components.uiToolingPreview)
        val unprefixedAliasDepRegex =
            Regex("""(implementation|api|ksp|kapt|compileOnly|runtimeOnly|testImplementation|androidTestImplementation)\(\s*([a-zA-Z_][\w.]+)\s*\)""")

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

                    val availableGradleVersions = findAvailableVersionsInGradleCache(
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
                        availableGradleVersions = availableGradleVersions,
                        isVersionSynced = availableGradleVersions?.versions?.any {
                            it.version == version
                        } == true
                    )
                    AppLogger.d(tag = TAG) { "Found normalDependency: $normalDependency" }
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
                            val availableGradleVersions = findAvailableVersionsInGradleCache(
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
                                availableGradleVersions = availableGradleVersions,
                                isVersionSynced = availableGradleVersions?.versions?.any {
                                    it.version == lib.version
                                } == true
                            )
                            AppLogger.d(tag = TAG) { "Found versionCatalogDependency: $versionCatalogDependency" }
                            dependencies.add(
                                versionCatalogDependency
                            )
                        } else {
                            AppLogger.d(tag = TAG) { "Library not found in version catalog: $match" }
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
                            val availableGradleVersions = findAvailableVersionsInGradleCache(
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
                                availableGradleVersions = availableGradleVersions,
                                isVersionSynced = availableGradleVersions?.versions?.any {
                                    it.version == library.version
                                } == true
                            )
                            AppLogger.d(tag = TAG) { "Found bundleDependency: $bundleDependency" }
                            dependencies.add(bundleDependency)
                        } else {
                            AppLogger.d(tag = TAG) { "Library not found in bundle: $artifact" }
                        }
                    }
                }

                // Unprefixed alias style (e.g. implementation(compose.components.uiToolingPreview))
                unprefixedAliasDepRegex.findAll(content).forEach { match ->
                    val path = match.groupValues[2] // e.g. compose.components.uiToolingPreview
                    // Skip ones already matched by libs.* to avoid duplicates
                    if (!path.startsWith("libs.")
                        && !path.startsWith("project")
                        && !path.startsWith("files")
                    ) {
                        val alias =
                            path.replace('.', '-') // e.g. compose-components-uiToolingPreview

                        val lib = versionCatalog?.libraries?.find {
                            it.name == alias
                        }

                        if (lib != null) {
                            val availableGradleVersions = findAvailableVersionsInGradleCache(
                                groupId = lib.group,
                                artifactId = lib.libName,
                                gradleModulesInfo = gradleModulesInfo
                            )
                            val dependency = Dependency(
                                versionName = lib.name,
                                name = lib.libName ?: lib.name,
                                id = lib.id,
                                group = lib.group ?: "",
                                version = lib.version,
                                configuration = match.groupValues[1],
                                module = module,
                                availableGradleVersions = availableGradleVersions,
                                isVersionSynced = availableGradleVersions?.versions?.any {
                                    it.version == lib.version
                                } == true
                            )
                            AppLogger.d(tag = TAG) { "Found unprefixedAliasDependency: $dependency" }
                            dependencies.add(dependency)
                        } else {
//                            AppLogger.d(tag = TAG) { "Library not found for unprefixed alias: $path" }
                            AppLogger.d(tag = TAG) { "Library not found for unprefixed alias: ${match.groupValues}" }
                            dependencies.add(
                                Dependency(
                                    versionName = "",
                                    name = path.substringAfterLast('.'),
                                    id = path,
                                    group = path,
                                    version = null,
                                    configuration = match.groupValues[1],
                                    module = module,
                                    availableGradleVersions = null,
                                    isVersionSynced = false
                                )
                            )
                        }
                    }
                }
            }
        }

        moduleBuildFileInfos.forEach { moduleBuildFileInfo ->
            findDependencies(
                mainContent = moduleBuildFileInfo.content,
                module = moduleBuildFileInfo.moduleName
            )
        }

        AppLogger.d(tag = TAG) { "Found ${dependencies.size} dependencies" }
        dependencies.forEach {
            AppLogger.i(tag = TAG) { "Dependency name: ${it.name} id: ${it.id} version: ${it.version} module: ${it.module} type: ${it.configuration} isAvailable: ${it.isVersionSynced} availableGradleVersions: ${it.availableGradleVersions}" }
        }

        return dependencies
    }

    private fun findPlugin(
        moduleBuildFileInfos: List<ModuleBuildFileInfo>,
        versionCatalog: VersionCatalog? = null,
        gradleModulesInfo: GradleModulesInfo?,
    ): List<Plugin> {
        AppLogger.d(tag = TAG) { "Finding plugins" }

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

                            val availableGradleVersions = findAvailableVersionsInGradleCache(
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
                                availableGradleVersions = availableGradleVersions,
                                isVersionSynced = availableGradleVersions?.versions?.any {
                                    it.version == version
                                } == true
                            )
                            AppLogger.d(tag = TAG) { "Found normalPlugin: $normalPlugin" }
                            plugins.add(
                                normalPlugin
                            )
                        }

                        // Library: com.android.tools.build:gradle groupId: com.android.tools.build artifactId: gradle versions: [7.2.2, 8.0.0, 8.1.4, 8.10.0, 8.10.1, 8.11.1, 8.12.0, 8.12.2, 8.12.3, 8.13.0, 8.5.2, 8.7.3, 8.8.0] sizeReadable: 142.89 MB totalSizeBytes: 149827947
                        // Case: classpath 'com.android.tools.build:gradle:8.0.2'
                        regexList[2].pattern -> {
                            val id = match.groupValues[1] // ex: com.android.tools.build:gradle
                            val version = match.groupValues[2] // ex: 8.3.2
                            val groupId = id.substringBeforeLast(":")  // ex com.android.tools.build
                            val artifactId = id.substringAfterLast(':') // ex gradle

                            val availableGradleVersions = findAvailableVersionsInGradleCache(
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
                                availableGradleVersions = availableGradleVersions,
                                isVersionSynced = availableGradleVersions?.versions?.any {
                                    it.version == version
                                } == true
                            )
                            AppLogger.d(tag = TAG) { "Found classPathPlugin: $classPathPlugin" }
                            plugins.add(
                                classPathPlugin
                            )
                        }

                        // Case: alias(libs.plugins.xxx)
                        // Library: org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin groupId: org.jetbrains.kotlin.plugin.serialization artifactId: org.jetbrains.kotlin.plugin.serialization.gradle.plugin versions: [2.1.21, 2.2.10, 2.2.20] sizeReadable: 4.40 KB totalSizeBytes: 4506
                        // alias(libs.plugins.kotlinSerialization) apply false
                        regexList[3].pattern -> {
                            val id = match.groupValues[1] // ex: libs.plugins.kotlinSerialization

                            val catalogPlugin = versionCatalog?.plugins?.find {
                                it.name == id.substringAfter("libs.plugins.").replace(".", "-")
                            }
                            if (catalogPlugin != null) {
                                val mainId = catalogPlugin.id + ".gradle.plugin"
                                val groupId = catalogPlugin.id
                                AppLogger.d(tag = TAG) { "Found catalogPlugin: $catalogPlugin" }
                                val availableGradleVersions = findAvailableVersionsInGradleCache(
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
                                    availableGradleVersions = availableGradleVersions,
                                    isVersionSynced = availableGradleVersions?.versions?.any {
                                        it.version == catalogPlugin.version
                                    } == true,
                                    group = groupId,
                                )
                                AppLogger.d(tag = TAG) { "Found versionCatalogPlugin: $versionCatalogPlugin" }
                                plugins.add(
                                    versionCatalogPlugin
                                )
                            }
                        }
                    }
                }
            }
        }

        // modules
        moduleBuildFileInfos.forEach { extractPlugins(it.content, module = it.moduleName) }

        AppLogger.d(tag = TAG) { "Found ${plugins.size} plugins" }
        plugins.forEach {
            AppLogger.i(tag = TAG) { "Plugin name: ${it.name} id: ${it.id} version: ${it.version} module: ${it.module}" }
        }

        return plugins
    }


    private fun findProjectOverviewInfo(
        projectDir: File,
        settingsGradleFileInfo: SettingsGradleFileInfo?,
        gradleWrapperPropertiesFileInfo: GradleWrapperPropertiesFileInfo?,
        versionCatalog: VersionCatalog?,
        moduleBuildFileInfos: List<ModuleBuildFileInfo>
    ): ProjectOverviewInfo {
        AppLogger.d(tag = TAG) { "Finding project info" }

        fun findProjectName(): String {
            if (settingsGradleFileInfo == null) return projectDir.name
            val readLines = settingsGradleFileInfo.readLines
            val projectNameLine = readLines.find { it.startsWith("rootProject.name") }
            if (projectNameLine == null) return projectDir.name
            val projectName = projectNameLine.substringAfter("=").replace("\"", "").trim()
            return projectName
        }

        val rootModuleBuildFileInfo =
            moduleBuildFileInfos.find { it.moduleName == findProjectName() }

        fun findGradleVersion(): String? {
            if (gradleWrapperPropertiesFileInfo == null) return null
            val readLines = gradleWrapperPropertiesFileInfo.readLines
            // distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
            val gradleVersionLine = readLines.find { it.startsWith("distributionUrl") }
            if (gradleVersionLine == null) return null

            val gradleVersion =
                gradleVersionLine.substringAfter("gradle-").substringBefore("-bin.zip")
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
                moduleBuildFileInfos.find { regex.containsMatchIn(it.content) }
            return subModuleBuildFileInfo?.let { regex.find(it.content)?.groupValues?.get(1) }
        }

        fun extractMinSdk(): String? {
            val fromCatalog =
                versionCatalog?.versions?.find { it.name == "android-minSdk" }?.version
            if (fromCatalog != null) return fromCatalog

            val regex = Regex("""minSdk(?:Version)?\s*=?\s*(\d+)""")
            val subModuleBuildFileInfo =
                moduleBuildFileInfos.find { regex.containsMatchIn(it.content) }
            return subModuleBuildFileInfo?.let { regex.find(it.content)?.groupValues?.get(1) }
        }

        fun extractTargetSdk(): String? {
            val fromCatalog =
                versionCatalog?.versions?.find { it.name == "android-targetSdk" }?.version
            if (fromCatalog != null) return fromCatalog

            val regex = Regex("""targetSdk(?:Version)?\s*=?\s*(\d+)""")
            val subModuleBuildFileInfo =
                moduleBuildFileInfos.find { regex.containsMatchIn(it.content) }
            return subModuleBuildFileInfo?.let { regex.find(it.content)?.groupValues?.get(1) }
        }

        fun extractNdkVersion(): String? {
            val regex = Regex("""ndkVersion\s*=?\s*["']?(\d+\.\d+\.\d+)["']?""")
            val subModuleBuildFileInfo =
                moduleBuildFileInfos.find { regex.containsMatchIn(it.content) }
            return subModuleBuildFileInfo?.let { regex.find(it.content)?.groupValues?.get(1) }
        }

        fun extractCmakeVersion(): String? {
            val regex = Regex("""version\s*=?\s*["']?(\d+\.\d+\.\d+)["']?""")
            val subModuleBuildFileInfo =
                moduleBuildFileInfos.find { regex.containsMatchIn(it.content) }
            return subModuleBuildFileInfo?.let { regex.find(it.content)?.groupValues?.get(1) }
        }

        fun getPlatforms(): List<String> {
            val platforms = mutableSetOf<String>()

            val androidRegex = Regex("""\bandroid(Target)?\s*([({])""")
            val jvmRegex = Regex("""\bjvm\s*([({])""")
            val jsRegex = Regex("""\bjs\s*([({])""")
            val wasmRegex = Regex("""\bwasm(Js)?\s*([({])""")
            val iosRegex = Regex("""\bios(Arm64|X64|SimulatorArm64)?\s*([({])""")
            val serverRegex = Regex("""\bapplication\s*([({])""")

            moduleBuildFileInfos.forEach { file ->
                val content = file.content

                if (androidRegex.containsMatchIn(content)) platforms.add("ANDROID")
                if (jvmRegex.containsMatchIn(content)) platforms.add("JVM")
                if (jsRegex.containsMatchIn(content)) platforms.add("JS")
                if (wasmRegex.containsMatchIn(content)) platforms.add("WASM")
                if (iosRegex.containsMatchIn(content)) platforms.add("IOS")
                if (serverRegex.containsMatchIn(content)) platforms.add("SERVER")
            }

            return platforms.toList()
        }

        val sizeBytes = FolderFileUtils.calculateFolderSize(projectDir)

        val projectOverviewInfo = ProjectOverviewInfo(
            projectPath = projectDir.absolutePath,
            projectName = findProjectName(),
            sizeReadable = FolderFileUtils.formatSize(sizeBytes),
            totalSizeBytes = sizeBytes,
            isMultiModule = moduleBuildFileInfos.size > 2,
            gradleVersion = findGradleVersion(),
            kotlinVersion = extractKotlinVersion(),
            androidGradlePluginVersion = extractAgpVersion(),
            targetSdkVersion = extractTargetSdk(),
            minSdkVersion = extractMinSdk(),
            compileSdkVersion = extractCompileSdk(),
            ndkVersion = extractNdkVersion(),
            cmakeVersion = extractCmakeVersion(),
            platformList = getPlatforms()
        )
        AppLogger.d(tag = TAG) { "Found project info." }
        AppLogger.i(tag = TAG) {
            """ 
                Project Info:
                Name: ${projectOverviewInfo.projectName} Path: ${projectOverviewInfo.projectPath}
                Total Size: ${projectOverviewInfo.sizeReadable} Total Size (bytes): ${projectOverviewInfo.totalSizeBytes} 
                Gradle Version: ${projectOverviewInfo.gradleVersion} Kotlin Version: ${projectOverviewInfo.kotlinVersion}
                isMultiModule: ${projectOverviewInfo.isMultiModule} Android Gradle Plugin Version: ${projectOverviewInfo.androidGradlePluginVersion}
                Compile SDK Version: ${projectOverviewInfo.compileSdkVersion} Target SDK Version: ${projectOverviewInfo.targetSdkVersion} Min SDK Version: ${projectOverviewInfo.minSdkVersion}
                Ndk Version: ${projectOverviewInfo.ndkVersion} CMake Version: ${projectOverviewInfo.cmakeVersion}
                Platforms: ${projectOverviewInfo.platformList}
            """.trimIndent()
        }
        return projectOverviewInfo
    }


    private suspend fun findVersionCatalogFile(projectDir: File): VersionCatalogFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(tag = TAG) { "Finding version catalog" }

            // Find version catalogs
            val versionCatalogFile =
                File(projectDir, "gradle" + File.separator + "libs.versions.toml")
            if (!versionCatalogFile.exists()) return@withContext null
            val sizeBytes = versionCatalogFile.length()

            val versionCatalogFileInfo = VersionCatalogFileInfo(
                name = versionCatalogFile.name,
                path = versionCatalogFile.absolutePath,
                sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
                content = versionCatalogFile.readText(),
                readLines = versionCatalogFile.readLines(),
                file = versionCatalogFile
            )
            AppLogger.d(tag = TAG) { "Found version catalog." }
            AppLogger.i(tag = TAG) {
                "Name: ${versionCatalogFileInfo.name} Path: ${versionCatalogFileInfo.path} Size: ${versionCatalogFileInfo.sizeReadable} Size (bytes): ${versionCatalogFileInfo.sizeBytes} isContent: ${versionCatalogFileInfo.content.isNotEmpty()}"
            }
            versionCatalogFileInfo
        }

    private suspend fun findModuleBuildFiles(projectDir: File): List<ModuleBuildFileInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.d(tag = TAG) { "Finding build files" }

            val moduleDirs = projectDir.walkTopDown()
                .filter { it.isDirectory && !it.name.startsWith(".") }
                .toList()
            // Find module build files
            val buildFiles = moduleDirs.flatMap { moduleDir ->
                BuildFileType.entries.mapNotNull { buildFileType ->
                    val file = File(moduleDir, buildFileType.fileName)
                    if (file.exists()) {
                        val sizeBytes = file.length()
                        val relativePath = moduleDir.relativeTo(projectDir).path
                        val moduleName =
                            relativePath.replace(File.separatorChar, ':').ifEmpty { moduleDir.name }

                        ModuleBuildFileInfo(
                            path = file.absolutePath,
                            type = buildFileType,
                            sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                            sizeBytes = sizeBytes,
                            content = file.readText(),
                            readLines = file.readLines(),
                            file = file,
                            moduleName = moduleName,
                            modulePath = moduleDir.absolutePath
                        )
                    } else null
                }
            }


            AppLogger.d(tag = TAG) { "Found ${buildFiles.size} build files" }
            buildFiles.forEach {
                AppLogger.i(tag = TAG) {
                    "name: ${it.type.fileName} Path: ${it.path} Type: ${it.type} Size: ${it.sizeReadable} Size (bytes): ${it.sizeBytes} isContent: ${it.content.isNotEmpty()} moduleName = ${it.moduleName} modulePath = ${it.modulePath}"
                }
            }
            buildFiles
        }

    private suspend fun findSettingsGradleFiles(projectDir: File): SettingsGradleFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(tag = TAG) { "Finding settings gradle files" }

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
                size = FolderFileUtils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
                content = file.readText(),
                readLines = file.readLines(),
                file = file
            )

            AppLogger.d(tag = TAG) { "Found settings.gradle files" }
            AppLogger.i(tag = TAG) {
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
            AppLogger.d(tag = TAG) { "Finding properties files" }
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
                size = FolderFileUtils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
                content = file.readText(),
                readLines = file.readLines(),
                file = file
            )
            AppLogger.d(tag = TAG) { "Found properties files" }
            AppLogger.i(tag = TAG) {
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
            AppLogger.d(tag = TAG) { "Finding gradle wrapper properties file" }

            // Find gradle wrapper properties file
            val gradleWrapperPropertiesFile =
                File(
                    projectDir,
                    "gradle" + File.separator + "wrapper" + File.separator + "gradle-wrapper.properties"
                )
            if (!gradleWrapperPropertiesFile.exists()) return@withContext null

            val sizeBytes = gradleWrapperPropertiesFile.length()

            val gradleWrapperPropertiesFileInfo = GradleWrapperPropertiesFileInfo(
                name = gradleWrapperPropertiesFile.name,
                path = gradleWrapperPropertiesFile.absolutePath,
                size = FolderFileUtils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
                content = gradleWrapperPropertiesFile.readText(),
                readLines = gradleWrapperPropertiesFile.readLines(),
                file = gradleWrapperPropertiesFile,
            )

            AppLogger.d(tag = TAG) { "Found version catalog." }
            AppLogger.i(tag = TAG) {
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
        AppLogger.d(tag = TAG) { "Found version catalog." }
        AppLogger.i(tag = TAG) {
            """
                Version Catalog:
                Versions: ${versionCatalog.versions.size}
                Libraries: ${versionCatalog.libraries.size}
                Plugins: ${versionCatalog.plugins.size}
                Bundles: ${versionCatalog.bundles.size}
            """.trimIndent()
        }
        AppLogger.i(tag = TAG) { "Version:" }
        versionCatalog.versions.forEach {
            AppLogger.i(tag = TAG) {
                "Name: ${it.name} Version: ${it.version}"
            }
        }
        AppLogger.i(tag = TAG) { "Library:" }
        versionCatalog.libraries.forEach {
            AppLogger.i(tag = TAG) {
                "Name: ${it.name} Group: ${it.group} LibName: ${it.libName} Version: ${it.version} id: ${it.id}"
            }
        }
        AppLogger.i(tag = TAG) { "Plugin:" }
        versionCatalog.plugins.forEach {
            AppLogger.i(tag = TAG) {
                "Name: ${it.name} Id: ${it.id} Version: ${it.version} Module: ${it.module}"
            }
        }
        AppLogger.i(tag = TAG) { "Bundle:" }
        versionCatalog.bundles.forEach {
            AppLogger.i(tag = TAG) {
                "Name: ${it.name} Artifacts: ${it.artifacts}"
            }
        }
        versionCatalog
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun findProjectFiles(projectDir: File): List<ProjectFileInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.d(tag = TAG) { "Finding project files in: ${projectDir.absolutePath}" }

            val projectFiles = mutableListOf<ProjectFileInfo>()
            fun determineFileType(file: File): FileType {
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
                    extension in listOf(
                        "png",
                        "jpg",
                        "jpeg",
                        "gif",
                        "bmp",
                        "webp",
                        "svg"
                    ) -> FileType.IMAGE

                    path.contains("drawable") -> FileType.DRAWABLE
                    path.contains("assets") -> FileType.ASSETS
                    path.contains("res/") -> FileType.RESOURCE
                    extension in listOf(
                        "toml",
                        "yaml",
                        "yml",
                        "conf",
                        "config"
                    ) -> FileType.CONFIGURATION

                    else -> FileType.OTHER
                }
            }

            fun isTextFile(file: File): Boolean {
                val textExtensions = setOf(
                    "kt", "kts", "java", "xml", "json", "properties", "toml", "yaml", "yml",
                    "txt", "md", "gradle", "gitignore", "pro", "conf", "config", "sh", "bat",
                    "swift", "xcconfig", "plist"
                )
                return file.extension.lowercase() in textExtensions ||
                        file.name.lowercase() in setOf("dockerfile", "makefile", "readme")
            }
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
                                    sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                                    sizeBytes = sizeBytes,
                                    extension = file.extension.lowercase(),
                                    content = file.readText(),
                                    file = file,
                                    isReadable = isReadable
                                )
                            )
                        } catch (e: Exception) {
                            AppLogger.e(
                                tag = TAG,
                                throwable = e
                            ) { "Error processing file: ${file.absolutePath}" }
                        }
                    }
            } catch (e: Exception) {
                AppLogger.e(tag = TAG, throwable = e) { "Error walking project directory" }
            }

            AppLogger.d(tag = TAG) { "Found ${projectFiles.size} project files" }
            projectFiles.sortedBy { it.relativePath }
        }

}
