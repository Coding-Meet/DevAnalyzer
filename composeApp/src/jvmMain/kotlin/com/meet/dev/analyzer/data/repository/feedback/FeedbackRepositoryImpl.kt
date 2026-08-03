package com.meet.dev.analyzer.data.repository.feedback

import com.meet.dev.analyzer.data.models.feedback.FeedbackData
import com.meet.dev.analyzer.utility.platform.DesktopConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedbackRepositoryImpl(
    private val appConfig: DesktopConfig,
) : FeedbackRepository {

    override suspend fun submitFeedback(feedback: FeedbackData): Result<Unit> =
        withContext(Dispatchers.IO) {
            val client = HttpClient(CIO)
            try {
                val response = client.submitForm(
                    url = appConfig.feedbackUrl,
                    formParameters = Parameters.build {
                        append("entry.1761072179", feedback.rating.toString())
                        append("entry.1087708121", feedback.name)
                        append("entry.1559444738", feedback.likesAndImprovements)

                        feedback.mostUsedFeatures.forEach { feature ->
                            append("entry.1144468351", feature)
                        }

                        append("entry.929779127", feedback.futureFeatures)
                        append("entry.1500692418", feedback.systemInfo)
                        append("entry.1169610342", feedback.email)
                    }
                )

                if (response.status.value in 200..299) {
                    Result.success(Unit)
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
