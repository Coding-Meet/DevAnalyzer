package com.meet.dev.analyzer.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.meet.dev.analyzer.utility.crash_report.FileLogWriter
import com.meet.dev.analyzer.utility.crash_report.SentryLogWriter
import com.meet.dev.analyzer.utility.platform.AppEnvironment
import org.koin.core.module.Module
import org.koin.dsl.module

fun getLoggingModule(
    appEnvironment: AppEnvironment,
): Module =
    module {
        single<SentryLogWriter> { SentryLogWriter() }
        single<FileLogWriter> { FileLogWriter() }


        single {
            val fileLogWriter = get<FileLogWriter>()
            val sentryWriter = get<SentryLogWriter>()
            val loggers = mutableListOf(platformLogWriter(), fileLogWriter)
            if (appEnvironment.isRelease()) {
                loggers.add(sentryWriter)
            }

            val minSeverity = if (appEnvironment.isRelease()) {
                Severity.Info
            } else {
                Severity.Verbose
            }
            val logger = Logger(
                config = StaticConfig(
                    logWriterList = loggers,
                    minSeverity = minSeverity,
                ),
                tag = "Dev-Analyzer",
            )
            Logger.setLogWriters(
                loggers
            )
            // dont pass logger object in class it not work.so use Logger.e....
            logger
        }
    }

