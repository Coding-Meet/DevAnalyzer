package com.meet.dev.analyzer.di

import com.meet.dev.analyzer.utility.analytics.AnalyticsConfig
import com.meet.dev.analyzer.utility.platform.DesktopConfig
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    appConfig: DesktopConfig,
    analyticsConfig: AnalyticsConfig,
    appDeclaration: KoinAppDeclaration = {}
) =
    startKoin {
        appDeclaration()
        modules(
            coreModule(appConfig = appConfig, analyticsConfig = analyticsConfig),
            repositoryModule,
            viewModule
        )
    }