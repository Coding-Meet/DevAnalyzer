package com.meet.dev.analyzer.utility.crash_report

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

    fun tagName(
        javaClass: Class<*>
    ): String {
        return if (!javaClass.isAnonymousClass) {
            val name = javaClass.simpleName
            if (name.length <= 23) name else name.take(23)  // first 23 chars
        } else {
            val name = javaClass.name
            if (name.length <= 23) name else name.substring(
                name.length - 23, name.length
            )                   // last 23 chars
        }
    }
}