package com.meet.dev.analyzer.utility.analytics

/**
 * Holds PostHog connection parameters read from props.properties.
 * A blank [apiKey] means analytics is unavailable and [NoOpAnalyticsManager] is used.
 */
data class AnalyticsConfig(
    val apiKey: String,
    val host: String,
)
