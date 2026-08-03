package com.meet.dev.analyzer.data.models.feedback

data class FeedbackData(
    val rating: Int,
    val name: String,
    val likesAndImprovements: String,
    val mostUsedFeatures: List<String>,
    val futureFeatures: String,
    val systemInfo: String,
    val email: String
)
