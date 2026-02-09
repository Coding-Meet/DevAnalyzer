package com.meet.dev.analyzer.utility.crash_report

import co.touchlab.kermit.Logger
import org.koin.mp.KoinPlatform.getKoin

fun enableLocalLogs() {
    val fileLogWriter = getKoin().get<FileLogWriter>()
    if (Logger.config.logWriterList.none { it is FileLogWriter }) {
        Logger.addLogWriter(fileLogWriter)
    }
}

fun disableLocalLogs() {
    val writer = getKoin().get<FileLogWriter>()
    Logger.setLogWriters(
        Logger.config.logWriterList.filterNot { w -> w == writer }
    )
}