package com.meet.project.analyzer.data.repository

import com.meet.project.analyzer.core.utility.AppLogger
import com.meet.project.analyzer.core.utility.Utils
import com.meet.project.analyzer.core.utility.Utils.formatSize
import com.meet.project.analyzer.core.utility.Utils.tagName
import com.meet.project.analyzer.data.models.AvdInfo
import com.meet.project.analyzer.data.models.CacheInfo
import com.meet.project.analyzer.data.models.DevEnvironmentInfo
import com.meet.project.analyzer.data.models.GradleCacheInfo
import com.meet.project.analyzer.data.models.GradleInfo
import com.meet.project.analyzer.data.models.GradleModulesInfo
import com.meet.project.analyzer.data.models.GradleWrapperInfo
import com.meet.project.analyzer.data.models.JdkInfo
import com.meet.project.analyzer.data.models.KonanInfo
import com.meet.project.analyzer.data.models.SdkInfo
import com.meet.project.analyzer.data.models.SdkItem
import com.meet.project.analyzer.data.models.StorageInfo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties

class StorageAnalyzerRepositoryImpl : StorageAnalyzerRepository {

    private val TAG = tagName(javaClass = javaClass)

    override suspend fun getAvdInfoList(): List<AvdInfo> = withContext(Dispatchers.IO) {

        fun parseConfiguredSize(raw: String?): String {
            if (raw == null) return "Unknown"
            return try {
                when {
                    raw.endsWith("M", true) -> {
                        val mb = raw.dropLast(1).toLong()
                        formatSize(mb * 1024 * 1024)
                    }

                    raw.endsWith("K", true) -> {
                        val kb = raw.dropLast(1).toLong()
                        formatSize(kb * 1024)
                    }

                    raw.endsWith("G", true) -> {
                        val gb = raw.dropLast(1).toLong()
                        formatSize(gb * 1024 * 1024 * 1024)
                    }

                    raw.toLongOrNull() != null -> formatSize(raw.toLong())
                    else -> raw
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, throwable = e, message = { "Error parsing configured size: $raw" })
                raw
            }
        }

        AppLogger.i(TAG) { "Loading AVD information" }
        try {
            val home = System.getProperty("user.home")
            val avdDir = File("$home/.android/avd")

            if (!avdDir.exists()) {
                AppLogger.e(TAG) { "AVD directory not found: ${avdDir.absolutePath}" }
                return@withContext emptyList()
            }

            avdDir.listFiles { file -> file.extension == "ini" }
                ?.mapNotNull { iniFile ->
                    async {
                        try {
                            val props = Properties().apply {
                                load(iniFile.inputStream())
                            }
                            val path = props.getProperty("path") ?: return@async null
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
                            val actualSizeBytes = Utils.calculateFolderSize(File(path))
                            val actualSize = formatSize(actualSizeBytes)
                            AvdInfo(
                                name = name,
                                apiLevel = apiLevel,
                                device = device,
                                path = path,
                                configuredStorage = configuredStorage,
                                actualStorage = actualSize,
                                sizeBytes = actualSizeBytes
                            )
                        } catch (e: Exception) {
                            AppLogger.e(TAG, e) { "Error processing AVD file: ${iniFile.name}" }
                            null
                        }
                    }
                }?.awaitAll()?.filterNotNull()?.sortedByDescending {
                    it.sizeBytes
                } ?: emptyList()
        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Error loading AVD information" }
            emptyList()
        }
    }

    override suspend fun getSdkInfo(): SdkInfo = withContext(Dispatchers.IO) {

        suspend fun findAndroidSdkPath(): String? = withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding Android SDK path" }
            val userHome = System.getProperty("user.home")
            val os = System.getProperty("os.name").lowercase()

            val possiblePaths = when {
                os.contains("windows") -> listOf(
                    "$userHome\\AppData\\Local\\Android\\Sdk",
                    "C:\\Android\\Sdk"
                )

                os.contains("mac") -> listOf(
                    "$userHome/Library/Android/sdk",
                    "$userHome/Android/Sdk"
                )

                else -> listOf(
                    "$userHome/Android/Sdk",
                    "$userHome/android-sdk"
                )
            }

            System.getenv("ANDROID_HOME")
                ?: System.getenv("ANDROID_SDK_ROOT")
                ?: possiblePaths.find { File(it).exists() }
        }


