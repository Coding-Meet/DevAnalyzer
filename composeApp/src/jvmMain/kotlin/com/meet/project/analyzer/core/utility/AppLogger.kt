package com.meet.project.analyzer.core.utility

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter

object AppLogger {

    const val isDebugBuild = true

    val logger = Logger(
        config = StaticConfig(
            logWriterList = if (isDebugBuild) {
                listOf(platformLogWriter())
            } else {
                emptyList() // No logging in release
            }
        ),
        tag = "Project-Analyzer"
    )

    inline fun d(tag: String = "", throwable: Throwable? = null, message: () -> String) {
        if (isDebugBuild) {
            logger.d(tag = tag, throwable = throwable, message = message)
        }
    }

    inline fun i(tag: String = "", throwable: Throwable? = null, message: () -> String) {
        if (isDebugBuild) {
            logger.i(tag = tag, throwable = throwable, message = message)
        }
    }

    inline fun e(tag: String = "", throwable: Throwable? = null, message: () -> String) {
        if (isDebugBuild) {
            logger.e(tag = tag, throwable = throwable, message = message)
        }
    }
    inline fun w(tag: String = "", throwable: Throwable? = null, message: () -> String) {
        if (isDebugBuild) {
            logger.w(tag = tag, throwable = throwable, message = message)
        }
    }
}