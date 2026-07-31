package com.meet.dev.analyzer.utility.platform

import com.meet.dev.analyzer.data.models.storage.GradleLibraryInfo
import com.meet.dev.analyzer.data.models.storage.GradleModulesInfo
import com.meet.dev.analyzer.data.models.storage.GradleVersionInfo
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import io.github.z4kn4fein.semver.VersionFormatException
import io.github.z4kn4fein.semver.toVersion
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

object FolderFileUtils {
    private val TAG = tagName(javaClass = javaClass)

    fun calculateFolderSize(file: File, isCommend: Boolean = true): Long {
        AppLogger.d(TAG) { "Calculating size for: ${file.absolutePath}" }

        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        val path = file.path

        return if (isCommend) {
            try {
                when {
                    getDesktopOS().isWindows() -> {
                        val bytes = file.walkTopDown().map { it.length() }.sum()

//                        // 🪟 Windows - PowerShell method in cmd very slow in window
//                        val command = listOf(
//                            "powershell",
//                            "-Command",
//                            "(Get-ChildItem \"$path\" -Recurse -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum"
//                        )
//                        val process = ProcessBuilder(command).start()
//                        val output = process.inputStream.bufferedReader().readText().trim()
//                        val bytes = output.toLongOrNull()
//                        if (bytes != null) {
//                            AppLogger.d(TAG) { "Windows PowerShell method worked: $bytes bytes" }
//                        }
                        bytes
                    }

                    else -> {
                        // 🍎 macOS / 🐧 Linux - du method
                        val command = listOf(
                            "bash", "-c", "du -sk \"$path\" | awk '{print \$1 * 1024}'"
                        )
                        val process = ProcessBuilder(command).start()
                        val output = process.inputStream.bufferedReader().readText().trim()
                        val bytes = output.toLongOrNull() ?: output.split("\t").firstOrNull()
                            ?.toLongOrNull()
                        if (bytes != null) {
                            AppLogger.d(TAG) { "macOS/Linux du method worked: $bytes bytes" }
                        }
                        bytes
                    }
                } ?: run {
                    // Fallback: Walk through all files
                    val bytes = file.walkTopDown().map { it.length() }.sum()
                    AppLogger.d(TAG) { "Fallback walkTopDown method used: $bytes bytes" }
                    bytes
                }
            } catch (e: Exception) {
                AppLogger.e(
                    TAG,
                    throwable = e
                ) { "Error calculating folder size for ${file.absolutePath}" }
                0L
            }
        } else {
            val bytes = file.walkTopDown().map { it.length() }.sum()
            AppLogger.d(TAG) { "Fallback walkTopDown method used: $bytes bytes" }
            bytes
        }

    }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val base = 1000.0 // Decimal (1000): 6.33 GB Binary (1024): 5.90 GB
        val digitGroups = (log10(bytes.toDouble()) / log10(base)).toInt()
        return String.format(
            Locale.US,
            "%.2f %s",
            bytes / base.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    fun File.openFile() {
        if (Desktop.isDesktopSupported()) {
            if (isDirectory) {
                Desktop.getDesktop().open(this)
            } else {
                Desktop.getDesktop().open(parentFile)
            }
        }
    }

    fun formatElapsedTime(startTime: Long): String {
        val seconds = (System.currentTimeMillis() - startTime) / 1000
        val min = seconds / 60
        val sec = seconds % 60
        return "%02d:%02d".format(Locale.US, min, sec)
    }


    fun String.openFile() {
        val file = File(this)
        file.openFile()
    }
    fun copyToClipboard(content: String) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(StringSelection(content), null)
        } catch (e: Exception) {
            AppLogger.e(TAG, throwable = e) { "Error copying to clipboard" }
        }
    }

}