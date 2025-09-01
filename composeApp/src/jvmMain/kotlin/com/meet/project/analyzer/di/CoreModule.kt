package com.meet.project.analyzer.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import org.koin.dsl.module
import java.io.File

val coreModule = module {
    // DataStore
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            File(System.getProperty("user.home"), ".project_analyzer/preferences.preferences_pb")
        }
    }

}