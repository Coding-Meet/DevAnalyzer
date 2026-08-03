package com.meet.dev.analyzer.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager
import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.utility.analytics.AnalyticsConfig
import com.meet.dev.analyzer.utility.analytics.AnalyticsManager
import com.meet.dev.analyzer.utility.analytics.AnalyticsManagerImpl
import com.meet.dev.analyzer.utility.analytics.NoOpAnalyticsManager
import com.meet.dev.analyzer.utility.crash_report.FileLogWriter
import com.meet.dev.analyzer.utility.crash_report.SentryLogWriter
import com.meet.dev.analyzer.utility.platform.DesktopConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

fun coreModule(
    appConfig: DesktopConfig,
    analyticsConfig: AnalyticsConfig,
) = module {
    single<DesktopConfig> { appConfig }

    // DataStore
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            File(
                System.getProperty("user.home"),
                ".dev_analyzer" + File.separator + "preferences.preferences_pb"
            )
        }
    }
    singleOf(::AppPreferenceManager)
    singleOf(::PathPreferenceManger)

    // logging
    single<SentryLogWriter> { SentryLogWriter() }
    single<FileLogWriter> { FileLogWriter() }

    // Analytics — scope lives for the duration of the app; cancelled in onCloseRequest
    single<CoroutineScope>(named("analyticsScope")) {
        CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    single<AnalyticsManager> {
        if (analyticsConfig.apiKey.isBlank()) {
            NoOpAnalyticsManager()
        } else {
            AnalyticsManagerImpl(
                analyticsEnabledFlow = get<AppPreferenceManager>().analyticsEnabled,
                analyticsScope = get(named("analyticsScope"))
            )
        }
    }
}