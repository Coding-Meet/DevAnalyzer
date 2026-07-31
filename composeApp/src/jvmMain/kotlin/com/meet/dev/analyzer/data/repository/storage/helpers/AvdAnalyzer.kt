package com.meet.dev.analyzer.data.repository.storage.helpers

import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.data.models.storage.*
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Properties

class AvdAnalyzer(
    private val pathPreferenceManger: PathPreferenceManger
) {
    private val tag = tagName(javaClass = javaClass)

    suspend fun analyzeAvdData(): AndroidAvdInfo = withContext(Dispatchers.IO) {

        fun parseConfiguredSize(raw: String?): String {
            if (raw == null) return "Unknown"
            return try {
                when {
                    raw.endsWith("M", true) -> {
                        val mb = raw.dropLast(1).toLong()
                        FolderFileUtils.formatSize(mb * 1024 * 1024)
                    }

                    raw.endsWith("K", true) -> {
                        val kb = raw.dropLast(1).toLong()
                        FolderFileUtils.formatSize(kb * 1024)
                    }

                    raw.endsWith("G", true) -> {
                        val gb = raw.dropLast(1).toLong()
                        FolderFileUtils.formatSize(gb * 1024 * 1024 * 1024)
                    }

                    raw.toLongOrNull() != null -> FolderFileUtils.formatSize(raw.toLong())
                    else -> raw
                }
            } catch (e: Exception) {
                AppLogger.e(
                    tag = tag,
                    throwable = e,
                    message = { "Error parsing configured size: $raw" })
                raw
            }
        }

        suspend fun loadAvdInfos(avdDir: File): List<AvdItem> = withContext(Dispatchers.IO) {
            avdDir.listFiles { file -> file.extension == "ini" }
                ?.map { iniFile ->
                    async {
                        try {
                            val props = Properties().apply {
                                load(iniFile.inputStream())
                            }
                            val path = props.getProperty("path")
                            val configFile = File(path, "config.ini")

                            if (!configFile.exists()) return@async null

                            val configProps = Properties().apply {
                                load(configFile.inputStream())
                            }

                            val name = iniFile.nameWithoutExtension
                            val apiLevel = configProps.getProperty("target")
                            val device = configProps.getProperty("hw.device.name")
                            val configuredRaw = configProps.getProperty("disk.dataPartition.size")
                            val configuredStorage = parseConfiguredSize(configuredRaw)
                            val actualSizeBytes = FolderFileUtils.calculateFolderSize(File(path))
                            val actualSize = FolderFileUtils.formatSize(actualSizeBytes)
                            AvdItem(
                                name = name,
                                apiLevel = apiLevel,
                                device = device,
                                path = path,
                                configuredStorage = configuredStorage,
                                actualStorage = actualSize,
                                sizeBytes = actualSizeBytes
                            )
                        } catch (e: Exception) {
                            AppLogger.e(
                                tag = tag,
                                throwable = e
                            ) { "Error processing AVD file: ${iniFile.name}" }
                            null
                        }
                    }
                }?.awaitAll()?.filterNotNull()?.sortedByDescending {
                    it.sizeBytes
                } ?: emptyList()
        }
        AppLogger.i(tag = tag) { "Loading AVD information" }
        try {
            val avdLocationPath = pathPreferenceManger.avdLocationPath.first()

            val avdDir = File(avdLocationPath)

            AppLogger.i(tag = tag) { "AVD directory: ${avdDir.absolutePath}" }

            val avdItemLists = async {
                loadAvdInfos(avdDir)
            }.await()
            val totalSizeBytes = avdItemLists.sumOf { it.sizeBytes }
            AndroidAvdInfo(
                avdItemList = avdItemLists,
                totalSizeBytes = totalSizeBytes,
                sizeReadable = FolderFileUtils.formatSize(totalSizeBytes)
            )
        } catch (e: Exception) {
            AppLogger.e(tag = tag, throwable = e) { "Error loading AVD information" }
            return@withContext AndroidAvdInfo(
                avdItemList = emptyList(),
                totalSizeBytes = 0,
                sizeReadable = "0 B"
            )
        }
    }
}
