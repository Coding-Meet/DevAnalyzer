package com.meet.dev.analyzer.core.utility

import co.touchlab.kermit.DefaultFormatter
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import io.sentry.Sentry
import java.net.SocketTimeoutException

class SentryLogWriter(
    private val minSeverity: Severity = Severity.Warn,
    private val minCrashSeverity: Severity? = Severity.Error,
    private val messageStringFormatter: MessageStringFormatter = DefaultFormatter,
    private val isSentryEnabled: () -> Boolean = { Sentry.isEnabled() },
    private val captureMessage: (String) -> Unit = { message -> Sentry.captureMessage(message) },
    private val captureException: (Throwable) -> Unit = { throwable ->
        Sentry.captureException(
            throwable
        )
    },
) : LogWriter() {

    init {
        @Suppress("UseRequire")
        if (minCrashSeverity != null && minSeverity > minCrashSeverity) {
            throw IllegalArgumentException(
                "minSeverity ($minSeverity) cannot be greater than minCrashSeverity ($minCrashSeverity)",
            )
        }
    }

    override fun isLoggable(tag: String, severity: Severity): Boolean = severity >= minSeverity

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (throwable is SocketTimeoutException) {
            return
        }

        if (!isSentryEnabled()) {
            return
        }

        val shouldCaptureException = throwable != null &&
                minCrashSeverity != null &&
                severity >= minCrashSeverity

        if (shouldCaptureException) {
            // Avoid duplicate Sentry events for throwable logs.
            throwable?.let { captureException(it) }
            return
        }

        captureMessage(
            messageStringFormatter.formatMessage(severity, Tag(tag), Message(message)),
        )
    }
}
