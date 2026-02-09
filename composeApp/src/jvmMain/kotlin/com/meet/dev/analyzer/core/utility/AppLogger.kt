package com.meet.dev.analyzer.core.utility

import co.touchlab.kermit.Logger

object AppLogger {

    fun d(tag: String = "", throwable: Throwable? = null, message: () -> String) {
        Logger.d(tag, throwable = throwable, message = message)
    }

    fun i(tag: String = "", throwable: Throwable? = null, message: () -> String) {
        Logger.i(tag, throwable = throwable, message = message)
    }

    fun e(tag: String = "", throwable: Throwable? = null, message: () -> String) {
        Logger.e(tag, throwable = throwable, message = message)
    }

    fun w(tag: String = "", throwable: Throwable? = null, message: () -> String) {
        Logger.w(tag, throwable = throwable, message = message)
    }
}