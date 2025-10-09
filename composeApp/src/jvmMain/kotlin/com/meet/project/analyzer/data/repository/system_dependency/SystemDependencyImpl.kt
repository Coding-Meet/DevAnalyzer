package com.meet.project.analyzer.data.repository.system_dependency

import com.meet.project.analyzer.core.utility.AppLogger
import com.meet.project.analyzer.core.utility.Utils
import com.meet.project.analyzer.core.utility.Utils.tagName
import com.meet.project.analyzer.data.models.scanner.BuildFileType
import com.meet.project.analyzer.data.models.scanner.ModuleBuildFileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SystemDependencyImpl : SystemDependency {
    private val TAG = tagName(javaClass)

    override suspend fun getAllDependencies(rootDirPath: String) {
        val rootDir = File(rootDirPath)
        val projectModules = findAllProjectModules(rootDir = rootDir)
    }

    data class ProjectModuleInfo(
        val projectName: String,
        val projectPath: String,
        val modules: List<ModuleBuildFileInfo>
    )

    private suspend fun findAllProjectModules(rootDir: File): List<ProjectModuleInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding project modules" }
            val projectModules = mutableListOf<ProjectModuleInfo>()

            rootDir.listFiles()?.filter { it.isDirectory && !it.name.startsWith(".") }
                ?.forEach { projectDir ->
                    val projectName = projectDir.name
                    val moduleBuildFileInfos = findModuleBuildFiles(projectDir = projectDir)
                    projectModules.add(
                        ProjectModuleInfo(
                            projectName = projectName,
                            projectPath = projectDir.absolutePath,
                            modules = moduleBuildFileInfos
                        )
                    )
                }
            AppLogger.d(TAG) { "Found ${projectModules.size} project modules" }
            projectModules.forEach {
                AppLogger.d(TAG) { "Project name: ${it.projectName} Path: ${it.projectPath}" }
                it.modules.forEach {
                    AppLogger.d(TAG) { "Module name: ${it.moduleName} Path: ${it.path}" }
                }
            }
            projectModules
        }

    private suspend fun findModuleBuildFiles(projectDir: File): List<ModuleBuildFileInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding build files" }

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
                            size = Utils.formatSize(sizeBytes),
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


            AppLogger.d(TAG) { "Found ${buildFiles.size} build files" }
            buildFiles.forEach {
                AppLogger.i(TAG) {
                    "name: ${it.type.fileName} Path: ${it.path} Type: ${it.type} Size: ${it.size} Size (bytes): ${it.sizeBytes} isContent: ${it.content.isNotEmpty()} moduleName = ${it.moduleName} modulePath = ${it.modulePath}"
                }
            }
            buildFiles
        }
}