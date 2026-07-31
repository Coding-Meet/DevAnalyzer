package com.meet.dev.analyzer.data.repository.project.helpers

import com.meet.dev.analyzer.data.models.project.*
import com.meet.dev.analyzer.data.models.storage.GradleLibraryInfo
import com.meet.dev.analyzer.data.models.storage.GradleModulesInfo
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName

class PluginAnalyzer {
    private val tag = tagName(javaClass = javaClass)

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

    fun findPlugin(
        moduleBuildFileInfos: List<ModuleBuildFileInfo>,
        versionCatalog: VersionCatalog? = null,
        gradleModulesInfo: GradleModulesInfo?,
    ): List<Plugin> {
        AppLogger.d(tag = tag) { "Finding plugins" }

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
                            AppLogger.d(tag = tag) { "Found normalPlugin: $normalPlugin" }
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
                            AppLogger.d(tag = tag) { "Found classPathPlugin: $classPathPlugin" }
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
                                AppLogger.d(tag = tag) { "Found catalogPlugin: $catalogPlugin" }
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
                                AppLogger.d(tag = tag) { "Found versionCatalogPlugin: $versionCatalogPlugin" }
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

        AppLogger.d(tag = tag) { "Found ${plugins.size} plugins" }
        plugins.forEach {
            AppLogger.i(tag = tag) { "Plugin name: ${it.name} id: ${it.id} version: ${it.version} module: ${it.module}" }
        }

        return plugins
    }
}
