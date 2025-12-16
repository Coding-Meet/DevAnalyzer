package com.meet.dev.analyzer.data.repository.cleanbuild

import com.meet.dev.analyzer.core.utility.AppLogger
import com.meet.dev.analyzer.core.utility.Utils.calculateFolderSize
import com.meet.dev.analyzer.core.utility.Utils.formatSize
import com.meet.dev.analyzer.core.utility.Utils.tagName
import com.meet.dev.analyzer.data.models.cleanbuild.ModuleBuild
import com.meet.dev.analyzer.data.models.cleanbuild.ProjectBuildInfo
import com.meet.dev.analyzer.data.models.project.BuildFileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

class CleanBuildRepositoryImpl : CleanBuildRepository {

    private val TAG = tagName(javaClass = javaClass)

    override suspend fun scanProjects(
        rootPath: String,
        updateProgress: (progress: Float, status: String) -> Unit
    ): List<ProjectBuildInfo> = withContext(Dispatchers.IO) {

        AppLogger.i(TAG) { "Scanning projects..." }

        try {
            val rootDir = File(rootPath)
            val projectDirs = rootDir.listFiles()?.filter { it.isDirectory }.orEmpty()
            val totalProjects = projectDirs.size.coerceAtLeast(1)

            val projects = mutableListOf<ProjectBuildInfo>()
            var scannedCount = 0

            projectDirs.forEach { projectDir ->

                updateProgress(
                    scannedCount.toFloat() / totalProjects,
                    "Scanning ${projectDir.name}"
                )

                // Detect Gradle project
                val isGradleProject = BuildFileType.entries.any {
                    File(projectDir, it.fileName).exists()
                }
                if (!isGradleProject) {
                    scannedCount++
                    return@forEach
                }

                val modules = mutableListOf<ModuleBuild>()

                // Root build
                val rootBuildDir = File(projectDir, "build")
                if (rootBuildDir.exists()) {
                    val size = calculateFolderSize(rootBuildDir, false)
                    modules.add(
                        ModuleBuild(
                            moduleName = projectDir.name,
                            projectName = null,
                            path = rootBuildDir.absolutePath,
                            sizeBytes = size,
                            sizeFormatted = formatSize(size)
                        )
                    )
                }

                // Module builds
                projectDir.listFiles()?.forEach { moduleDir ->
                    val buildDir = File(moduleDir, "build")
                    if (buildDir.exists()) {
                        val size = calculateFolderSize(buildDir, false)
                        modules.add(
                            ModuleBuild(
                                moduleName = moduleDir.name,
                                projectName = projectDir.name,
                                path = buildDir.absolutePath,
                                sizeBytes = size,
                                sizeFormatted = formatSize(size)
                            )
                        )
                    }
                }

                if (modules.isNotEmpty()) {
                    val totalSize = modules.sumOf { it.sizeBytes }
                    projects.add(
                        ProjectBuildInfo(
                            projectName = projectDir.name,
                            projectPath = projectDir.absolutePath,
                            modules = modules.sortedByDescending { it.sizeBytes },
                            sizeBytes = totalSize,
                            sizeFormatted = formatSize(totalSize)
                        )
                    )
                }

                scannedCount++
            }

            updateProgress(1f, "Scan completed")
            projects.sortedByDescending { it.sizeBytes }

        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Error scanning projects" }
            emptyList()
        }
    }


    override suspend fun deleteBuildFolder(path: String): Boolean {
//        delay(500)
//        return try {
//            val file = File(path)
//            if (file.exists() && file.isDirectory) {
//                file.deleteRecursively()
//            } else {
//                false
//            }
//        } catch (e: Exception) {
//            AppLogger.e(TAG, e) { "Error deleting folder: $path" }
//            false
//        }
        return Random.nextBoolean()
    }
}
