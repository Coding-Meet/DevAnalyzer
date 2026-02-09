package com.meet.dev.analyzer.di

import com.meet.dev.analyzer.utility.platform.AppEnvironment
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    appEnvironment: AppEnvironment,
    appDeclaration: KoinAppDeclaration = {}
) =
    startKoin {
        appDeclaration()
        modules(
            coreModule,
            getLoggingModule(
                appEnvironment = appEnvironment,
            ),
            repositoryModule,
            viewModule
        )
    }