        AppLogger.i(TAG) { "Loading SDK information" }
        try {
            val sdkRoot = findAndroidSdkPath() ?: return@withContext SdkInfo(
                sdkPath = "Unknown",
                totalSize = "Unknown",
                freeSpace = "Unknown",
                platforms = emptyList(),
                buildTools = emptyList(),
                systemImages = emptyList(),
                extras = emptyList()
            )

            val sdkDir = File(sdkRoot)

            // Process different SDK components in parallel
            val platformsDeferred = async {
                loadSdkItems(File(sdkDir, "platforms"))
            }
            val buildToolsDeferred = async {
                loadSdkItems(File(sdkDir, "build-tools"))
            }
            val systemImagesDeferred = async {
                loadSdkItems(File(sdkDir, "system-images"))
            }
            val extrasDeferred = async {
                loadSdkExtras(sdkDir)
            }
            val totalSizeDeferred = async {
                Utils.calculateFolderSize(sdkDir)
            }

            val platforms = platformsDeferred.await()
            val buildTools = buildToolsDeferred.await()
            val systemImages = systemImagesDeferred.await()
            val extras = extrasDeferred.await()
            val totalSizeBytes = totalSizeDeferred.await()

            SdkInfo(
                sdkPath = sdkRoot,
                totalSize = formatSize(totalSizeBytes),
                freeSpace = formatSize(sdkDir.freeSpace),
                platforms = platforms,
                buildTools = buildTools,
                systemImages = systemImages,
                extras = extras,
                totalSizeBytes = totalSizeBytes
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Error loading SDK information" }
            SdkInfo(
                sdkPath = "Error",
                totalSize = "Error",
                freeSpace = "Error",
                platforms = emptyList(),
                buildTools = emptyList(),
                systemImages = emptyList(),
                extras = emptyList()
            )
        }
    }

    private suspend fun loadSdkItems(directory: File): List<SdkItem> = withContext(Dispatchers.IO) {
        try {
            directory.listFiles()
                ?.filter { !it.name.startsWith(".") }
                ?.map { dir ->
                    async {
                        val sizeBytes = Utils.calculateFolderSize(dir)
                        SdkItem(
                            name = dir.name,
                            path = dir.absolutePath,
                            size = formatSize(sizeBytes),
                            sizeBytes = sizeBytes
                        )
                    }
                }?.awaitAll()?.sortedByDescending {
                    it.sizeBytes
                } ?: emptyList()
        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Error loading SDK items from ${directory.absolutePath}" }
            emptyList()
        }
    }

    private suspend fun loadSdkExtras(sdkDir: File): List<SdkItem> = withContext(Dispatchers.IO) {
        val extraFolders = listOf("platform-tools", "emulator", "cmdline-tools")
        extraFolders.map { folder ->
            async {
                val dir = File(sdkDir, folder)
                if (dir.exists()) {
                    val sizeBytes = Utils.calculateFolderSize(dir)
                    SdkItem(
                        name = folder,
                        path = dir.absolutePath,
                        size = formatSize(sizeBytes),
                        sizeBytes = sizeBytes
                    )
                } else null
            }
        }.awaitAll().filterNotNull().sortedByDescending {
            it.sizeBytes
        }
    }

