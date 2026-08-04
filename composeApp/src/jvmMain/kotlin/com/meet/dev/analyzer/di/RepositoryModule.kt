package com.meet.dev.analyzer.di

import com.meet.dev.analyzer.data.repository.cleanbuild.CleanBuildRepository
import com.meet.dev.analyzer.data.repository.cleanbuild.CleanBuildRepositoryImpl
import com.meet.dev.analyzer.data.repository.feedback.FeedbackRepository
import com.meet.dev.analyzer.data.repository.feedback.FeedbackRepositoryImpl
import com.meet.dev.analyzer.data.repository.project.ProjectAnalyzerRepository
import com.meet.dev.analyzer.data.repository.project.ProjectAnalyzerRepositoryImpl
import com.meet.dev.analyzer.data.repository.project.helpers.DependencyAnalyzer
import com.meet.dev.analyzer.data.repository.project.helpers.PluginAnalyzer
import com.meet.dev.analyzer.data.repository.project.helpers.ProjectFileScanner
import com.meet.dev.analyzer.data.repository.project.helpers.ProjectOverviewAnalyzer
import com.meet.dev.analyzer.data.repository.project.helpers.VersionCatalogParser
import com.meet.dev.analyzer.data.repository.setting.SettingsRepository
import com.meet.dev.analyzer.data.repository.setting.SettingsRepositoryImpl
import com.meet.dev.analyzer.data.repository.storage.StorageAnalyzerRepository
import com.meet.dev.analyzer.data.repository.storage.StorageAnalyzerRepositoryImpl
import com.meet.dev.analyzer.data.repository.storage.helpers.AndroidSdkAnalyzer
import com.meet.dev.analyzer.data.repository.storage.helpers.AvdAnalyzer
import com.meet.dev.analyzer.data.repository.storage.helpers.GradleAnalyzer
import com.meet.dev.analyzer.data.repository.storage.helpers.IdeAnalyzer
import com.meet.dev.analyzer.data.repository.storage.helpers.KonanAnalyzer
import com.meet.dev.analyzer.data.repository.updater.UpdaterRepository
import com.meet.dev.analyzer.data.repository.updater.UpdaterRepositoryImpl
import com.meet.dev.analyzer.data.repository.workspace.WorkspaceRepository
import com.meet.dev.analyzer.data.repository.workspace.WorkspaceRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {

    // Storage Analyzers Helper classes
    singleOf(::IdeAnalyzer)
    singleOf(::KonanAnalyzer)
    singleOf(::AvdAnalyzer)
    singleOf(::AndroidSdkAnalyzer)
    singleOf(::GradleAnalyzer)

    // Project Analyzer Helper classes
    singleOf(::ProjectFileScanner)
    singleOf(::VersionCatalogParser)
    singleOf(::DependencyAnalyzer)
    singleOf(::PluginAnalyzer)
    singleOf(::ProjectOverviewAnalyzer)

    // Main Repositories
    singleOf(::ProjectAnalyzerRepositoryImpl).bind(ProjectAnalyzerRepository::class)
    singleOf(::StorageAnalyzerRepositoryImpl).bind(StorageAnalyzerRepository::class)
    singleOf(::CleanBuildRepositoryImpl).bind(CleanBuildRepository::class)
    singleOf(::SettingsRepositoryImpl).bind(SettingsRepository::class)
    singleOf(::WorkspaceRepositoryImpl).bind(WorkspaceRepository::class)
    singleOf(::FeedbackRepositoryImpl).bind(FeedbackRepository::class)
    singleOf(::UpdaterRepositoryImpl).bind(UpdaterRepository::class)

}
