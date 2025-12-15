package com.meet.dev.analyzer.di

import com.meet.dev.analyzer.data.repository.cleanbuild.CleanBuildRepository
import com.meet.dev.analyzer.data.repository.cleanbuild.CleanBuildRepositoryImpl
import com.meet.dev.analyzer.data.repository.project.ProjectAnalyzerRepository
import com.meet.dev.analyzer.data.repository.project.ProjectAnalyzerRepositoryImpl
import com.meet.dev.analyzer.data.repository.storage.StorageAnalyzerRepository
import com.meet.dev.analyzer.data.repository.storage.StorageAnalyzerRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {

    singleOf(::ProjectAnalyzerRepositoryImpl).bind(ProjectAnalyzerRepository::class)
    singleOf(::StorageAnalyzerRepositoryImpl).bind(StorageAnalyzerRepository::class)
    singleOf(::CleanBuildRepositoryImpl).bind(CleanBuildRepository::class)

}