    override suspend fun getDevEnvironmentInfo(): DevEnvironmentInfo = withContext(Dispatchers.IO) {
        AppLogger.i(TAG) { "Loading development environment information" }
        try {
            val userHome = System.getProperty("user.home")

            val gradleCacheDeferred = async { loadGradleCache(userHome) }
            val ideaCacheDeferred = async { loadIdeaCache(userHome) }
            val konanInfoDeferred = async { loadKonanInfo() }
            val skikoInfoDeferred = async { loadSkikoInfo() }
            val konanInfosDeferred = async { loadKonanInfos() }
            val gradleInfosDeferred = async { loadGradleInfos() }
            val gradleWrapperInfosDeferred = async { loadGradleWrapperInfos() }
            val jdksDeferred = async { loadJdks() }

            DevEnvironmentInfo(
                gradleCache = gradleCacheDeferred.await(),
                ideaCache = ideaCacheDeferred.await(),
                konanInfo = konanInfoDeferred.await(),
                skikoInfo = skikoInfoDeferred.await(),
                konanInfos = konanInfosDeferred.await(),
                gradleInfos = gradleInfosDeferred.await(),
                gradleWrapperInfos = gradleWrapperInfosDeferred.await(),
                jdks = jdksDeferred.await()
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Error loading development environment info" }
            throw e
        }
    }

    private suspend fun loadGradleCache(userHome: String): StorageInfo =
        withContext(Dispatchers.IO) {
            val gradleCachePath = Paths.get(userHome, ".gradle")
            val sizeBytes = Utils.calculateFolderSize(gradleCachePath.toFile())
            StorageInfo(
                path = gradleCachePath.toString(),
                exists = Files.exists(gradleCachePath),
                sizeReadable = formatSize(sizeBytes),
                sizeBytes = sizeBytes
            )
        }

    private suspend fun loadIdeaCache(userHome: String): StorageInfo = withContext(Dispatchers.IO) {
        val ideaCachePath = when {
            System.getProperty("os.name").lowercase().contains("mac") ->
                Paths.get(userHome, "Library", "Caches")

            System.getProperty("os.name").lowercase().contains("windows") ->
                Paths.get(userHome, "AppData", "Local", "JetBrains")

            else -> Paths.get(userHome, ".cache")
        }
        val sizeBytes = Utils.calculateFolderSize(ideaCachePath.toFile())
        StorageInfo(
            path = ideaCachePath.toString(),
            exists = Files.exists(ideaCachePath),
            sizeReadable = formatSize(sizeBytes),
            sizeBytes = sizeBytes
        )
    }

    private suspend fun loadKonanInfo(): CacheInfo = withContext(Dispatchers.IO) {
        val konanDir = File(System.getProperty("user.home"), ".konan")
        val sizeBytes = if (konanDir.exists()) Utils.calculateFolderSize(konanDir) else 0L
        CacheInfo(
            name = "Kotlin/Native (.konan)",
            path = konanDir.absolutePath,
            sizeReadable = formatSize(sizeBytes),
            sizeBytes = sizeBytes
        )
    }

    private suspend fun loadSkikoInfo(): CacheInfo = withContext(Dispatchers.IO) {
        val skikoDir = File(System.getProperty("user.home"), ".skiko")
        val sizeBytes = if (skikoDir.exists()) Utils.calculateFolderSize(skikoDir) else 0L
        CacheInfo(
            name = "Skiko (.skiko)",
            path = skikoDir.absolutePath,
            sizeReadable = formatSize(sizeBytes),
            sizeBytes = sizeBytes
        )
    }

    private suspend fun loadKonanInfos(): List<KonanInfo> = withContext(Dispatchers.IO) {
        val konanDir = File(System.getProperty("user.home"), ".konan")
        if (!konanDir.exists()) return@withContext emptyList()

        val versionRegex = Regex("""\d+\.\d+(\.\d+)?""")
        konanDir.listFiles()
            ?.filter { it.isDirectory && it.name.contains("kotlin-native-prebuilt") }
            ?.map { dir ->
                async {
                    val version = versionRegex.find(dir.name)?.value
                    val sizeBytes = Utils.calculateFolderSize(dir)
                    KonanInfo(
                        version = version,
                        path = dir.absolutePath,
                        sizeReadable = formatSize(sizeBytes),
                        sizeBytes = sizeBytes
                    )
                }
            }?.awaitAll()?.sortedByDescending {
                it.sizeBytes
            } ?: emptyList()
    }

    private suspend fun loadGradleInfos(): List<GradleInfo> = withContext(Dispatchers.IO) {
        val gradleDir = File(System.getProperty("user.home"), ".gradle")
        if (!gradleDir.exists()) return@withContext emptyList()

        val targets = listOf("caches", ".tmp", "wrapper", "daemon")
        targets.map { sub ->
            async {
                val folder = File(gradleDir, sub)
                if (folder.exists()) {
                    val sizeBytes = Utils.calculateFolderSize(folder)
                    GradleInfo(
                        type = sub,
                        path = folder.absolutePath,
                        sizeReadable = formatSize(sizeBytes),
                        sizeBytes = sizeBytes
                    )
                } else null
            }
        }.awaitAll().filterNotNull().sortedByDescending {
            it.sizeBytes
        }
    }

    private suspend fun loadGradleWrapperInfos(): List<GradleWrapperInfo> =
        withContext(Dispatchers.IO) {
            val wrapperDir = File(System.getProperty("user.home"), ".gradle/wrapper/dists")
            if (!wrapperDir.exists()) return@withContext emptyList()

            val versionRegex = Regex("""\d+\.\d+(\.\d+)?""")
            wrapperDir.listFiles()
                ?.filter { it.isDirectory }
                ?.map { distDir ->
                    async {
                        val version = versionRegex.find(distDir.name)?.value
                        val sizeBytes = Utils.calculateFolderSize(distDir)
                        GradleWrapperInfo(
                            version = version ?: "Unknown",
                            path = distDir.absolutePath,
                            sizeReadable = formatSize(sizeBytes),
                            sizeBytes = sizeBytes
                        )
                    }
                }?.awaitAll()?.sortedByDescending {
                    it.sizeBytes
                } ?: emptyList()
        }

    private suspend fun loadJdks(): List<JdkInfo> = withContext(Dispatchers.IO) {
        val jdks = mutableListOf<Deferred<JdkInfo>>()
        val userHome = System.getProperty("user.home")
        val os = System.getProperty("os.name").lowercase()

        // Add JAVA_HOME if exists
        System.getenv("JAVA_HOME")?.let { javaHome ->
            val envJavaHome = File(javaHome)
            if (envJavaHome.exists()) {
                jdks.add(async { readJdkInfo(envJavaHome) })
            }
        }

        val possiblePaths = when {
            os.contains("mac") -> listOf(
                "$userHome/Library/Java/JavaVirtualMachines",
                "/Library/Java/JavaVirtualMachines",
                "$userHome/.gradle/jdks",
                "$userHome/.sdkman/candidates/java",
                "/usr/local/opt/openjdk",
                "/opt/homebrew/opt/openjdk"
            )

            os.contains("windows") -> listOf(
                "C:\\Program Files\\Java",
                "C:\\Program Files\\Eclipse Adoptium",
                "C:\\Program Files\\Android\\Android Studio\\jbr",
                "$userHome\\.gradle\\jdks",
                "$userHome\\.sdkman\\candidates\\java"
            )

            else -> listOf(
                "/usr/lib/jvm",
                "/usr/java",
                "$userHome/.gradle/jdks",
                "$userHome/.sdkman/candidates/java"
            )
        }

        possiblePaths.forEach { base ->
            val baseDir = File(base)
            if (baseDir.exists() && baseDir.isDirectory) {
                baseDir.listFiles()?.forEach { dir ->
                    if (dir.isDirectory) {
                        jdks.add(async { readJdkInfo(dir) })
                    }
                }
            }
        }

        jdks.awaitAll().distinctBy { it.path }.sortedByDescending {
            it.sizeBytes
        }
    }

    private suspend fun readJdkInfo(jdkDir: File): JdkInfo = withContext(Dispatchers.IO) {
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
            AppLogger.e(TAG, e) { "Error reading JDK version from ${jdkDir.absolutePath}" }
            null
        }

        val sizeBytes = Utils.calculateFolderSize(jdkDir)
        JdkInfo(
            path = jdkDir.absolutePath,
            version = version,
            sizeReadable = formatSize(sizeBytes),
            sizeBytes = sizeBytes
        )
    }

