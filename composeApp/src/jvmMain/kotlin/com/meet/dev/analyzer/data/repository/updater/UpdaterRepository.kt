package com.meet.dev.analyzer.data.repository.updater

import com.meet.dev.analyzer.data.models.updater.GitHubRelease

interface UpdaterRepository {
    suspend fun checkForUpdates(): Result<GitHubRelease?>
}
