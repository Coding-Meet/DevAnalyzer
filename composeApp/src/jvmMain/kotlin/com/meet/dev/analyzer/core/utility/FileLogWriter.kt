package com.meet.dev.analyzer.core.utility

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FileLogWriter(
    private val minSeverity: Severity = Severity.Error
) : LogWriter() {
    private val properties = CustomProperties.loadProperties()
    private val desktopConfig = CustomProperties.createAppConfig(properties)

    private val logDir =
        File(System.getProperty("user.home"), ".dev_analyzer")


    private var logFile: File? = null

    private fun getOrCreateLogFile(): File {
        if (logFile != null) return logFile!!

        logDir.mkdirs()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        val timestamp = LocalDateTime.now().format(formatter)

        val file = File(logDir, "app_$timestamp.log")

        // Write header ONCE
        file.appendText(
            """
            ========= Dev Analyzer Crash Log =========
            Started: ${LocalDateTime.now()}
            OS: ${System.getProperty("os.name")}
            Environment: ${desktopConfig.appEnvironment}
            App Version: ${desktopConfig.version ?: "unknown"}
            Java: ${System.getProperty("java.version")}
            Arch: ${System.getProperty("os.arch")}
            =========================================
            """.trimIndent() + "\n"
        )

        logFile = file
        return file
    }

    override fun isLoggable(tag: String, severity: Severity): Boolean =
        severity >= minSeverity

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity < minSeverity) return
        val stack = throwable?.stackTraceToString() ?: return

        val file = getOrCreateLogFile()

        // optional size protection (~2MB)
        if (file.exists() && file.length() > 2_000_000) {
            file.writeText("")
        }

        file.appendText(
            """
-------------------------
Time: ${LocalDateTime.now()}
Severity: $severity
Tag: $tag
Message: $message

$stack
-------------------------
""".trimIndent()
        )
    }
}
