package com.meet.dev.analyzer.data.repository.storage

import com.meet.dev.analyzer.core.utility.AppLogger
import com.meet.dev.analyzer.core.utility.IdeDataSection
import com.meet.dev.analyzer.core.utility.Utils
import com.meet.dev.analyzer.core.utility.Utils.tagName
import com.meet.dev.analyzer.data.models.storage.AndroidAvdInfo
import com.meet.dev.analyzer.data.models.storage.AndroidSdkInfo
import com.meet.dev.analyzer.data.models.storage.AvdItem
import com.meet.dev.analyzer.data.models.storage.BuildToolInfo
import com.meet.dev.analyzer.data.models.storage.BuildToolItem
import com.meet.dev.analyzer.data.models.storage.CachesGradleWrapperInfo
import com.meet.dev.analyzer.data.models.storage.CachesGradleWrapperItem
import com.meet.dev.analyzer.data.models.storage.CmakeInfo
import com.meet.dev.analyzer.data.models.storage.CmakeInfoItem
import com.meet.dev.analyzer.data.models.storage.DaemonInfo
import com.meet.dev.analyzer.data.models.storage.DaemonItem
import com.meet.dev.analyzer.data.models.storage.DependenciesInfo
import com.meet.dev.analyzer.data.models.storage.DependenciesItem
import com.meet.dev.analyzer.data.models.storage.ExtrasInfo
import com.meet.dev.analyzer.data.models.storage.ExtrasInfoItem
import com.meet.dev.analyzer.data.models.storage.GradleInfo
import com.meet.dev.analyzer.data.models.storage.IdeDataInfo
import com.meet.dev.analyzer.data.models.storage.IdeGroup
import com.meet.dev.analyzer.data.models.storage.IdeInstallation
import com.meet.dev.analyzer.data.models.storage.JdkInfo
import com.meet.dev.analyzer.data.models.storage.JdkItem
import com.meet.dev.analyzer.data.models.storage.KonanInfo
import com.meet.dev.analyzer.data.models.storage.KotlinNativeInfo
import com.meet.dev.analyzer.data.models.storage.KotlinNativeItem
import com.meet.dev.analyzer.data.models.storage.NdkInfo
import com.meet.dev.analyzer.data.models.storage.NdkItem
import com.meet.dev.analyzer.data.models.storage.OtherGradleFolderInfo
import com.meet.dev.analyzer.data.models.storage.OtherGradleFolderItem
import com.meet.dev.analyzer.data.models.storage.PlatformInfo
import com.meet.dev.analyzer.data.models.storage.PlatformItem
import com.meet.dev.analyzer.data.models.storage.SourcesInfo
import com.meet.dev.analyzer.data.models.storage.SourcesInfoItem
import com.meet.dev.analyzer.data.models.storage.SystemImageInfo
import com.meet.dev.analyzer.data.models.storage.SystemImageInfoItem
import com.meet.dev.analyzer.data.models.storage.WrapperInfo
import com.meet.dev.analyzer.data.models.storage.WrapperItem
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Properties
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class StorageAnalyzerRepositoryImpl : StorageAnalyzerRepository {

    private val TAG = tagName(javaClass = javaClass)


    override suspend fun analyzeIdeData(): IdeDataInfo = withContext(Dispatchers.IO) {
        fun extractIdeNameAndVersionByFirstDigit(folderName: String): Pair<String, String> {
            val name = folderName.trim()
            val firstDigitIndex = name.indexOfFirst { it.isDigit() }
            if (firstDigitIndex == -1) return folderName to ""

            val idePart = name.substring(0, firstDigitIndex).trim()
            val versionPart = name.substring(firstDigitIndex).trim()

            if (idePart.isEmpty() || versionPart.isEmpty()) return folderName to ""
            return idePart to versionPart
        }

        fun scanBase(
            vendor: String,
            category: String,
            basePath: String
        ): List<IdeInstallation> {
            val dir = File(basePath)
            if (!dir.exists()) return emptyList()

            return dir.listFiles()
                ?.filter { it.isDirectory && it.name.any { ch -> ch.isDigit() } }
                ?.mapNotNull { file ->
                    val sizeBytes = Utils.calculateFolderSize(file)
                    val sizeReadable = Utils.formatSize(sizeBytes)
                    val info = extractIdeNameAndVersionByFirstDigit(file.name)
                    val (ideName, version) = info

                    IdeInstallation(
                        name = file.name,
                        ideName = ideName,
                        version = version,
                        category = category,
                        path = file.absolutePath,
                        sizeBytes = sizeBytes,
                        vendor = vendor,
                        sizeReadable = sizeReadable
                    )
                } ?: emptyList()
        }

        fun buildBasePaths(userHome: String, isWindows: Boolean): Map<String, Map<String, String>> {
            return if (isWindows) {
                mapOf(
                    "Google" to mapOf(
                        "PROGRAM_FILES" to "C:/Program Files/Android",
                        "LOCAL" to "$userHome/AppData/Local/Google",
                        "ROAMING" to "$userHome/AppData/Roaming/Google",
                    ),
                    "JetBrains" to mapOf(
                        "PROGRAM_FILES" to "C:/Program Files/JetBrains",
                        "LOCAL" to "$userHome/AppData/Local/JetBrains",
                        "ROAMING" to "$userHome/AppData/Roaming/JetBrains",
                    )
                )
            } else { // macOS
                mapOf(
                    "Google" to mapOf(
                        "CACHES" to "$userHome/Library/Caches/Google",
                        "LOGS" to "$userHome/Library/Logs/Google",
                        "SUPPORT" to "$userHome/Library/Application Support/Google"
                    ),
                    "JetBrains" to mapOf(
                        "CACHES" to "$userHome/Library/Caches/JetBrains",
                        "LOGS" to "$userHome/Library/Logs/JetBrains",
                        "SUPPORT" to "$userHome/Library/Application Support/JetBrains"
                    )
                )
            }
        }
        try {
            val userHome = System.getProperty("user.home")
            val os = System.getProperty("os.name").lowercase()
            val isWindows = os.contains("windows")
            val basePaths = buildBasePaths(userHome = userHome, isWindows = isWindows)

            val allInstallations = buildList {
                basePaths.forEach { (vendor, categories) ->
                    categories.forEach { (category, path) ->
                        addAll(scanBase(vendor, category, path))
                    }
                }
            }

            val firstCategoryKey = if (isWindows) "PROGRAM_FILES" else "CACHES"
            val secondCategoryKey = if (isWindows) "LOCAL" else "LOGS"
            val thirdCategoryKey = if (isWindows) "ROAMING" else "SUPPORT"

            val firstCategoryGroups = allInstallations.filter { it.category == firstCategoryKey }
                .sortedByDescending { it.sizeBytes }
            val secondCategoryGroups = allInstallations.filter { it.category == secondCategoryKey }
                .sortedByDescending { it.sizeBytes }
            val thirdCategoryGroups = allInstallations.filter { it.category == thirdCategoryKey }
                .sortedByDescending { it.sizeBytes }

            val totalSizeBytes = allInstallations.sumOf { it.sizeBytes }
            val totalSizeReadable = Utils.formatSize(totalSizeBytes)

            val firstCategorySizeBytes = firstCategoryGroups.sumOf { it.sizeBytes }
            val firstCategorySizeReadable = Utils.formatSize(firstCategorySizeBytes)
            val secondCategorySizeBytes = secondCategoryGroups.sumOf { it.sizeBytes }
            val secondCategorySizeReadable = Utils.formatSize(secondCategorySizeBytes)
            val thirdCategorySizeBytes = thirdCategoryGroups.sumOf { it.sizeBytes }
            val thirdCategorySizeReadable = Utils.formatSize(thirdCategorySizeBytes)

            val firstCategoryGroup = IdeGroup(
                totalSizeBytes = firstCategorySizeBytes,
                sizeReadable = firstCategorySizeReadable,
                installations = firstCategoryGroups,
                type = if (isWindows) IdeDataSection.WinProgramFiles else IdeDataSection.MacCaches,
                totalLabel = "Total " + if (isWindows) "Program Files" else "Caches"
            )
            val secondCategoryGroup = IdeGroup(
                totalSizeBytes = secondCategorySizeBytes,
                sizeReadable = secondCategorySizeReadable,
                installations = secondCategoryGroups,
                type = if (isWindows) IdeDataSection.WinLocal else IdeDataSection.MacLogs,
                totalLabel = "Total " + if (isWindows) "Local" else "Logs"
            )
            val thirdCategoryGroup = IdeGroup(
                totalSizeBytes = thirdCategorySizeBytes,
                sizeReadable = thirdCategorySizeReadable,
                installations = thirdCategoryGroups,
                type = if (isWindows) IdeDataSection.WinRoaming else IdeDataSection.MacSupport,
                totalLabel = "Total " + if (isWindows) "Roaming" else "Support"
            )

            IdeDataInfo(
                totalSizeReadable = totalSizeReadable,
                totalSizeBytes = totalSizeBytes,
                totalInstallations = allInstallations.size,
                firstCategoryGroup = firstCategoryGroup,
                secondCategoryGroup = secondCategoryGroup,
                thirdCategoryGroup = thirdCategoryGroup
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Error analyzing IDE data" }
            throw e
        }
    }


    override suspend fun analyzeKonanData(): KonanInfo = withContext(Dispatchers.IO) {

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
                            val sizeBytes = Utils.calculateFolderSize(dir)
                            DependenciesItem(
                                version = displayVersion,
                                path = dir.absolutePath,
                                sizeReadable = Utils.formatSize(sizeBytes),
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
                            val sizeBytes = Utils.calculateFolderSize(dir)
                            KotlinNativeItem(
                                version = version,
                                path = dir.absolutePath,
                                sizeReadable = Utils.formatSize(sizeBytes),
                                sizeBytes = sizeBytes
                            )
                        }
                    }?.awaitAll()?.sortedByDescending {
                        it.sizeBytes
                    } ?: emptyList()
            }
        try {
            AppLogger.i(TAG) { "Analyzing Konan data" }
            val konanRootDir = File(System.getProperty("user.home"), ".konan")

            val totalSizeBytes = Utils.calculateFolderSize(konanRootDir)
            val totalSizeReadable = Utils.formatSize(totalSizeBytes)


            val kotlinNativeInfosDeferred =
                async { loadKotlinNativeInfos(konanRootDir = konanRootDir) }
            val dependenciesInfosDeferred =
                async { loadDependenciesInfos(konanRootDir = konanRootDir) }

            val kotlinNativeItems = kotlinNativeInfosDeferred.await()
            val kotlinNativeTotalSizeBytes = kotlinNativeItems.sumOf { it.sizeBytes }
            val kotlinNativeTotalSizeReadable = Utils.formatSize(kotlinNativeTotalSizeBytes)
            val kotlinNativeInfo = KotlinNativeInfo(
                name = "Kotlin/Native (.konan)",
                sizeBytes = kotlinNativeTotalSizeBytes,
                sizeReadable = kotlinNativeTotalSizeReadable,
                kotlinNativeItems = kotlinNativeItems
            )
            val dependenciesItems = dependenciesInfosDeferred.await()
            val dependenciesTotalSizeBytes = dependenciesItems.sumOf { it.sizeBytes }
            val dependenciesTotalSizeReadable = Utils.formatSize(dependenciesTotalSizeBytes)
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
            AppLogger.e(TAG, e) { "Error analyzing Konan data" }
            throw e
        }
    }

    override suspend fun analyzeAvdData(): AndroidAvdInfo = withContext(Dispatchers.IO) {

        fun parseConfiguredSize(raw: String?): String {
            if (raw == null) return "Unknown"
            return try {
                when {
                    raw.endsWith("M", true) -> {
                        val mb = raw.dropLast(1).toLong()
                        Utils.formatSize(mb * 1024 * 1024)
                    }

                    raw.endsWith("K", true) -> {
                        val kb = raw.dropLast(1).toLong()
                        Utils.formatSize(kb * 1024)
                    }

                    raw.endsWith("G", true) -> {
                        val gb = raw.dropLast(1).toLong()
                        Utils.formatSize(gb * 1024 * 1024 * 1024)
                    }

                    raw.toLongOrNull() != null -> Utils.formatSize(raw.toLong())
                    else -> raw
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, throwable = e, message = { "Error parsing configured size: $raw" })
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
                            val actualSizeBytes = Utils.calculateFolderSize(File(path))
                            val actualSize = Utils.formatSize(actualSizeBytes)
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
                            AppLogger.e(TAG, e) { "Error processing AVD file: ${iniFile.name}" }
                            null
                        }
                    }
                }?.awaitAll()?.filterNotNull()?.sortedByDescending {
                    it.sizeBytes
                } ?: emptyList()
        }
        AppLogger.i(TAG) { "Loading AVD information" }
        try {
            val home = System.getProperty("user.home")
            val path = System.getenv("ANDROID_AVD_HOME")
            val avdDir =
                if (!path.isNullOrEmpty()) {
                    File(path)
                } else {
                    File(home, ".android" + File.separator + "avd")
                }
            AppLogger.i(TAG) { "AVD directory: ${avdDir.absolutePath}" }

            val avdItemLists = async {
                loadAvdInfos(avdDir)
            }.await()
            val totalSizeBytes = avdItemLists.sumOf { it.sizeBytes }
            AndroidAvdInfo(
                avdItemList = avdItemLists,
                totalSizeBytes = totalSizeBytes,
                sizeReadable = Utils.formatSize(totalSizeBytes)
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, e) { "Error loading AVD information" }
            throw e
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    data class SdkItem(
        val uniqueId: String = Uuid.random().toString(),
        val name: String,
        val path: String,
        val size: String,
        val sizeBytes: Long,
    )

    override suspend fun analyzeAndroidSdkData(): AndroidSdkInfo = withContext(Dispatchers.IO) {

        suspend fun findAndroidSdkPath(): String? = withContext(Dispatchers.IO) {
            AppLogger.d(TAG) { "Finding Android SDK path" }
            val userHome = System.getProperty("user.home")
            val os = System.getProperty("os.name").lowercase()

            val possiblePaths = when {
                os.contains("windows") -> listOf(
                    System.getenv("ANDROID_HOME"),
                    System.getenv("ANDROID_SDK_ROOT"),
                    "$userHome/Sdk"
                )

                else -> listOf(
                    System.getenv("ANDROID_HOME"),
                    System.getenv("ANDROID_SDK_ROOT"),
                    "$userHome/Library/Android/sdk",
                    "$userHome/Android/Sdk"
                )
            }

            // Check each path and return the first existing one
            val sdkPath = possiblePaths
                .filterNotNull()
                .firstOrNull { File(it).exists() }

            if (sdkPath != null) {
                AppLogger.d(TAG) { "Android SDK found at: $sdkPath" }
            } else {
                AppLogger.e(TAG) { "Android SDK not found in expected locations." }
            }

            sdkPath
        }


        suspend fun loadSdkItems(directory: File?): List<SdkItem> = withContext(Dispatchers.IO) {
            try {
                directory?.listFiles()
                    ?.filter { !it.name.startsWith(".") }
                    ?.map { dir ->
                        async {
                            val sizeBytes = Utils.calculateFolderSize(dir)
                            SdkItem(
                                name = dir.name,
                                path = dir.absolutePath,
                                size = Utils.formatSize(sizeBytes),
                                sizeBytes = sizeBytes
                            )
                        }
                    }?.awaitAll()?.sortedByDescending {
                        it.sizeBytes
                    } ?: emptyList()
            } catch (e: Exception) {
                AppLogger.e(TAG, e) { "Error loading SDK items from ${directory?.absolutePath}" }
                emptyList()
            }
        }

        suspend fun loadSdkExtras(sdkDir: File): List<SdkItem> = withContext(Dispatchers.IO) {
            val extraFolders = listOf("platform-tools", "emulator")
            extraFolders.map { folder ->
                async {
                    val dir = File(sdkDir, folder)
                    if (dir.exists()) {
                        val sizeBytes = Utils.calculateFolderSize(dir)
                        SdkItem(
                            name = folder,
                            path = dir.absolutePath,
                            size = Utils.formatSize(sizeBytes),
                            sizeBytes = sizeBytes
                        )
                    } else null
                }
            }.awaitAll().filterNotNull().sortedByDescending {
                it.sizeBytes
            }
        }

        AppLogger.i(TAG) { "Loading SDK information" }
        try {
            val sdkRoot = findAndroidSdkPath()
                ?: throw IllegalStateException("Android SDK path not found. Please ensure Android Studio or SDK is installed.")

            val sdkDir = File(sdkRoot)
            if (!sdkDir.exists() || !sdkDir.isDirectory) {
                throw IllegalStateException("Invalid SDK directory: $sdkRoot")
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
            val platformsSizeReadable = Utils.formatSize(platformsSize)
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
            val buildToolsSizeReadable = Utils.formatSize(buildToolsSize)
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
            val systemImageSizeReadable = Utils.formatSize(systemImageSize)
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
            val ndkSizeReadable = Utils.formatSize(ndkSize)
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
            val sourcesSizeReadable = Utils.formatSize(sourcesSize)
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
            val cmakeSizeReadable = Utils.formatSize(cmakeSize)
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
            val extrasSizeReadable = Utils.formatSize(extrasSize)
            val extrasInfo = ExtrasInfo(
                extrasInfoItems = extras,
                sizeReadable = extrasSizeReadable,
                totalSizeBytes = extrasSize
            )

            val sdkDirSizeBytes =
                platformsSize + buildToolsSize + systemImageSize + ndkSize + sourcesSize + cmakeSize + extrasSize
            val sdkDirSizeReadable = Utils.formatSize(sdkDirSizeBytes)


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
            AppLogger.e(TAG, e) { "Error loading SDK information" }
            throw e
        }
    }

    override suspend fun analyzeGradleData(): GradleInfo = withContext(Dispatchers.IO) {
        try {
            suspend fun loadOtherFolder(gradleDir: File): List<OtherGradleFolderItem> =
                withContext(Dispatchers.IO) {
                    val cachesDir = File(gradleDir, "caches")

                    val otherFolderList = listOf("transforms-3", "jars-9", "build-cache-1")
                    val cachesList = cachesDir.listFiles()
                        ?.filter { it.isDirectory && otherFolderList.contains(it.name) }
                        ?.map { distDir ->
                            async {
                                val sizeBytes = Utils.calculateFolderSize(distDir)
                                OtherGradleFolderItem(
                                    version = distDir.name,
                                    path = distDir.absolutePath,
                                    sizeReadable = Utils.formatSize(sizeBytes),
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
                                val sizeBytes = Utils.calculateFolderSize(metaDir)
                                OtherGradleFolderItem(
                                    version = metaDir.name,
                                    path = metaDir.absolutePath,
                                    sizeReadable = Utils.formatSize(sizeBytes),
                                    sizeBytes = sizeBytes
                                )
                            }
                        }?.awaitAll()?.sortedByDescending {
                            it.sizeBytes
                        } ?: emptyList()

                    val tempDir = File(gradleDir, ".tmp")
                    val temp = async {
                        val sizeBytes = Utils.calculateFolderSize(tempDir)
                        OtherGradleFolderItem(
                            version = tempDir.name,
                            path = tempDir.absolutePath,
                            sizeReadable = Utils.formatSize(sizeBytes),
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
                                val sizeBytes = Utils.calculateFolderSize(distDir)
                                CachesGradleWrapperItem(
                                    version = version,
                                    path = distDir.absolutePath,
                                    sizeReadable = Utils.formatSize(sizeBytes),
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
                                val sizeBytes = Utils.calculateFolderSize(distDir)
                                DaemonItem(
                                    name = version,
                                    path = distDir.absolutePath,
                                    sizeReadable = Utils.formatSize(sizeBytes),
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
                                val sizeBytes = Utils.calculateFolderSize(distDir)
                                WrapperItem(
                                    version = version,
                                    path = distDir.absolutePath,
                                    sizeReadable = Utils.formatSize(sizeBytes),
                                    sizeBytes = sizeBytes
                                )
                            }
                        }?.awaitAll()?.sortedByDescending {
                            it.sizeBytes
                        } ?: emptyList()
                }

            val gradleDir = File(System.getProperty("user.home"), ".gradle")
            val jdkInfoDeferred = async { loadJdkInfo() }
            val wrapperItemDeferred = async { loadGradleWrapperInfos(gradleDir) }
            val daemonItemDeferred = async { loadDaemonInfos(gradleDir) }
            val cachesGradleWrapperItemDeferred = async { loadCachesGradleWrapperInfos(gradleDir) }
            val otherFolderItemDeferred = async { loadOtherFolder(gradleDir) }


            val wrapperItems = wrapperItemDeferred.await()
            val wrapperTotalSizeBytes = wrapperItems.sumOf { it.sizeBytes }
            val wrapperTotalSizeReadable = Utils.formatSize(wrapperTotalSizeBytes)
            val wrapperInfo = WrapperInfo(
                totalSizeBytes = wrapperTotalSizeBytes,
                sizeReadable = wrapperTotalSizeReadable,
                wrapperItems = wrapperItems
            )

            val daemonItems = daemonItemDeferred.await()
            val daemonTotalSizeBytes = daemonItems.sumOf { it.sizeBytes }
            val daemonTotalSizeReadable = Utils.formatSize(daemonTotalSizeBytes)
            val daemonInfo = DaemonInfo(
                totalSizeBytes = daemonTotalSizeBytes,
                sizeReadable = daemonTotalSizeReadable,
                daemonItems = daemonItems
            )

            val cachesGradleWrapperItems = cachesGradleWrapperItemDeferred.await()
            val cachesGradleWrapperTotalSizeBytes = cachesGradleWrapperItems.sumOf { it.sizeBytes }
            val cachesGradleWrapperTotalSizeReadable =
                Utils.formatSize(cachesGradleWrapperTotalSizeBytes)
            val cachesGradleWrapperInfo = CachesGradleWrapperInfo(
                totalSizeBytes = cachesGradleWrapperTotalSizeBytes,
                sizeReadable = cachesGradleWrapperTotalSizeReadable,
                cachesGradleWrapperItems = cachesGradleWrapperItems
            )

            val jdkInfo = jdkInfoDeferred.await()

            val gradleModulesInfo = Utils.getGradleModulesInfo()

            val otherGradleFolderItems = otherFolderItemDeferred.await()
            val otherGradleFolderTotalSizeBytes = otherGradleFolderItems.sumOf { it.sizeBytes }
            val otherGradleFolderTotalSizeReadable =
                Utils.formatSize(otherGradleFolderTotalSizeBytes)
            val otherGradleFolderInfo = OtherGradleFolderInfo(
                totalSizeBytes = otherGradleFolderTotalSizeBytes,
                sizeReadable = otherGradleFolderTotalSizeReadable,
                otherGradleFolderItems = otherGradleFolderItems
            )

            val totalSizeBytes = gradleModulesInfo.sizeBytes + cachesGradleWrapperTotalSizeBytes +
                    daemonTotalSizeBytes + wrapperTotalSizeBytes + jdkInfo.totalSizeBytes + otherGradleFolderTotalSizeBytes
            val totalSizeReadable = Utils.formatSize(totalSizeBytes)

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
            AppLogger.e(TAG, e) { "Error analyzing Gradle data" }
            throw e
        }
    }

    private suspend fun loadJdkInfo(): JdkInfo = withContext(Dispatchers.IO) {

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
                AppLogger.e(TAG, e) { "Error reading JDK version from ${jdkDir.absolutePath}" }
                null
            }

            val sizeBytes = Utils.calculateFolderSize(jdkDir)
            JdkItem(
                path = jdkDir.absolutePath,
                name = version,
                sizeReadable = Utils.formatSize(sizeBytes),
                sizeBytes = sizeBytes,
            )
        }


        val jdksDeferred = mutableListOf<Deferred<JdkItem>>()
        val userHome = System.getProperty("user.home")
        val os = System.getProperty("os.name").lowercase()

        // Add JAVA_HOME if exists
        System.getenv("JAVA_HOME")?.let { javaHome ->
            val envJavaHome = File(javaHome)
            if (envJavaHome.exists()) {
                jdksDeferred.add(async { readJdkInfo(envJavaHome) })
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
                        jdksDeferred.add(async { readJdkInfo(dir) })
                    }
                }
            }
        }

        val jdks = jdksDeferred.awaitAll().distinctBy { it.name }.filter { it.name != null }
            .sortedByDescending {
                it.sizeBytes
            }
        val jdkSize = jdks.sumOf { it.sizeBytes }
        val jdkSizeReadable = Utils.formatSize(jdkSize)
        JdkInfo(
            sizeReadable = jdkSizeReadable,
            totalSizeBytes = jdkSize,
            jdkItems = jdks,
        )
    }
}