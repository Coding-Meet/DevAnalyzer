package com.meet.dev.analyzer.data.repository.storage.helpers

import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.data.models.storage.DependenciesInfo
import com.meet.dev.analyzer.data.models.storage.DependenciesItem
import com.meet.dev.analyzer.data.models.storage.KonanInfo
import com.meet.dev.analyzer.data.models.storage.KotlinNativeInfo
import com.meet.dev.analyzer.data.models.storage.KotlinNativeItem
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class KonanAnalyzer(
    private val pathPreferenceManger: PathPreferenceManger
) {
    private val tag = tagName(javaClass = javaClass)

    suspend fun analyzeKonanData(): KonanInfo = withContext(Dispatchers.IO) {

        suspend fun loadDependenciesInfos(konanRootDir: File): List<DependenciesItem> =
            withContext(Dispatchers.IO) {
                val dependenciesDir = File(konanRootDir, "dependencies")
                if (!dependenciesDir.exists()) return@withContext emptyList()

                dependenciesDir.listFiles()
                    ?.filter { it.isDirectory && it.name.any { ch -> ch.isDigit() } }
                    ?.map { dir ->
                        async {
                            val version = dir.name
                            val displayVersion = when {
                                version.startsWith("llvm") -> {
                                    // Example: llvm-19-aarch64-macos-essentials-79
                                    val regex = Regex("""llvm-(\d+).*-(\d+)$""")
                                    val match = regex.find(version)
                                    if (match != null) {
                                        val llvmVer = match.groupValues[1]
                                        val buildVer = match.groupValues[2]
                                        "LLVM $llvmVer (v$buildVer)"
                                    } else version
                                }

                                version.startsWith("lldb") -> {
                                    // Example: lldb-4-macos
                                    val regex = Regex("""lldb-(\d+).*""")
                                    val match = regex.find(version)
                                    if (match != null) {
                                        val lldbVer = match.groupValues[1]
                                        "LLDB $lldbVer"
                                    } else version
                                }

                                version.startsWith("libffi") -> {
                                    // Example: libffi-3.3-1-macos-arm64
                                    val regex = Regex("""libffi-(\d+\.\d+).*""")
                                    val match = regex.find(version)
                                    if (match != null) {
                                        val ffiVer = match.groupValues[1]
                                        "libffi $ffiVer"
                                    } else version
                                }

                                else -> version
                            }
                            val sizeBytes = FolderFileUtils.calculateFolderSize(dir)
                            DependenciesItem(
                                version = displayVersion,
                                path = dir.absolutePath,
                                sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                                sizeBytes = sizeBytes
                            )
                        }
                    }?.awaitAll()?.sortedByDescending {
                        it.sizeBytes
                    } ?: emptyList()
            }

        suspend fun loadKotlinNativeInfos(konanRootDir: File): List<KotlinNativeItem> =
            withContext(Dispatchers.IO) {
                val versionRegex = Regex("""\d+\.\d+(\.\d+)?""")
                konanRootDir.listFiles()
                    ?.filter { it.isDirectory && it.name.contains("kotlin-native-prebuilt") }
                    ?.map { dir ->
                        async {
                            val version = versionRegex.find(dir.name)?.value ?: dir.name
                            val sizeBytes = FolderFileUtils.calculateFolderSize(dir)
                            KotlinNativeItem(
                                version = version,
                                path = dir.absolutePath,
                                sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                                sizeBytes = sizeBytes
                            )
                        }
                    }?.awaitAll()?.sortedByDescending {
                        it.sizeBytes
                    } ?: emptyList()
            }
        try {
            AppLogger.i(tag = tag) { "Analyzing Konan data" }
            val konanFolderPath = pathPreferenceManger.konanFolderPath.first()
            val konanRootDir = File(konanFolderPath)
            if (!konanRootDir.exists()) {
                return@withContext KonanInfo(
                    rootPath = konanRootDir.absolutePath,
                    sizeReadable = "0 B",
                    totalSizeBytes = 0,
                    kotlinNativeInfo = KotlinNativeInfo(
                        name = "Kotlin/Native (.konan)",
                        sizeBytes = 0,
                        sizeReadable = "0 B",
                        kotlinNativeItems = emptyList()
                    ),
                    dependenciesInfo = DependenciesInfo(
                        name = "Dependencies (.konan)",
                        sizeBytes = 0,
                        sizeReadable = "0 B",
                        dependenciesItems = emptyList()
                    )
                )
            }
            val totalSizeBytes = FolderFileUtils.calculateFolderSize(konanRootDir)
            val totalSizeReadable = FolderFileUtils.formatSize(totalSizeBytes)

            val kotlinNativeInfosDeferred =
                async { loadKotlinNativeInfos(konanRootDir = konanRootDir) }
            val dependenciesInfosDeferred =
                async { loadDependenciesInfos(konanRootDir = konanRootDir) }

            val kotlinNativeItems = kotlinNativeInfosDeferred.await()
            val kotlinNativeTotalSizeBytes = kotlinNativeItems.sumOf { it.sizeBytes }
            val kotlinNativeTotalSizeReadable =
                FolderFileUtils.formatSize(kotlinNativeTotalSizeBytes)
            val kotlinNativeInfo = KotlinNativeInfo(
                name = "Kotlin/Native (.konan)",
                sizeBytes = kotlinNativeTotalSizeBytes,
                sizeReadable = kotlinNativeTotalSizeReadable,
                kotlinNativeItems = kotlinNativeItems
            )
            val dependenciesItems = dependenciesInfosDeferred.await()
            val dependenciesTotalSizeBytes = dependenciesItems.sumOf { it.sizeBytes }
            val dependenciesTotalSizeReadable =
                FolderFileUtils.formatSize(dependenciesTotalSizeBytes)
            val dependenciesInfo = DependenciesInfo(
                name = "Dependencies (.konan)",
                sizeBytes = dependenciesTotalSizeBytes,
                sizeReadable = dependenciesTotalSizeReadable,
                dependenciesItems = dependenciesItems
            )
            KonanInfo(
                rootPath = konanRootDir.absolutePath,
                sizeReadable = totalSizeReadable,
                totalSizeBytes = totalSizeBytes,
                kotlinNativeInfo = kotlinNativeInfo,
                dependenciesInfo = dependenciesInfo,
            )
        } catch (e: Exception) {
            AppLogger.e(tag = tag, throwable = e) { "Error analyzing Konan data" }
            throw e
        }
    }
}
