package com.meet.project.analyzer.di


import com.meet.project.analyzer.core.utility.AppLogger
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        AppLogger.d(tag = "Koin", message = { "🔥 Initializing Koin" })
        appDeclaration()
        modules(
            coreModule,
            repositoryModule,
            viewModule
        )
        AppLogger.d(tag = "Koin", message = { "🔥 Koin initialized" })
    }