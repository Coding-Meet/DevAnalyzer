package com.meet.dev.analyzer.utility.analytics

/**
 * Silent no-op implementation of [AnalyticsManager].
 *
 * Selected automatically by Koin when the PostHog API key is absent or blank.
 * The rest of the app is completely unaffected — it never knows PostHog is missing.
 */
class NoOpAnalyticsManager : AnalyticsManager {
    override fun capture(event: AnalyticsEvent) = Unit
}
