package com.meet.dev.analyzer.data.repository.project.helpers

import com.meet.dev.analyzer.data.models.project.Dependency
import com.meet.dev.analyzer.data.models.project.ModuleBuildFileInfo
import com.meet.dev.analyzer.data.models.project.Plugin
import com.meet.dev.analyzer.data.models.project.VersionCatalog
import com.meet.dev.analyzer.data.models.storage.GradleLibraryInfo
import com.meet.dev.analyzer.data.models.storage.GradleModulesInfo
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName

class DependencyAnalyzer {
    private val tag = tagName(javaClass = javaClass)

    fun addDependencyEachModule(
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

    fun findAvailableVersionsInGradleCache(
        groupId: String?,
        artifactId: String?,
        gradleModulesInfo: GradleModulesInfo?
    ): GradleLibraryInfo? {
        AppLogger.d(tag = tag) { "Finding available versions for: $groupId:$artifactId" }
        if (gradleModulesInfo == null) return null
        val lib = gradleModulesInfo.libraries.find {
            it.groupId == groupId && it.artifactId == artifactId
        }
        AppLogger.d(tag = tag) { "Found $groupId:$artifactId ${lib?.versions?.size} available versions" }
        lib?.versions?.forEach {
            AppLogger.d(tag = tag) { "Available version: ${it.version} Size: ${it.sizeReadable}" }
        }
        return lib
    }

    fun findDependencies(
        moduleBuildFileInfos: List<ModuleBuildFileInfo>,
        versionCatalog: VersionCatalog?,
        gradleModulesInfo: GradleModulesInfo?,
    ): List<Dependency> {

        AppLogger.d(tag = tag) { "Finding dependencies" }

        val versionResolver = GradleVersionResolver()
        versionResolver.collectExtVariables(moduleBuildFileInfos)

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
                    val version = versionResolver.resolve(match.groupValues[4]) // Resolve version

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
                    AppLogger.d(tag = tag) { "Found normalDependency: $normalDependency" }
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
                            AppLogger.d(tag = tag) { "Found versionCatalogDependency: $versionCatalogDependency" }
                            dependencies.add(
                                versionCatalogDependency
                            )
                        } else {
                            AppLogger.d(tag = tag) { "Library not found in version catalog: $match" }
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
                            AppLogger.d(tag = tag) { "Found bundleDependency: $bundleDependency" }
                            dependencies.add(bundleDependency)
                        } else {
                            AppLogger.d(tag = tag) { "Library not found in bundle: $artifact" }
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
                            AppLogger.d(tag = tag) { "Found unprefixedAliasDependency: $dependency" }
                            dependencies.add(dependency)
                        } else {
//                            AppLogger.d(tag = tag) { "Library not found for unprefixed alias: $path" }
                            AppLogger.d(tag = tag) { "Library not found for unprefixed alias: ${match.groupValues}" }
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

        AppLogger.d(tag = tag) { "Found ${dependencies.size} dependencies" }
        dependencies.forEach {
            AppLogger.i(tag = tag) { "Dependency name: ${it.name} id: ${it.id} version: ${it.version} module: ${it.module} type: ${it.configuration} isAvailable: ${it.isVersionSynced} availableGradleVersions: ${it.availableGradleVersions}" }
        }

        return dependencies
    }
}
