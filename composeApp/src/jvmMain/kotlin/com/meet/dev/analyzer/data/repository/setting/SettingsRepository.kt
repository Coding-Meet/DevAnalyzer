package com.meet.dev.analyzer.data.repository.setting

import com.meet.dev.analyzer.data.models.setting.LogFile
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    /* ---------- Paths Flow ---------- */

    val sdkPath: Flow<String>
    val gradleUserHomePath: Flow<String>
    val avdLocationPath: Flow<String>
    val androidFolderPath: Flow<String>
    val konanFolderPath: Flow<String>

    val jdkPath1: Flow<String>
    val jdkPath2: Flow<String>
    val jdkPath3: Flow<String>

    val ideJetBrainsPath1: Flow<String>
    val ideJetBrainsPath2: Flow<String>
    val ideJetBrainsPath3: Flow<String>

    val ideGooglePath1: Flow<String>
    val ideGooglePath2: Flow<String>
    val ideGooglePath3: Flow<String>

    val crashReportingEnabled: Flow<Boolean>
    val localLogsEnabled: Flow<Boolean>

    /* ---------- Save Paths ---------- */

    suspend fun saveSdkPath(path: String)
    suspend fun saveGradleUserHomePath(path: String)
    suspend fun saveAvdLocationPath(path: String)
    suspend fun saveAndroidFolderPath(path: String)
    suspend fun saveKonanFolderPath(path: String)

    suspend fun saveJdkPath1(path: String)
    suspend fun saveJdkPath2(path: String)
    suspend fun saveJdkPath3(path: String)

    suspend fun saveIdeJetBrainsPath1(path: String)
    suspend fun saveIdeJetBrainsPath2(path: String)
    suspend fun saveIdeJetBrainsPath3(path: String)

    suspend fun saveIdeGooglePath1(path: String)
    suspend fun saveIdeGooglePath2(path: String)
    suspend fun saveIdeGooglePath3(path: String)

    /* ---------- App Settings ---------- */

    suspend fun setCrashReporting(enabled: Boolean)
    suspend fun setLocalLogs(enabled: Boolean)

    fun getLatestLogFile(): LogFile?
}