    override suspend fun getGradleCacheInfos(): List<GradleCacheInfo> =
        withContext(Dispatchers.IO) {
            AppLogger.i(TAG) { "Loading Gradle cache information" }
            try {
                val cachesDir = File(System.getProperty("user.home"), ".gradle/caches")
                if (!cachesDir.exists()) return@withContext emptyList()

                val versionRegex = Regex("""\d+\.\d+(\.\d+)?""")
                cachesDir.listFiles()
                    ?.filter { it.isDirectory && versionRegex.matches(it.name) }
                    ?.map { versionDir ->
                        async {
                            val sizeBytes = Utils.calculateFolderSize(versionDir)
                            GradleCacheInfo(
                                version = versionDir.name,
                                path = versionDir.absolutePath,
                                sizeReadable = formatSize(sizeBytes),
                                sizeBytes = sizeBytes
                            )
                        }
                    }?.awaitAll()?.sortedByDescending {
                        it.sizeBytes
                    } ?: emptyList()
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading Gradle cache information" }
                emptyList()
            }
        }

    override suspend fun getGradleModulesInfo(): GradleModulesInfo? = withContext(Dispatchers.IO) {
        AppLogger.i(TAG) { "Loading Gradle modules information" }
        try {
            Utils.getGradleModulesInfo()
        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Error loading Gradle modules information" }
            null
        }
    }
}