package com.meet.project.analyzer.core.utility

import java.io.File
import kotlin.math.log10
import kotlin.math.pow

object StorageUtils {
    private const val TAG = "StorageUtils"

    fun calculateFolderSize(file: File): Long {
        AppLogger.d(TAG) { "Calculating size for: ${file.absolutePath}" }
        return try {
            if (!file.exists()) return 0L
            if (file.isFile) return file.length()

            file.listFiles()?.sumOf { childFile ->
                calculateFolderSize(childFile)
            } ?: 0L
        } catch (e: Exception) {
            AppLogger.e(
                TAG,
                throwable = e
            ) {
                "Error calculating folder size for ${file.absolutePath}"
            }
            0L
        }
    }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            "%.2f %s",
            bytes / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

}