package com.meet.project.analyzer.core.utility

import com.meet.project.analyzer.data.models.GradleLibraryInfo
import com.meet.project.analyzer.data.models.GradleModulesInfo
import com.meet.project.analyzer.data.models.GradleVersionInfo
import io.github.z4kn4fein.semver.VersionFormatException
import io.github.z4kn4fein.semver.toVersion
import java.awt.Desktop
import java.io.File
import kotlin.math.log10
import kotlin.math.pow

object Utils {
    private val TAG = tagName(javaClass = javaClass)


    fun calculateFolderSize(file: File): Long {
        AppLogger.d(TAG) { "Calculating size for: ${file.absolutePath}" }
        return try {
            if (!file.exists()) return 0L
            if (file.isFile) return file.length()
            return file
                .walkTopDown()
                .map { it.length() }
                .sum()
//            file.listFiles()?.sumOf { childFile ->
//                calculateFolderSize(childFile)
//            } ?: 0L
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

    fun File.openFile() {
        if (Desktop.isDesktopSupported()) {
            if (isDirectory) {
                Desktop.getDesktop().open(this)
            } else {
                Desktop.getDesktop().open(parentFile)
            }
        }
    }

    fun String.openFile() {
        val file = File(this)
        file.openFile()
    }

    fun getGradleModulesInfo(): GradleModulesInfo? {
        AppLogger.d(TAG) { "Getting gradle modules info" }
        val modulesDir =
            File(System.getProperty("user.home"), ".gradle/caches/modules-2/files-2.1")
        if (!modulesDir.exists()) return null

        val sizeBytes = calculateFolderSize(modulesDir)
        val libraryMap = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()

        val allDirs = modulesDir.walkTopDown()
            .maxDepth(3)
            .filter { it.isDirectory }
            .toList()

        allDirs.forEach { dir ->
            val parts = dir.relativeTo(modulesDir).path.split(File.separator)
            if (parts.size == 3) {
                val (groupId, artifactId, version) = parts
                libraryMap
                    .getOrPut(groupId) { mutableMapOf() }
                    .getOrPut(artifactId) { mutableSetOf() }
                    .add(version)
            }
        }

        val libraries = libraryMap.map { (groupId, artifactMap) ->
            artifactMap.map { (artifactId, versions) ->
                val artifactDir = File(modulesDir, "$groupId/$artifactId")

                // per-version size calculate
                val versionInfos = try {
                    versions.map { version ->
                        val versionDir = File(artifactDir, version)
                        val versionSizeBytes = calculateFolderSize(versionDir)
                        GradleVersionInfo(
                            version = version,
                            sizeReadable = formatSize(versionSizeBytes),
                            sizeBytes = versionSizeBytes
                        )
                    }.sortedByDescending { it.version.toVersion(false) }
                } catch (e: VersionFormatException) {
                    versions.map { version ->
                        val versionDir = File(artifactDir, version)
                        val versionSizeBytes = calculateFolderSize(versionDir)
                        GradleVersionInfo(
                            version = version,
                            sizeReadable = formatSize(versionSizeBytes),
                            sizeBytes = versionSizeBytes
                        )
                    }
                }
                val artifactSizeBytes = versionInfos.sumOf { it.sizeBytes }
                GradleLibraryInfo(
                    groupId = groupId,
                    artifactId = artifactId,
                    versions = versionInfos,
                    totalSize = formatSize(artifactSizeBytes),
                    totalSizeBytes = artifactSizeBytes
                )
            }
        }.flatten()

        val gradleModulesInfo = GradleModulesInfo(
            path = modulesDir.absolutePath,
            sizeReadable = formatSize(sizeBytes),
            libraries = libraries.sortedBy { "${it.groupId}:${it.artifactId}" },
            sizeBytes = sizeBytes
        )
        AppLogger.d(TAG) { "Found ${libraries.size} libraries" }
        AppLogger.d(TAG) {
            """
                GradleModulesInfo:
                path: ${gradleModulesInfo.path}
                sizeReadable: ${gradleModulesInfo.sizeReadable}
                sizeBytes: ${gradleModulesInfo.sizeBytes}
            """.trimIndent()
        }
        gradleModulesInfo.libraries.forEach {
            AppLogger.d(TAG) { "Library: ${it.groupId}:${it.artifactId} groupId: ${it.groupId} artifactId: ${it.artifactId} versions: ${it.versions} totalSize: ${it.totalSize} totalSizeBytes: ${it.totalSizeBytes}" }
        }
        return gradleModulesInfo
    }

    fun tagName(
        javaClass: Class<*>
    ): String {
        return if (!javaClass.isAnonymousClass) {
            val name = javaClass.simpleName
            if (name.length <= 23) name else name.substring(0, 23)  // first 23 chars
        } else {
            val name = javaClass.name
            if (name.length <= 23) name else name.substring(
                name.length - 23, name.length
            )                   // last 23 chars
        }
    }
}