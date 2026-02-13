package com.meet.dev.analyzer.utility.crash_report

import co.touchlab.kermit.Logger
import io.sentry.Sentry
import io.sentry.SentryLevel
import org.koin.mp.KoinPlatform.getKoin

fun initSentry(
    dns: String,
    version: String,
) {
    val sentryLogWriter = getKoin().get<SentryLogWriter>()
    if (Logger.config.logWriterList.none { it is FileLogWriter }) {
        Logger.addLogWriter(sentryLogWriter)
    }
    Sentry.init { options ->
        options.dsn = dns

        options.release = "com.meet.dev.analyzer@$version"

        options.setDiagnosticLevel(
            SentryLevel.ERROR,
        )
    }
}

fun disableSentry() {
    Sentry.close()
    val sentryLogWriter = getKoin().get<SentryLogWriter>()
    Logger.setLogWriters(
        Logger.config.logWriterList.filterNot { w -> w == sentryLogWriter }
    )
}




