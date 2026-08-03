package com.meet.dev.analyzer.data.repository.feedback

import com.meet.dev.analyzer.data.models.feedback.FeedbackData

interface FeedbackRepository {
    suspend fun submitFeedback(feedback: FeedbackData): Result<Unit>
}
