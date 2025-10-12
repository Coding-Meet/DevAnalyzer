package com.meet.project.analyzer.di

import com.meet.project.analyzer.data.repository.scanner.ProjectScannerRepository
import com.meet.project.analyzer.data.repository.scanner.ProjectScannerRepositoryImpl
import com.meet.project.analyzer.data.repository.storage.StorageAnalyzerRepository
import com.meet.project.analyzer.data.repository.storage.StorageAnalyzerRepositoryImpl
import com.meet.project.analyzer.data.repository.system_dependency.SystemDependencyRepository
import com.meet.project.analyzer.data.repository.system_dependency.SystemDependencyRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {

    singleOf(::ProjectScannerRepositoryImpl).bind(ProjectScannerRepository::class)
    singleOf(::StorageAnalyzerRepositoryImpl).bind(StorageAnalyzerRepository::class)
    singleOf(::SystemDependencyRepositoryImpl).bind(SystemDependencyRepository::class)

}

