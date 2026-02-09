package com.meet.dev.analyzer.core.utility

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
        val version = properties["version"]?.toString()
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
}