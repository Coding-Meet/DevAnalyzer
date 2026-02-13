package com.meet.dev.analyzer.data.repository.setting

import com.meet.dev.analyzer.data.datastore.AppPreferenceManager
import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.data.models.setting.LogFile
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import java.io.File

class SettingsRepositoryImpl(
    private val pathPreferenceManger: PathPreferenceManger,
    private val appPreferenceManager: AppPreferenceManager
) : SettingsRepository {

    override val pathSettings = pathPreferenceManger.pathSettings

    override val crashReportingEnabled = appPreferenceManager.crashReportingEnabled
    override val localLogsEnabled = appPreferenceManager.isLocalLogsEnabled

    override suspend fun saveSdkPath(path: String) {
        pathPreferenceManger.saveSdkPath(path)
    }

    override suspend fun saveGradleUserHomePath(path: String) {
        pathPreferenceManger.saveGradleUserHomePath(path)
    }

    override suspend fun saveAvdLocationPath(path: String) {
        pathPreferenceManger.saveAvdLocationPath(path)
    }

    override suspend fun saveAndroidFolderPath(path: String) {
        pathPreferenceManger.saveAndroidFolderPath(path)
    }

    override suspend fun saveKonanFolderPath(path: String) {
        pathPreferenceManger.saveKonanFolderPath(path)
    }

    override suspend fun saveJdkPath1(path: String) {
        pathPreferenceManger.saveJdkPath1(path)
    }

    override suspend fun saveJdkPath2(path: String) {
        pathPreferenceManger.saveJdkPath2(path)
    }

    override suspend fun saveJdkPath3(path: String) {
        pathPreferenceManger.saveJdkPath3(path)
    }

    override suspend fun saveIdeJetBrainsPath1(path: String) {
        pathPreferenceManger.saveIdeJetBrainsPath1(path)
    }

    override suspend fun saveIdeJetBrainsPath2(path: String) {
        pathPreferenceManger.saveIdeJetBrainsPath2(path)
    }

    override suspend fun saveIdeJetBrainsPath3(path: String) {
        pathPreferenceManger.saveIdeJetBrainsPath3(path)
    }

    override suspend fun saveIdeGooglePath1(path: String) {
        pathPreferenceManger.saveIdeGooglePath1(path)
    }

    override suspend fun saveIdeGooglePath2(path: String) {
        pathPreferenceManger.saveIdeGooglePath2(path)
    }

    override suspend fun saveIdeGooglePath3(path: String) {
        pathPreferenceManger.saveIdeGooglePath3(path)
    }

    override suspend fun setCrashReporting(enabled: Boolean) {
        appPreferenceManager.saveCrashReportingEnabled(enabled)
    }

    override suspend fun setLocalLogs(enabled: Boolean) {
        appPreferenceManager.saveLocalLogsEnabled(enabled)
    }

    override fun getLatestLogFile(): LogFile? {
        return try {
            val dir = File(System.getProperty("user.home"), ".dev_analyzer")

            val latestFile = dir.listFiles { f -> f.extension == "log" }
                ?.maxByOrNull { it.lastModified() }
                ?: return null

            val content = latestFile.readText()
            val totalSizeBytes = FolderFileUtils.calculateFolderSize(latestFile)
            LogFile(
                name = latestFile.name,
                path = latestFile.absolutePath,
                sizeReadable = FolderFileUtils.formatSize(totalSizeBytes),
                totalSizeBytes = totalSizeBytes,
                lines = content.lines().size,
                timestamp = latestFile.lastModified(),
                content = content,
                totalCharacters = content.length
            )
        } catch (e: Exception) {
            null
        }
    }
}
