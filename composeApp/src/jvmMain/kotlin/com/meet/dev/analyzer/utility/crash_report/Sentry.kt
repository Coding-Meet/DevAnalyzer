package com.meet.dev.analyzer.utility.crash_report

import io.sentry.Sentry
import io.sentry.SentryLevel

fun initSentry(
    dns: String,
    version: String,
) {
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
}




