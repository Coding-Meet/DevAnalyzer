package com.meet.project.analyzer.di

import com.meet.project.analyzer.presentation.screen.storage.StorageAnalyzerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::StorageAnalyzerViewModel)
}