package com.meet.dev.analyzer.data.repository.project

import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.data.models.project.ProjectInfo
import com.meet.dev.analyzer.data.repository.project.helpers.DependencyAnalyzer
import com.meet.dev.analyzer.data.repository.project.helpers.PluginAnalyzer
import com.meet.dev.analyzer.data.repository.project.helpers.ProjectFileScanner
import com.meet.dev.analyzer.data.repository.project.helpers.ProjectOverviewAnalyzer
import com.meet.dev.analyzer.data.repository.project.helpers.VersionCatalogParser
import com.meet.dev.analyzer.data.repository.storage.helpers.GradleAnalyzer
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProjectAnalyzerRepositoryImpl(
    private val fileScanner: ProjectFileScanner,
    private val versionCatalogParser: VersionCatalogParser,
    private val dependencyAnalyzer: DependencyAnalyzer,
    private val pluginAnalyzer: PluginAnalyzer,
    private val projectOverviewAnalyzer: ProjectOverviewAnalyzer,
    private val gradleAnalyzer: GradleAnalyzer
) : ProjectAnalyzerRepository {

    private val TAG = tagName(javaClass = javaClass)

    override suspend fun analyzeProject(
        projectPath: String, updateProgress: (progress: Float, status: String) -> Unit
    ): ProjectInfo = withContext(Dispatchers.IO) {
        AppLogger.i(tag = TAG) { "Starting project analysis for: $projectPath" }

        val projectDir = File(projectPath)

        updateProgress(0.1f, "Finding build files...")

        val moduleBuildFileInfos =
            fileScanner.findModuleBuildFiles(projectDir = projectDir)

        val settingsGradleFileInfo =
            fileScanner.findSettingsGradleFiles(projectDir = projectDir)

        val propertiesFileInfo =
            fileScanner.findPropertiesFiles(projectDir = projectDir)

        val gradleWrapperPropertiesFileInfo =
            fileScanner.findGradleWrapperProFile(projectDir = projectDir)

        val versionCatalogFileInfo =
            fileScanner.findVersionCatalogFile(projectDir = projectDir)

        updateProgress(0.3f, "Analyzing modules...")
        val versionCatalog =
            versionCatalogParser.findVersionCatalog(versionCatalogFileInfo = versionCatalogFileInfo)

        val projectOverviewInfo =
            projectOverviewAnalyzer.findProjectOverviewInfo(
                projectDir = projectDir,
                settingsGradleFileInfo = settingsGradleFileInfo,
                gradleWrapperPropertiesFileInfo = gradleWrapperPropertiesFileInfo,
                versionCatalog = versionCatalog,
                moduleBuildFileInfos = moduleBuildFileInfos
            )

        updateProgress(0.5f, "Analyzing plugins...")
        val gradleModulesInfo = gradleAnalyzer.getGradleModulesInfo()
        val plugins =
            pluginAnalyzer.findPlugin(
                moduleBuildFileInfos = moduleBuildFileInfos,
                versionCatalog = versionCatalog,
                gradleModulesInfo = gradleModulesInfo,
            )

        updateProgress(0.7f, "Analyzing dependencies...")
        val dependencies =
            dependencyAnalyzer.findDependencies(
                moduleBuildFileInfos = moduleBuildFileInfos,
                versionCatalog = versionCatalog,
                gradleModulesInfo = gradleModulesInfo
            )
        val modulesWithDependency =
            dependencyAnalyzer.addDependencyEachModule(
                moduleBuildFileInfos = moduleBuildFileInfos,
                plugins = plugins,
                dependencies = dependencies
            )
        updateProgress(0.8f, "Building project info...")

        val projectFiles = fileScanner.findProjectFiles(projectDir)

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
}
