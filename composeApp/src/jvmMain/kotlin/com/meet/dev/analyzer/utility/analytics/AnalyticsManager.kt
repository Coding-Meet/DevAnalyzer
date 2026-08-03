package com.meet.dev.analyzer.utility.analytics

/**
 * Analytics abstraction used throughout the app.
 *
 * ViewModels depend only on this interface — never on PostHog directly.
 * The rest of the app must never import com.posthog.*.
 */
interface AnalyticsManager {
    fun capture(event: AnalyticsEvent)
}
