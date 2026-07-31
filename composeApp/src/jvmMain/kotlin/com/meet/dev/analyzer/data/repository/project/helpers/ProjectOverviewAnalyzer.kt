package com.meet.dev.analyzer.data.repository.project.helpers

import com.meet.dev.analyzer.data.models.project.*
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import java.io.File

class ProjectOverviewAnalyzer {
    private val tag = tagName(javaClass = javaClass)

    fun findProjectOverviewInfo(
        projectDir: File,
        settingsGradleFileInfo: SettingsGradleFileInfo?,
        gradleWrapperPropertiesFileInfo: GradleWrapperPropertiesFileInfo?,
        versionCatalog: VersionCatalog?,
        moduleBuildFileInfos: List<ModuleBuildFileInfo>
    ): ProjectOverviewInfo {
        AppLogger.d(tag = tag) { "Finding project info" }

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
            val newFormatRegex = Regex(
                """compileSdk\s*\{\s*(?:[^{}]*)\bversion\s*=\s*release\((\d+)\)""",
                RegexOption.DOT_MATCHES_ALL
            )

            val subModuleBuildFileInfo = moduleBuildFileInfos.find {
                regex.containsMatchIn(it.content) || newFormatRegex.containsMatchIn(it.content)
            }
            return subModuleBuildFileInfo?.let {
                regex.find(it.content)?.groupValues?.get(1)
                    ?: newFormatRegex.find(it.content)?.groupValues?.get(1)
            }
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

        fun extractBuildToolsSdk(): String? {
            val buildToolsRegex = Regex("""buildToolsVersion\s*=?\s*(["'])?(\d+(?:\.\d+){0,2})\1""")
            val subModuleBuildFileInfo =
                moduleBuildFileInfos.find { buildToolsRegex.containsMatchIn(it.content) }
            return subModuleBuildFileInfo
                ?.let { buildToolsRegex.find(it.content)?.groupValues?.get(2) }
                ?: extractCompileSdk()?.let { "$it.0.0" }
        }

        fun extractNdkVersion(): String? {
            val regex = Regex("""ndkVersion\s*=?\s*["']?(\d+\.\d+\.\d+)["']?""")
            val subModuleBuildFileInfo =
                moduleBuildFileInfos.find { regex.containsMatchIn(it.content) }
            return subModuleBuildFileInfo?.let { regex.find(it.content)?.groupValues?.get(1) }
        }

        fun extractCmakeVersion(): String? {
            val blockRegex = Regex("""cmake\s*\{([^}]*)\}""", RegexOption.DOT_MATCHES_ALL)
            val versionRegex = Regex("""\bversion\s*=?\s*["']?(\d+\.\d+\.\d+)["']?""")

            val buildFile = moduleBuildFileInfos.firstOrNull()?.content ?: return null

            val blockMatch = blockRegex.find(buildFile) ?: return null
            val blockContent = blockMatch.groupValues[1]

            return versionRegex.find(blockContent)?.groupValues?.get(1)
        }

        fun getPlatforms(): List<String> {
            val platforms = mutableSetOf<String>()

            val androidRegex = Regex("""\bandroid(Target)?\s*([({])""")
            val jvmRegex = Regex("""\bjvm\s*([({])""")
            val jsRegex = Regex("""\bjs\s*([({])""")
            val wasmRegex = Regex("""\bwasm(Js)?\s*([({])""")
            val iosRegex = Regex("""\bios(Arm64|X64|SimulatorArm64)?\s*([({])""")

            // Server frameworks / plugins
            val serverFrameworkRegex = Regex(
                """ktor|springframework|spring-boot|micronaut|vertx""",
                RegexOption.IGNORE_CASE
            )

            // Compose Desktop exclusion
            val composeDesktopRegex =
                Regex("""org\.jetbrains\.compose\.desktop""")

            var hasJvm = false
            var hasServerFramework = false
            var isComposeDesktop = false
            moduleBuildFileInfos.forEach { file ->
                val content = file.content

                if (androidRegex.containsMatchIn(content)) platforms.add("Android")
                if (jvmRegex.containsMatchIn(content)) {
                    platforms.add("JVM")
                    hasJvm = true
                }
                if (jsRegex.containsMatchIn(content)) platforms.add("JS")
                if (wasmRegex.containsMatchIn(content)) platforms.add("Wasm")
                if (iosRegex.containsMatchIn(content)) platforms.add("iOS")

                if (serverFrameworkRegex.containsMatchIn(content)) hasServerFramework = true
                if (composeDesktopRegex.containsMatchIn(content)) isComposeDesktop = true
            }
            // SERVER = JVM + Backend framework - Compose Desktop
            if (hasJvm && hasServerFramework && !isComposeDesktop) {
                platforms.add("Server")
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
            buildToolsSdk = extractBuildToolsSdk(),
            targetSdkVersion = extractTargetSdk(),
            minSdkVersion = extractMinSdk(),
            compileSdkVersion = extractCompileSdk(),
            ndkVersion = extractNdkVersion(),
            cmakeVersion = extractCmakeVersion(),
            platformList = getPlatforms()
        )
        AppLogger.d(tag = tag) { "Found project info." }
        AppLogger.i(tag = tag) {
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
}
