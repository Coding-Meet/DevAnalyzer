package com.meet.dev.analyzer.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.meet.dev.analyzer.data.models.setting.PathSettings
import com.meet.dev.analyzer.utility.getDefaultAndroidFolderPath
import com.meet.dev.analyzer.utility.getDefaultAndroidSdkPath
import com.meet.dev.analyzer.utility.getDefaultAvdLocationPath
import com.meet.dev.analyzer.utility.getDefaultGoogleFolderPaths
import com.meet.dev.analyzer.utility.getDefaultGradleHomePath
import com.meet.dev.analyzer.utility.getDefaultJdkFolderPaths
import com.meet.dev.analyzer.utility.getDefaultJetbrainsFolderPaths
import com.meet.dev.analyzer.utility.getDefaultKonanFolderPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PathPreferenceManger(private val dataStore: DataStore<Preferences>) {
    private object PreferencesKey {

        val SDK_PATH_KEY = stringPreferencesKey("sdk_path")
        val GRADLE_USER_HOME_PATH_KEY = stringPreferencesKey("gradle_user_home_path")
        val AVD_LOCATION_PATH_KEY = stringPreferencesKey("avd_location_path")
        val ANDROID_FOLDER_PATH_KEY = stringPreferencesKey("android_folder_path")
        val KONAN_FOLDER_PATH_KEY = stringPreferencesKey("konan_folder_path")

        val JDK_PATH_1_KEY = stringPreferencesKey("jdk_path_1")
        val JDK_PATH_2_KEY = stringPreferencesKey("jdk_path_2")
        val JDK_PATH_3_KEY = stringPreferencesKey("jdk_path_3")


        val IDE_JETBRAINS_1_KEY = stringPreferencesKey("ide_jetbrains_1")
        val IDE_JETBRAINS_2_KEY = stringPreferencesKey("ide_jetbrains_2")
        val IDE_JETBRAINS_3_KEY = stringPreferencesKey("ide_jetbrains_3")

        val IDE_GOOGLE_1_KEY = stringPreferencesKey("ide_google_1")
        val IDE_GOOGLE_2_KEY = stringPreferencesKey("ide_google_2")
        val IDE_GOOGLE_3_KEY = stringPreferencesKey("ide_google_3")

    }

    val pathSettings = dataStore.data.map { prefs ->

        val defaultJdks = getDefaultJdkFolderPaths()
        val defaultJetbrainsIde = getDefaultJetbrainsFolderPaths()
        val defaultGoogleIde = getDefaultGoogleFolderPaths()

        PathSettings(
            sdkPath = prefs[PreferencesKey.SDK_PATH_KEY] ?: getDefaultAndroidSdkPath(),
            gradleUserHomePath = prefs[PreferencesKey.GRADLE_USER_HOME_PATH_KEY]
                ?: getDefaultGradleHomePath(),
            avdLocationPath = prefs[PreferencesKey.AVD_LOCATION_PATH_KEY]
                ?: getDefaultAvdLocationPath(),
            androidFolderPath = prefs[PreferencesKey.ANDROID_FOLDER_PATH_KEY]
                ?: getDefaultAndroidFolderPath(),
            konanFolderPath = prefs[PreferencesKey.KONAN_FOLDER_PATH_KEY]
                ?: getDefaultKonanFolderPath(),

            jdkPath1 = prefs[PreferencesKey.JDK_PATH_1_KEY] ?: defaultJdks[0],
            jdkPath2 = prefs[PreferencesKey.JDK_PATH_2_KEY] ?: defaultJdks[1],
            jdkPath3 = prefs[PreferencesKey.JDK_PATH_3_KEY] ?: defaultJdks[2],

            ideJetBrains1 = prefs[PreferencesKey.IDE_JETBRAINS_1_KEY] ?: defaultJetbrainsIde[0],
            ideJetBrains2 = prefs[PreferencesKey.IDE_JETBRAINS_2_KEY] ?: defaultJetbrainsIde[1],
            ideJetBrains3 = prefs[PreferencesKey.IDE_JETBRAINS_3_KEY] ?: defaultJetbrainsIde[2],

            ideGoogle1 = prefs[PreferencesKey.IDE_GOOGLE_1_KEY] ?: defaultGoogleIde[0],
            ideGoogle2 = prefs[PreferencesKey.IDE_GOOGLE_2_KEY] ?: defaultGoogleIde[1],
            ideGoogle3 = prefs[PreferencesKey.IDE_GOOGLE_3_KEY] ?: defaultGoogleIde[2],
        )
    }

    val sdkPath = dataStore.data.map { prefs ->
        prefs[PreferencesKey.SDK_PATH_KEY] ?: getDefaultAndroidSdkPath()
    }
    val gradleUserHomePath = dataStore.data.map { prefs ->
        prefs[PreferencesKey.GRADLE_USER_HOME_PATH_KEY] ?: getDefaultGradleHomePath()
    }
    val avdLocationPath = dataStore.data.map { prefs ->
        prefs[PreferencesKey.AVD_LOCATION_PATH_KEY] ?: getDefaultAvdLocationPath()
    }
    val androidFolderPath = dataStore.data.map { prefs ->
        prefs[PreferencesKey.ANDROID_FOLDER_PATH_KEY] ?: getDefaultAndroidFolderPath()
    }
    val konanFolderPath = dataStore.data.map { prefs ->
        prefs[PreferencesKey.KONAN_FOLDER_PATH_KEY] ?: getDefaultKonanFolderPath()
    }
    private val defaultJdks = getDefaultJdkFolderPaths()
    val jdkPath1 = dataStore.data.map { prefs ->
        prefs[PreferencesKey.JDK_PATH_1_KEY] ?: defaultJdks[0]
    }
    val jdkPath2 = dataStore.data.map { prefs ->
        prefs[PreferencesKey.JDK_PATH_2_KEY] ?: defaultJdks[1]
    }
    val jdkPath3 = dataStore.data.map { prefs ->
        prefs[PreferencesKey.JDK_PATH_3_KEY] ?: defaultJdks[2]
    }

    private val defaultJetbrainsIde = getDefaultJetbrainsFolderPaths()
    val ideJetBrainsPath1 = dataStore.data.map { prefs ->
        prefs[PreferencesKey.IDE_JETBRAINS_1_KEY] ?: defaultJetbrainsIde[0]
    }
    val ideJetBrainsPath2 = dataStore.data.map { prefs ->
        prefs[PreferencesKey.IDE_JETBRAINS_2_KEY] ?: defaultJetbrainsIde[1]
    }
    val ideJetBrainsPath3 = dataStore.data.map { prefs ->
        prefs[PreferencesKey.IDE_JETBRAINS_3_KEY] ?: defaultJetbrainsIde[2]
    }

    private val defaultGoogleIde = getDefaultGoogleFolderPaths()
    val ideGooglePath1 = dataStore.data.map { prefs ->
        prefs[PreferencesKey.IDE_GOOGLE_1_KEY] ?: defaultGoogleIde[0]
    }
    val ideGooglePath2 = dataStore.data.map { prefs ->
        prefs[PreferencesKey.IDE_GOOGLE_2_KEY] ?: defaultGoogleIde[1]
    }
    val ideGooglePath3 = dataStore.data.map { prefs ->
        prefs[PreferencesKey.IDE_GOOGLE_3_KEY] ?: defaultGoogleIde[2]
    }

    suspend fun saveSdkPath(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.SDK_PATH_KEY] = path }
    }

    suspend fun saveGradleUserHomePath(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.GRADLE_USER_HOME_PATH_KEY] = path }
    }

    suspend fun saveAvdLocationPath(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.AVD_LOCATION_PATH_KEY] = path }
    }

    suspend fun saveAndroidFolderPath(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.ANDROID_FOLDER_PATH_KEY] = path }
    }

    suspend fun saveKonanFolderPath(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.KONAN_FOLDER_PATH_KEY] = path }
    }

    suspend fun saveJdkPath1(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.JDK_PATH_1_KEY] = path }
    }

    suspend fun saveJdkPath2(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.JDK_PATH_2_KEY] = path }
    }

    suspend fun saveJdkPath3(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.JDK_PATH_3_KEY] = path }
    }


    suspend fun saveIdeJetBrainsPath1(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.IDE_JETBRAINS_1_KEY] = path }
    }

    suspend fun saveIdeJetBrainsPath2(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.IDE_JETBRAINS_2_KEY] = path }
    }

    suspend fun saveIdeJetBrainsPath3(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.IDE_JETBRAINS_3_KEY] = path }
    }


    suspend fun saveIdeGooglePath1(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.IDE_GOOGLE_1_KEY] = path }
    }

    suspend fun saveIdeGooglePath2(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.IDE_GOOGLE_2_KEY] = path }
    }

    suspend fun saveIdeGooglePath3(path: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKey.IDE_GOOGLE_3_KEY] = path }
    }


}