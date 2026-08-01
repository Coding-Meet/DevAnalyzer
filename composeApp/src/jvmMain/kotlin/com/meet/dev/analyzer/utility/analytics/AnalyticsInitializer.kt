package com.meet.dev.analyzer.utility.analytics

import com.meet.dev.analyzer.utility.analytics.AnalyticsInitializer.initialize
import com.meet.dev.analyzer.utility.analytics.AnalyticsInitializer.initialized
import com.posthog.kmp.PersonProfiles
import com.posthog.kmp.PostHog
import com.posthog.kmp.PostHogConfig
import com.posthog.kmp.PostHogContext

/**
 * Owns the PostHog SDK lifecycle: setup, super properties, and shutdown.
 *
 * This object is the ONLY place PostHog.setup() is called.
 * AnalyticsManager is responsible only for event capture — not initialization.
 *
 * The [initialized] guard prevents accidental double-setup if [initialize] is
 * called more than once (e.g. in future hot-reload or test scenarios).
 */
object AnalyticsInitializer {

    private var initialized = false

    /**
     * Must be called once — before entering the Compose [application {}] block.
     * Does nothing if [apiKey] is blank (graceful degradation to no-op).
     */
    fun initialize(
        apiKey: String,
        host: String,
        appVersion: String,
        operatingSystem: String,
    ) {
        if (initialized) return
        if (apiKey.isBlank()) return
        try {
            PostHog.setup(
                config = PostHogConfig(
                    apiKey = apiKey,
                    host = host,
                    personProfiles = PersonProfiles.NEVER, // fully anonymous, no person profiles created
                ),
                context = PostHogContext(),
            )
            // Static super properties — attached to every event automatically
            PostHog.register("app_version", appVersion)
            PostHog.register("operating_system", operatingSystem)
            initialized = true
        } catch (_: Exception) {
            // Analytics failures must never crash the app
        }
    }

    /**
     * Updates the "theme" super property on every theme change.
     * Called from main.kt via LaunchedEffect(isDarkMode).
     */
    fun updateTheme(isDark: Boolean) {
        if (!initialized) return
        try {
            PostHog.register("theme", if (isDark) "dark" else "light")
        } catch (_: Exception) {
        }
    }

    /**
     * Explicit flush before close for reliable event delivery on desktop.
     * Order guaranteed: flush → close.
     */
    fun flushAndClose() {
        if (!initialized) return
        try {
            PostHog.flush()
        } catch (_: Exception) {
        }
        try {
            PostHog.close()
        } catch (_: Exception) {
        }
        initialized = false
    }
}
