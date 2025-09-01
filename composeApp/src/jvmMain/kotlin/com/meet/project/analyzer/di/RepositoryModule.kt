package com.meet.project.analyzer.di

import com.meet.project.analyzer.data.repository.StorageAnalyzerRepository
import com.meet.project.analyzer.data.repository.StorageAnalyzerRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::StorageAnalyzerRepositoryImpl).bind(StorageAnalyzerRepository::class)
}