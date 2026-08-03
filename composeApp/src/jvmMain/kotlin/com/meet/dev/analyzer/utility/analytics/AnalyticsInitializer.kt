package com.meet.dev.analyzer.utility.analytics

import com.meet.dev.analyzer.utility.analytics.AnalyticsInitializer.initialize
import com.meet.dev.analyzer.utility.analytics.AnalyticsInitializer.initialized
import com.meet.dev.analyzer.utility.crash_report.AppLogger
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
    val tag = "AnalyticsInitializer"

    @Volatile
    private var initialized = false

    /**
     * Must be called once — before entering the Compose [application {}] block.
     * Does nothing if [apiKey] is blank (graceful degradation to no-op).
     */
    @Synchronized
    fun initialize(
        apiKey: String,
        host: String,
        isDebug: Boolean,
        appVersion: String,
        operatingSystem: String,
    ) {
        if (initialized) {
            AppLogger.w(tag) { "Analytics already initialized, skipping setup." }
            return
        }
        if (apiKey.isBlank()) {
            AppLogger.w(tag) { "Analytics apiKey is blank! PostHog will NOT be initialized (events will be ignored)." }
            return
        }
        if (host.isBlank()) {
            AppLogger.w(tag) { "Analytics host is blank! PostHog will NOT be initialized (events will be ignored)." }
            return
        }
        try {
            PostHog.setup(
                config = PostHogConfig(
                    apiKey = apiKey,
                    host = host,
                    debug = isDebug,

                    personProfiles = PersonProfiles.IDENTIFIED_ONLY,

                    // Queue
                    flushAt = 1,
                    flushIntervalSeconds = 2,
                    maxQueueSize = 1000,
                    maxBatchSize = 50,

                    optOut = false,
                ),
                context = PostHogContext(),
            )
            initialized = true
            // Static super properties — attached to every event automatically
            PostHog.register("app_version", appVersion)
            PostHog.register("operating_system", operatingSystem)

            AppLogger.i(tag) { "Analytics initialization" }
        } catch (e: Exception) {
            AppLogger.e(tag, e, { "Analytics initialization failed" })
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
