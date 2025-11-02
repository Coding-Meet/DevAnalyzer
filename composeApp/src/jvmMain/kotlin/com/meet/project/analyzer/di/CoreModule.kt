package com.meet.project.analyzer.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.meet.project.analyzer.data.datastore.AppPreferenceManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

val coreModule = module {
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

}