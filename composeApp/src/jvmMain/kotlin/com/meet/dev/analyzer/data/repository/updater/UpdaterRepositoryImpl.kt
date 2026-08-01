package com.meet.dev.analyzer.data.repository.updater

import com.meet.dev.analyzer.data.models.updater.GitHubRelease
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class UpdaterRepositoryImpl : UpdaterRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun checkForUpdates(): Result<GitHubRelease?> = withContext(Dispatchers.IO) {
        val client = HttpClient(CIO)
        try {
            val response = client.get("https://api.github.com/repos/Coding-Meet/DevAnalyzer/releases/latest") {
                // Add a user agent header so GitHub API doesn't reject us
                headers.append("User-Agent", "DevAnalyzer-Updater")
            }
            if (response.status.value == 200) {
                val bodyText = response.bodyAsText()
                val release = json.decodeFromString<GitHubRelease>(bodyText)
                Result.success(release)
            } else {
                Result.failure(Exception("HTTP Error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            client.close()
        }
    }
}
