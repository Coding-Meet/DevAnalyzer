package com.meet.project.analyzer.data.models.scanner

import com.meet.project.analyzer.data.models.storage.GradleLibraryInfo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class VersionCatalog(
    val versions: List<Version> = emptyList(),
    val libraries: List<Library> = emptyList(),
    val plugins: List<Plugin> = emptyList(),
    val bundles: List<Bundle> = emptyList()
)

data class Version(
    val name: String, // ex: androidx-core-ktx
    val version: String?, // ex: 1.12.0
)

data class Library(
    val id: String,  // ex: com.google.android.material:material
    val name: String, // ex: androidx-material
    val group: String?, // ex: com.google.android.material
    val libName: String?, // ex: material
    val version: String?, // ex: 1.12.0
)

@OptIn(ExperimentalUuidApi::class)
data class Plugin(
    val uniqueId: String = Uuid.random().toString(),
    val name: String, // ex: com.android.application.gradle.plugin
    val id: String, // ex: com.android.application:com.android.application.gradle.plugin
    val group: String, // ex: com.android.application
    val version: String?, // version in project ex: 8.7.1
    val module: String, // ex: root, sub-module like app, core, etc.
    val configuration: String,    // ex: normal, classpath, versionCatalog
    val isVersionSynced: Boolean = false, // whether currentVersion exists in Gradle list
    val availableGradleVersions: GradleLibraryInfo? = null  // versions fetched from locally in the Gradle cache
)

data class Bundle(
    val name: String, // ex: androidx-bundles
    val artifacts: List<String>  // ex: [androidx-core-ktx, androidx-appcompat-resources ..]
)

@OptIn(ExperimentalUuidApi::class)
data class Dependency(
    val uniqueId: String = Uuid.random().toString(),
    val versionName: String, // ex: androidx-core-ktx
    val name: String,      // ex: material
    val id: String,        // ex: com.google.android.material:material
    val group: String,     // ex: com.google.android.material
    val version: String?,   // version in project ex: 1.11.0
    val module: String,    // ex: sub-module like app, core, etc.
    val configuration: String,       // ex: implementation, api, compileOnly, runtimeOnly, testImplementation, androidTestImplementation
    val isVersionSynced: Boolean = false, // whether currentVersion exists in Gradle list
    val availableGradleVersions: GradleLibraryInfo? = null // versions fetched from locally in the Gradle cache
)