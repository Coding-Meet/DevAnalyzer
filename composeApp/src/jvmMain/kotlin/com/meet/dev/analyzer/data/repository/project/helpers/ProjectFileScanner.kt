package com.meet.dev.analyzer.data.repository.project.helpers

import com.meet.dev.analyzer.data.models.project.*
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ProjectFileScanner {
    private val tag = tagName(javaClass = javaClass)

    suspend fun findGradleProjects(rootDir: File): List<File> = withContext(Dispatchers.IO) {
        rootDir.listFiles()
            ?.filter { it.isDirectory && !it.name.startsWith(".") }
            ?.filter { dir -> findModuleBuildFiles(dir).isNotEmpty() }
            ?: emptyList()
    }

    suspend fun findVersionCatalogFile(projectDir: File): VersionCatalogFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(tag = tag) { "Finding version catalog" }

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
            AppLogger.d(tag = tag) { "Found version catalog." }
            AppLogger.i(tag = tag) {
                "Name: ${versionCatalogFileInfo.name} Path: ${versionCatalogFileInfo.path} Size: ${versionCatalogFileInfo.sizeReadable} Size (bytes): ${versionCatalogFileInfo.sizeBytes} isContent: ${versionCatalogFileInfo.content.isNotEmpty()}"
            }
            versionCatalogFileInfo
        }

    suspend fun findModuleBuildFiles(projectDir: File): List<ModuleBuildFileInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.d(tag = tag) { "Finding build files" }

            val moduleDirs = projectDir.walkTopDown()
                .maxDepth(4)
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
                            isRootBuildFile = moduleDir == projectDir,
                            modulePath = moduleDir.absolutePath
                        )
                    } else null
                }
            }

            AppLogger.d(tag = tag) { "Found ${buildFiles.size} build files" }
            buildFiles.forEach {
                AppLogger.i(tag = tag) {
                    "name: ${it.type.fileName} Path: ${it.path} Type: ${it.type} Size: ${it.sizeReadable} Size (bytes): ${it.sizeBytes} isContent: ${it.content.isNotEmpty()} moduleName = ${it.moduleName} modulePath = ${it.modulePath}"
                }
            }
            buildFiles
        }


    suspend fun findSettingsGradleFiles(projectDir: File): SettingsGradleFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(tag = tag) { "Finding settings gradle files" }

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

            AppLogger.d(tag = tag) { "Found settings.gradle files" }
            AppLogger.i(tag = tag) {
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

    suspend fun findPropertiesFiles(projectDir: File): PropertiesFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(tag = tag) { "Finding properties files" }
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
            AppLogger.d(tag = tag) { "Found properties files" }
            AppLogger.i(tag = tag) {
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

    suspend fun findGradleWrapperProFile(projectDir: File): GradleWrapperPropertiesFileInfo? =
        withContext(Dispatchers.IO) {
            AppLogger.d(tag = tag) { "Finding gradle wrapper properties file" }

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
                file = gradleWrapperPropertiesFile
            )

            AppLogger.d(tag = tag) { "Found version catalog." }
            AppLogger.i(tag = tag) {
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

    @OptIn(ExperimentalUuidApi::class)
    suspend fun findProjectFiles(projectDir: File): List<ProjectFileInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.d(tag = tag) { "Finding project files in: ${projectDir.absolutePath}" }

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
                                tag = tag,
                                throwable = e
                            ) { "Error processing file: ${file.absolutePath}" }
                        }
                    }
            } catch (e: Exception) {
                AppLogger.e(tag = tag, throwable = e) { "Error walking project directory" }
            }

            AppLogger.d(tag = tag) { "Found ${projectFiles.size} project files" }
            projectFiles.sortedBy { it.relativePath }
        }
}
