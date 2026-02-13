package com.meet.dev.analyzer.data.repository.setting

import com.meet.dev.analyzer.data.models.setting.LogFile
import com.meet.dev.analyzer.data.models.setting.PathSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val pathSettings: Flow<PathSettings>

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
