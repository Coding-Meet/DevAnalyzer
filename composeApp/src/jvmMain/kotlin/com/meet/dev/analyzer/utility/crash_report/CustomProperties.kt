package com.meet.dev.analyzer.utility.crash_report

import com.meet.dev.analyzer.BuildKonfig
import com.meet.dev.analyzer.utility.analytics.AnalyticsConfig
import com.meet.dev.analyzer.utility.platform.AppEnvironment
import com.meet.dev.analyzer.utility.platform.DesktopConfig
import java.io.InputStream
import java.util.Properties

object CustomProperties {
    fun loadProperties(): Properties {
        val properties = Properties()
        val propsFile =
            CustomProperties::class.java.classLoader?.getResourceAsStream("props.properties")
                ?: InputStream.nullInputStream()
        properties.load(propsFile)
        return properties
    }

    fun createAppConfig(properties: Properties): DesktopConfig {
        val sentryDns = properties["sentry_dns"]?.toString()
        val version = BuildKonfig.VERSION_NAME
        val isRelease = properties["is_release"]?.toString()?.toBooleanStrictOrNull() ?: false

        val appEnvironment = if (isRelease) AppEnvironment.Release else AppEnvironment.Debug

        return DesktopConfig(
            sentryDns = sentryDns,
            version = version,
            appEnvironment = appEnvironment,
        )
    }

    fun setupCrashReporting(appConfig: DesktopConfig, isCrashReportEnabled: Boolean) {
        if (appConfig.appEnvironment.isRelease() &&
            appConfig.sentryDns != null &&
            appConfig.version != null &&
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
     * which causes [NoOpAnalyticsManager] to be selected automatically.
     */
    fun createAnalyticsConfig(properties: Properties): AnalyticsConfig {
        val apiKey = properties["posthog_api_key"]?.toString().orEmpty()
        val host = properties["posthog_host"]?.toString().orEmpty()
            .ifBlank { AnalyticsConfig.DEFAULT_HOST }
        return AnalyticsConfig(apiKey = apiKey, host = host)
    }
}