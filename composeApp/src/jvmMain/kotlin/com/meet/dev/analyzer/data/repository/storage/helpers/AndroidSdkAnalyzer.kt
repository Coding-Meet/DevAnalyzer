package com.meet.dev.analyzer.data.repository.storage.helpers

import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.data.models.storage.AndroidSdkInfo
import com.meet.dev.analyzer.data.models.storage.BuildToolInfo
import com.meet.dev.analyzer.data.models.storage.BuildToolItem
import com.meet.dev.analyzer.data.models.storage.CmakeInfo
import com.meet.dev.analyzer.data.models.storage.CmakeInfoItem
import com.meet.dev.analyzer.data.models.storage.ExtrasInfo
import com.meet.dev.analyzer.data.models.storage.ExtrasInfoItem
import com.meet.dev.analyzer.data.models.storage.NdkInfo
import com.meet.dev.analyzer.data.models.storage.NdkItem
import com.meet.dev.analyzer.data.models.storage.PlatformInfo
import com.meet.dev.analyzer.data.models.storage.PlatformItem
import com.meet.dev.analyzer.data.models.storage.SourcesInfo
import com.meet.dev.analyzer.data.models.storage.SourcesInfoItem
import com.meet.dev.analyzer.data.models.storage.SystemImageInfo
import com.meet.dev.analyzer.data.models.storage.SystemImageInfoItem
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AndroidSdkAnalyzer(
    private val pathPreferenceManger: PathPreferenceManger
) {
    private val tag = tagName(javaClass = javaClass)

    @OptIn(ExperimentalUuidApi::class)
    data class SdkItem(
        val uniqueId: String = Uuid.random().toString(),
        val name: String,
        val path: String,
        val size: String,
        val sizeBytes: Long,
    )

    suspend fun analyzeAndroidSdkData(): AndroidSdkInfo = withContext(Dispatchers.IO) {

        suspend fun loadSdkItems(directory: File?): List<SdkItem> = withContext(Dispatchers.IO) {
            try {
                directory?.listFiles()
                    ?.filter { !it.name.startsWith(".") }
                    ?.map { dir ->
                        async {
                            val sizeBytes = FolderFileUtils.calculateFolderSize(dir)
                            SdkItem(
                                name = dir.name,
                                path = dir.absolutePath,
                                size = FolderFileUtils.formatSize(sizeBytes),
                                sizeBytes = sizeBytes
                            )
                        }
                    }?.awaitAll()?.sortedByDescending {
                        it.sizeBytes
                    } ?: emptyList()
            } catch (e: Exception) {
                AppLogger.e(
                    tag = tag,
                    throwable = e
                ) { "Error loading SDK items from ${directory?.absolutePath}" }
                emptyList()
            }
        }

        suspend fun loadSdkExtras(sdkDir: File): List<SdkItem> = withContext(Dispatchers.IO) {
            val extraFolders = listOf("platform-tools", "emulator")
            extraFolders.map { folder ->
                async {
                    val dir = File(sdkDir, folder)
                    if (dir.exists()) {
                        val sizeBytes = FolderFileUtils.calculateFolderSize(dir)
                        SdkItem(
                            name = folder,
                            path = dir.absolutePath,
                            size = FolderFileUtils.formatSize(sizeBytes),
                            sizeBytes = sizeBytes
                        )
                    } else null
                }
            }.awaitAll().filterNotNull().sortedByDescending {
                it.sizeBytes
            }
        }

        AppLogger.i(tag = tag) { "Loading SDK information" }
        val emptyAndroidSdkInfo = AndroidSdkInfo(
            sdkPath = "",
            sizeReadable = "0 B",
            totalSizeBytes = 0,
            platformInfo = PlatformInfo(
                platforms = emptyList(),
                sizeReadable = "0 B",
                totalSizeBytes = 0
            ),
            buildToolInfo = BuildToolInfo(
                buildTools = emptyList(),
                sizeReadable = "0 B",
                totalSizeBytes = 0
            ),
            systemImageInfo = SystemImageInfo(
                systemImages = emptyList(),
                sizeReadable = "0 B",
                totalSizeBytes = 0
            ),
            ndkInfo = NdkInfo(
                ndkItems = emptyList(),
                sizeReadable = "0 B",
                totalSizeBytes = 0
            ),
            sourcesInfo = SourcesInfo(
                sources = emptyList(),
                sizeReadable = "0 B",
                totalSizeBytes = 0
            ),
            cmakeInfo = CmakeInfo(
                cmakeItems = emptyList(),
                sizeReadable = "0 B",
                totalSizeBytes = 0
            ),
            extrasInfo = ExtrasInfo(
                extrasInfoItems = emptyList(),
                sizeReadable = "0 B",
                totalSizeBytes = 0
            )
        )
        try {
            val sdkLocationPath = pathPreferenceManger.sdkPath.first()
            val sdkDir = File(sdkLocationPath)
            if (!sdkDir.exists() || !sdkDir.isDirectory) {
                return@withContext emptyAndroidSdkInfo
            }

            val platformsDeferred = async {
                loadSdkItems(File(sdkDir, "platforms"))
            }
            val buildToolsDeferred = async {
                loadSdkItems(File(sdkDir, "build-tools"))
            }
            val systemImagesDeferred = async {
                loadSdkItems(File(sdkDir, "system-images"))
            }
            val ndkDeferred = async {
                loadSdkItems(File(sdkDir, "ndk"))
            }

            val sourcesDeferred = async {
                loadSdkItems(File(sdkDir, "sources"))
            }

            val cmakeDeferred = async {
                loadSdkItems(File(sdkDir, "cmake"))
            }

            val extrasDeferred = async {
                loadSdkExtras(sdkDir)
            }

            val platforms = platformsDeferred.await().map {
                PlatformItem(
                    name = it.name,
                    path = it.path,
                    sizeReadable = it.size,
                    sizeBytes = it.sizeBytes,
                )
            }
            val platformsSize = platforms.sumOf { it.sizeBytes }
            val platformsSizeReadable = FolderFileUtils.formatSize(platformsSize)
            val platformInfo = PlatformInfo(
                platforms = platforms,
                sizeReadable = platformsSizeReadable,
                totalSizeBytes = platformsSize
            )

            val buildTools = buildToolsDeferred.await().map {
                BuildToolItem(
                    name = it.name,
                    path = it.path,
                    sizeReadable = it.size,
                    sizeBytes = it.sizeBytes,
                )
            }
            val buildToolsSize = buildTools.sumOf { it.sizeBytes }
            val buildToolsSizeReadable = FolderFileUtils.formatSize(buildToolsSize)
            val buildToolInfo = BuildToolInfo(
                buildTools = buildTools,
                sizeReadable = buildToolsSizeReadable,
                totalSizeBytes = buildToolsSize
            )

            val systemImages = systemImagesDeferred.await().map {
                SystemImageInfoItem(
                    name = it.name,
                    path = it.path,
                    sizeReadable = it.size,
                    sizeBytes = it.sizeBytes,
                )
            }
            val systemImageSize = systemImages.sumOf { it.sizeBytes }
            val systemImageSizeReadable = FolderFileUtils.formatSize(systemImageSize)
            val systemImageInfo = SystemImageInfo(
                systemImages = systemImages,
                sizeReadable = systemImageSizeReadable,
                totalSizeBytes = systemImageSize
            )

            val ndkItems = ndkDeferred.await().map {
                NdkItem(
                    name = it.name,
                    path = it.path,
                    sizeReadable = it.size,
                    sizeBytes = it.sizeBytes,
                )
            }
            val ndkSize = ndkItems.sumOf { it.sizeBytes }
            val ndkSizeReadable = FolderFileUtils.formatSize(ndkSize)
            val ndkInfo = NdkInfo(
                ndkItems = ndkItems,
                sizeReadable = ndkSizeReadable,
                totalSizeBytes = ndkSize
            )

            val sources = sourcesDeferred.await().map {
                SourcesInfoItem(
                    name = it.name,
                    path = it.path,
                    sizeReadable = it.size,
                    sizeBytes = it.sizeBytes,
                )
            }
            val sourcesSize = sources.sumOf { it.sizeBytes }
            val sourcesSizeReadable = FolderFileUtils.formatSize(sourcesSize)
            val sourcesInfo = SourcesInfo(
                sources = sources,
                sizeReadable = sourcesSizeReadable,
                totalSizeBytes = sourcesSize
            )

            val cmakeList = cmakeDeferred.await().map {
                CmakeInfoItem(
                    name = it.name,
                    path = it.path,
                    sizeReadable = it.size,
                    sizeBytes = it.sizeBytes
                )
            }
            val cmakeSize = cmakeList.sumOf { it.sizeBytes }
            val cmakeSizeReadable = FolderFileUtils.formatSize(cmakeSize)
            val cmakeInfo = CmakeInfo(
                cmakeItems = cmakeList,
                sizeReadable = cmakeSizeReadable,
                totalSizeBytes = cmakeSize
            )


            val extras = extrasDeferred.await().map {
                ExtrasInfoItem(
                    name = it.name,
                    path = it.path,
                    sizeReadable = it.size,
                    sizeBytes = it.sizeBytes
                )
            }
            val extrasSize = extras.sumOf { it.sizeBytes }
            val extrasSizeReadable = FolderFileUtils.formatSize(extrasSize)
            val extrasInfo = ExtrasInfo(
                extrasInfoItems = extras,
                sizeReadable = extrasSizeReadable,
                totalSizeBytes = extrasSize
            )

            val sdkDirSizeBytes =
                platformsSize + buildToolsSize + systemImageSize + ndkSize + sourcesSize + cmakeSize + extrasSize
            val sdkDirSizeReadable = FolderFileUtils.formatSize(sdkDirSizeBytes)


            AndroidSdkInfo(
                sdkPath = sdkDir.absolutePath,
                sizeReadable = sdkDirSizeReadable,
                totalSizeBytes = sdkDirSizeBytes,
                platformInfo = platformInfo,
                buildToolInfo = buildToolInfo,
                systemImageInfo = systemImageInfo,
                ndkInfo = ndkInfo,
                sourcesInfo = sourcesInfo,
                cmakeInfo = cmakeInfo,
                extrasInfo = extrasInfo
            )
        } catch (e: Exception) {
            AppLogger.e(tag = tag, throwable = e) { "Error loading SDK information" }
            return@withContext emptyAndroidSdkInfo
        }
    }
}
