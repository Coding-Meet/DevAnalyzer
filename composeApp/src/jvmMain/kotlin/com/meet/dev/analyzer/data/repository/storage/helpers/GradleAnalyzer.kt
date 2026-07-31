package com.meet.dev.analyzer.data.repository.storage.helpers

import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.data.models.storage.*
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import io.github.z4kn4fein.semver.VersionFormatException
import io.github.z4kn4fein.semver.toVersion
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class GradleAnalyzer(
    private val pathPreferenceManger: PathPreferenceManger
) {
    private val tag = tagName(javaClass = javaClass)

    fun getGradleModulesInfo(): GradleModulesInfo {
        AppLogger.d(tag) { "Getting gradle modules info" }
        val modulesDir =
            File(
                System.getProperty("user.home"),
                ".gradle" + File.separator + "caches" + File.separator + "modules-2" + File.separator + "files-2.1"
            )

        val sizeBytes = FolderFileUtils.calculateFolderSize(modulesDir)
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
                        val versionSizeBytes = FolderFileUtils.calculateFolderSize(versionDir, isCommend = false)
                        GradleVersionInfo(
                            version = version,
                            path = versionDir.absolutePath,
                            sizeReadable = FolderFileUtils.formatSize(versionSizeBytes),
                            sizeBytes = versionSizeBytes
                        )
                    }.sortedByDescending { it.version.toVersion(false) }
                } catch (e: VersionFormatException) {
                    versions.map { version ->
                        val versionDir = File(artifactDir, version)
                        val versionSizeBytes = FolderFileUtils.calculateFolderSize(versionDir, isCommend = false)
                        GradleVersionInfo(
                            version = version,
                            path = versionDir.absolutePath,
                            sizeReadable = FolderFileUtils.formatSize(versionSizeBytes),
                            sizeBytes = versionSizeBytes
                        )
                    }
                }
                val artifactSizeBytes = versionInfos.sumOf { it.sizeBytes }
                GradleLibraryInfo(
                    groupId = groupId,
                    artifactId = artifactId,
                    versions = versionInfos,
                    path = artifactDir.absolutePath,
                    sizeReadable =  FolderFileUtils.formatSize(artifactSizeBytes),
                    totalSizeBytes = artifactSizeBytes
                )
            }
        }.flatten()

        val gradleModulesInfo = GradleModulesInfo(
            path = modulesDir.absolutePath,
            sizeReadable = FolderFileUtils.formatSize(sizeBytes),
            groupList = libraries.distinctBy { it.groupId },
            libraries = libraries.sortedBy { "${it.groupId}:${it.artifactId}" },
            sizeBytes = sizeBytes
        )
        AppLogger.d(tag) { "Found ${libraries.size} libraries" }
        AppLogger.d(tag) {
            """
                GradleModulesInfo:
                path: ${gradleModulesInfo.path}
                sizeReadable: ${gradleModulesInfo.sizeReadable}
                sizeBytes: ${gradleModulesInfo.sizeBytes}
            """.trimIndent()
        }
        gradleModulesInfo.libraries.forEach {
            AppLogger.d(tag) { "Library: ${it.groupId}:${it.artifactId} groupId: ${it.groupId} artifactId: ${it.artifactId} versions: ${it.versions} sizeReadable: ${it.sizeReadable} totalSizeBytes: ${it.totalSizeBytes}" }
        }
        return gradleModulesInfo
    }

    suspend fun loadOtherFolder(gradleDir: File): List<OtherGradleFolderItem> =
        withContext(Dispatchers.IO) {
            val cachesDir = File(gradleDir, "caches")

            val otherFolderList = listOf("transforms-3", "jars-9", "build-cache-1")
            val cachesList = cachesDir.listFiles()
                ?.filter { it.isDirectory && otherFolderList.contains(it.name) }
                ?.map { distDir ->
                    async {
                        val sizeBytes = FolderFileUtils.calculateFolderSize(distDir)
                        OtherGradleFolderItem(
                            version = distDir.name,
                            path = distDir.absolutePath,
                            sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                            sizeBytes = sizeBytes
                        )
                    }
                }?.awaitAll()?.sortedByDescending {
                    it.sizeBytes
                } ?: emptyList()

            val modulesDir = File(cachesDir, "modules-2")
            val metadataList = modulesDir.listFiles()
                ?.filter { it.isDirectory && it.name != "files-2.1" }
                ?.map { metaDir ->
                    async {
                        val sizeBytes = FolderFileUtils.calculateFolderSize(metaDir)
                        OtherGradleFolderItem(
                            version = metaDir.name,
                            path = metaDir.absolutePath,
                            sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                            sizeBytes = sizeBytes
                        )
                    }
                }?.awaitAll()?.sortedByDescending {
                    it.sizeBytes
                } ?: emptyList()

            val tempDir = File(gradleDir, ".tmp")
            val temp = async {
                val sizeBytes = FolderFileUtils.calculateFolderSize(tempDir)
                OtherGradleFolderItem(
                    version = tempDir.name,
                    path = tempDir.absolutePath,
                    sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                    sizeBytes = sizeBytes
                )
            }.await()
            cachesList + metadataList + temp
        }

    suspend fun loadCachesGradleWrapperInfos(gradleDir: File): List<CachesGradleWrapperItem> =
        withContext(Dispatchers.IO) {
            val wrapperDir = File(gradleDir, "caches")

            val versionRegex = Regex("""\d+\.\d+(\.\d+)?""")
            val ignoreDirs =
                listOf("modules-2", "transforms-3", "jars-9", "journal-1", "build-cache-1")

            wrapperDir.listFiles()
                ?.filter { it.isDirectory && !ignoreDirs.contains(it.name) }
                ?.map { distDir ->
                    async {
                        val version = versionRegex.find(distDir.name)?.value ?: distDir.name
                        val sizeBytes = FolderFileUtils.calculateFolderSize(distDir)
                        CachesGradleWrapperItem(
                            version = version,
                            path = distDir.absolutePath,
                            sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                            sizeBytes = sizeBytes
                        )
                    }
                }?.awaitAll()?.sortedByDescending {
                    it.sizeBytes
                } ?: emptyList()
        }

    suspend fun loadDaemonInfos(gradleDir: File): List<DaemonItem> =
        withContext(Dispatchers.IO) {
            val wrapperDir = File(gradleDir, "daemon")

            val versionRegex = Regex("""\d+\.\d+(\.\d+)?""")
            wrapperDir.listFiles()
                ?.filter { it.isDirectory }
                ?.map { distDir ->
                    async {
                        val version = versionRegex.find(distDir.name)?.value ?: distDir.name
                        val sizeBytes = FolderFileUtils.calculateFolderSize(distDir)
                        DaemonItem(
                            name = version,
                            path = distDir.absolutePath,
                            sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                            sizeBytes = sizeBytes
                        )
                    }
                }?.awaitAll()?.sortedByDescending {
                    it.sizeBytes
                } ?: emptyList()
        }

    suspend fun loadGradleWrapperInfos(gradleDir: File): List<WrapperItem> =
        withContext(Dispatchers.IO) {
            val wrapperDir = File(gradleDir, "wrapper/dists")

            val versionRegex = Regex("""\d+\.\d+(\.\d+)?""")
            wrapperDir.listFiles()
                ?.filter { it.isDirectory }
                ?.map { distDir ->
                    async {
                        val version = versionRegex.find(distDir.name)?.value ?: distDir.name
                        val sizeBytes = FolderFileUtils.calculateFolderSize(distDir)
                        WrapperItem(
                            version = version,
                            path = distDir.absolutePath,
                            sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                            sizeBytes = sizeBytes
                        )
                    }
                }?.awaitAll()?.sortedByDescending {
                    it.sizeBytes
                } ?: emptyList()
        }
    suspend fun analyzeGradleData(): GradleInfo = withContext(Dispatchers.IO) {
        val emptyGradleInfo = GradleInfo(
            sizeReadable = "0 B",
            totalSizeBytes = 0,
            cachesGradleWrapperInfo = CachesGradleWrapperInfo(
                sizeReadable = "0 B",
                totalSizeBytes = 0,
                cachesGradleWrapperItems = emptyList()
            ),
            daemonInfo = DaemonInfo(
                sizeReadable = "0 B",
                totalSizeBytes = 0,
                daemonItems = emptyList()
            ),
            wrapperInfo = WrapperInfo(
                sizeReadable = "0 B",
                totalSizeBytes = 0,
                wrapperItems = emptyList()
            ),
            rootPath = "",
            otherGradleFolderInfo = OtherGradleFolderInfo(
                sizeReadable = "0 B",
                totalSizeBytes = 0,
                otherGradleFolderItems = emptyList()
            ),
            gradleModulesInfo = GradleModulesInfo(
                path = "",
                sizeBytes = 0,
                sizeReadable = "0 B",
                groupList = emptyList(),
                libraries = emptyList()
            ),
            jdkInfo = JdkInfo(
                sizeReadable = "0 B",
                totalSizeBytes = 0,
                jdkItems = emptyList()
            )
        )
        try {
            val gradleUserHomePath = pathPreferenceManger.gradleUserHomePath.first()
            val gradleDir = File(gradleUserHomePath)
            if (!gradleDir.exists()) {
                // Return empty info if gradle dir doesn't exist
                return@withContext emptyGradleInfo
            }
            val jdkInfoDeferred = async { loadJdkInfo() }
            val wrapperItemDeferred = async { loadGradleWrapperInfos(gradleDir) }
            val daemonItemDeferred = async { loadDaemonInfos(gradleDir) }
            val cachesGradleWrapperItemDeferred = async { loadCachesGradleWrapperInfos(gradleDir) }
            val otherFolderItemDeferred = async { loadOtherFolder(gradleDir) }


            val wrapperItems = wrapperItemDeferred.await()
            val wrapperTotalSizeBytes = wrapperItems.sumOf { it.sizeBytes }
            val wrapperTotalSizeReadable = FolderFileUtils.formatSize(wrapperTotalSizeBytes)
            val wrapperInfo = WrapperInfo(
                totalSizeBytes = wrapperTotalSizeBytes,
                sizeReadable = wrapperTotalSizeReadable,
                wrapperItems = wrapperItems
            )

            val daemonItems = daemonItemDeferred.await()
            val daemonTotalSizeBytes = daemonItems.sumOf { it.sizeBytes }
            val daemonTotalSizeReadable = FolderFileUtils.formatSize(daemonTotalSizeBytes)
            val daemonInfo = DaemonInfo(
                totalSizeBytes = daemonTotalSizeBytes,
                sizeReadable = daemonTotalSizeReadable,
                daemonItems = daemonItems
            )

            val cachesGradleWrapperItems = cachesGradleWrapperItemDeferred.await()
            val cachesGradleWrapperTotalSizeBytes = cachesGradleWrapperItems.sumOf { it.sizeBytes }
            val cachesGradleWrapperTotalSizeReadable =
                FolderFileUtils.formatSize(cachesGradleWrapperTotalSizeBytes)
            val cachesGradleWrapperInfo = CachesGradleWrapperInfo(
                totalSizeBytes = cachesGradleWrapperTotalSizeBytes,
                sizeReadable = cachesGradleWrapperTotalSizeReadable,
                cachesGradleWrapperItems = cachesGradleWrapperItems
            )

            val jdkInfo = jdkInfoDeferred.await()

            val gradleModulesInfo = getGradleModulesInfo()

            val otherGradleFolderItems = otherFolderItemDeferred.await()
            val otherGradleFolderTotalSizeBytes = otherGradleFolderItems.sumOf { it.sizeBytes }
            val otherGradleFolderTotalSizeReadable =
                FolderFileUtils.formatSize(otherGradleFolderTotalSizeBytes)
            val otherGradleFolderInfo = OtherGradleFolderInfo(
                totalSizeBytes = otherGradleFolderTotalSizeBytes,
                sizeReadable = otherGradleFolderTotalSizeReadable,
                otherGradleFolderItems = otherGradleFolderItems
            )

            val totalSizeBytes = gradleModulesInfo.sizeBytes + cachesGradleWrapperTotalSizeBytes +
                    daemonTotalSizeBytes + wrapperTotalSizeBytes + jdkInfo.totalSizeBytes + otherGradleFolderTotalSizeBytes
            val totalSizeReadable = FolderFileUtils.formatSize(totalSizeBytes)

            GradleInfo(
                rootPath = gradleDir.absolutePath,
                jdkInfo = jdkInfo,
                wrapperInfo = wrapperInfo,
                daemonInfo = daemonInfo,
                cachesGradleWrapperInfo = cachesGradleWrapperInfo,
                totalSizeBytes = totalSizeBytes,
                sizeReadable = totalSizeReadable,
                gradleModulesInfo = gradleModulesInfo,
                otherGradleFolderInfo = otherGradleFolderInfo
            )
        } catch (e: Exception) {
            AppLogger.e(tag = tag, throwable = e) { "Error analyzing Gradle data" }
            return@withContext emptyGradleInfo
        }
    }

    suspend fun loadJdkInfo(): JdkInfo = withContext(Dispatchers.IO) {

        suspend fun readJdkInfo(jdkDir: File): JdkItem = withContext(Dispatchers.IO) {
            val version = try {
                val possibleReleaseFiles = listOf(
                    File(jdkDir, "release"),
                    File(jdkDir, "Contents/Home/release"),
                    File(jdkDir, "Home/release")
                )

                val releaseFile = possibleReleaseFiles.firstOrNull { it.exists() }
                    ?: jdkDir.walkTopDown()
                        .maxDepth(4)
                        .firstOrNull { it.isFile && it.name == "release" }

                releaseFile?.useLines { lines ->
                    lines.firstOrNull { it.startsWith("JAVA_VERSION=") }
                        ?.substringAfter("=")
                        ?.replace("\"", "")
                }
            } catch (e: Exception) {
                AppLogger.e(
                    tag = tag,
                    throwable = e
                ) { "Error reading JDK version from ${jdkDir.absolutePath}" }
                null
            }

            val sizeBytes = FolderFileUtils.calculateFolderSize(jdkDir)
            JdkItem(
                path = jdkDir.absolutePath,
                name = version ?: "Unknown",
                sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
            )
        }

        val jdksDeferred = mutableListOf<Deferred<JdkItem>>()
        val jdkPath1 = pathPreferenceManger.jdkPath1.first()
        val jdkPath2 = pathPreferenceManger.jdkPath2.first()
        val jdkPath3 = pathPreferenceManger.jdkPath3.first()

        val jdkDir1 = File(jdkPath1)

        if (jdkDir1.exists() && jdkDir1.isDirectory) {
            jdkDir1.listFiles()?.forEach { dir ->
                if (dir.isDirectory) {
                    jdksDeferred.add(async { readJdkInfo(dir) })
                }
            }
        }
        val jdkDir2 = File(jdkPath2)
        if (jdkDir2.exists()) {
            jdkDir2.listFiles()?.forEach { dir ->
                if (dir.isDirectory) {
                    jdksDeferred.add(async { readJdkInfo(dir) })
                }
            }
        }
        val jdkDir3 = File(jdkPath3)
        if (jdkDir3.exists()) {
            jdkDir3.listFiles()?.forEach { dir ->
                if (dir.isDirectory) {
                    jdksDeferred.add(async { readJdkInfo(dir) })
                }
            }
        }

        val jdks = jdksDeferred.awaitAll().distinctBy { it.name }
            .sortedByDescending {
                it.sizeBytes
            }
        val jdkSize = jdks.sumOf { it.sizeBytes }
        val jdkSizeReadable = FolderFileUtils.formatSize(jdkSize)
        JdkInfo(
            sizeReadable = jdkSizeReadable,
            totalSizeBytes = jdkSize,
            jdkItems = jdks,
        )
    }
}
