package com.meet.dev.analyzer.utility.crash_report

import com.meet.dev.analyzer.utility.analytics.AnalyticsConfig
import com.meet.dev.analyzer.utility.platform.AppEnvironment
import com.meet.dev.analyzer.utility.platform.DesktopConfig
import java.util.Properties

private object Keys {
    const val SENTRY_DSN = "sentry_dns"
    const val IS_RELEASE = "is_release"
    const val ENABLE_SENTRY = "enable_sentry"
    const val ENABLE_ANALYTICS = "enable_analytics"
    const val POSTHOG_API_KEY = "posthog_api_key"
    const val POSTHOG_HOST = "posthog_host"
    const val VERSION = "version"
    const val UPDATER_URL = "updater_url"
    const val FEEDBACK_URL = "feedback_url"
}

object CustomProperties {

    fun loadProperties(): Properties {
        return Properties().apply {
            CustomProperties::class.java.classLoader
                ?.getResourceAsStream("props.properties")
                ?.use(::load)
        }
    }

    fun createAppConfig(properties: Properties): DesktopConfig {
        val sentryDns = properties.requireProperty(Keys.SENTRY_DSN)
        val version = properties.requireProperty(Keys.VERSION)
        val isRelease = properties.getProperty(Keys.IS_RELEASE)
            ?.toBooleanStrictOrNull() ?: false

        val enableSentry = properties.getProperty(Keys.ENABLE_SENTRY)
            ?.toBooleanStrictOrNull() ?: false

        val enableAnalytics = properties.getProperty(Keys.ENABLE_ANALYTICS)
            ?.toBooleanStrictOrNull() ?: false

        val updaterUrl = properties.requireProperty(Keys.UPDATER_URL)
        val feedbackUrl = properties.requireProperty(Keys.FEEDBACK_URL)

        val appEnvironment = if (isRelease) AppEnvironment.Release else AppEnvironment.Debug

        return DesktopConfig(
            sentryDns = sentryDns,
            version = version,
            enableSentry = enableSentry,
            enableAnalytics = enableAnalytics,
            appEnvironment = appEnvironment,
            updaterUrl = updaterUrl,
            feedbackUrl = feedbackUrl,
        )
    }

    fun setupCrashReporting(appConfig: DesktopConfig, isCrashReportEnabled: Boolean) {
        if (appConfig.enableSentry &&
            appConfig.sentryDns != null &&
            isCrashReportEnabled
        ) {
            initSentry(
                dns = appConfig.sentryDns,
                version = appConfig.version,
            )
        } else {
            disableSentry()
        }
    }

    fun setupLocalLogs(isLocalLogsEnabled: Boolean) {
        if (isLocalLogsEnabled) {
            enableLocalLogs()
        } else {
            disableLocalLogs()
        }
    }

    /**
     * Reads PostHog API key and host from [properties].
     * Returns [AnalyticsConfig] with blank [AnalyticsConfig.apiKey] when the key is absent,
     * which causes [com.meet.dev.analyzer.utility.analytics.NoOpAnalyticsManager] to be selected automatically.
     */
    fun createAnalyticsConfig(properties: Properties): AnalyticsConfig {
        return AnalyticsConfig(
            apiKey = properties.requireProperty(Keys.POSTHOG_API_KEY),
            host = properties.requireProperty(Keys.POSTHOG_HOST)
        )
    }

    private fun Properties.requireProperty(key: String): String {
        return requireNotNull(getProperty(key)) {
            "Missing required property: $key"
        }
    }
}