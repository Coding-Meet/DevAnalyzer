package com.meet.dev.analyzer.utility.analytics

import com.posthog.kmp.PostHog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * PostHog-backed implementation of [AnalyticsManager].
 *
 * This is the ONLY file in the project that imports com.posthog.kmp.*.
 * All PostHog calls are wrapped in try/catch — analytics failures never crash the app.
 *
 * Opt-in/out is driven by [analyticsEnabledFlow] automatically via [distinctUntilChanged],
 * so no manual toggling is needed anywhere else in the codebase.
 */
class AnalyticsManagerImpl(
    analyticsEnabledFlow: Flow<Boolean>,
    private val analyticsScope: CoroutineScope,
) : AnalyticsManager {

    init {
        analyticsScope.launch {
            analyticsEnabledFlow
                .distinctUntilChanged() // avoid redundant optIn/optOut calls
                .collect { enabled ->
                    try {
                        if (enabled) PostHog.optIn() else PostHog.optOut()
                    } catch (_: Exception) {
                        // Analytics failures must never crash the app
                    }
                }
        }
    }

    override fun capture(event: AnalyticsEvent) {
        try {
            PostHog.capture(
                event = event.name,
                properties = event.toProperties(),
            )
        } catch (_: Exception) {
            // Analytics failures must never crash the app
        }
    }
}
