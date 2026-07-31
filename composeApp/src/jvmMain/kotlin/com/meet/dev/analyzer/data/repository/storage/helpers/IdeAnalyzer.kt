package com.meet.dev.analyzer.data.repository.storage.helpers

import com.meet.dev.analyzer.data.datastore.PathPreferenceManger
import com.meet.dev.analyzer.data.models.storage.*
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import com.meet.dev.analyzer.utility.platform.FolderFileUtils
import com.meet.dev.analyzer.utility.platform.getDesktopOS
import com.meet.dev.analyzer.utility.platform.isLinux
import com.meet.dev.analyzer.utility.platform.isWindows
import com.meet.dev.analyzer.utility.ui.IdeDataSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class IdeAnalyzer(
    private val pathPreferenceManger: PathPreferenceManger
) {
    private val tag = tagName(javaClass = javaClass)

    suspend fun analyzeIdeData(): IdeDataInfo = withContext(Dispatchers.IO) {
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
            basePath: String,
            isLinux: Boolean
        ): List<IdeInstallation> {
            val dir = File(basePath)
            return dir.listFiles()
                ?.filter { it.isDirectory && it.name.any { ch -> ch.isDigit() } }
                ?.mapNotNull { ideDir ->
                    val targetDir = when {
                        isLinux && category == "LOGS" ->
                            File(ideDir, "log")

                        else ->
                            ideDir
                    }

                    if (!targetDir.exists()) return@mapNotNull null

                    val sizeBytes = when {
                        isLinux && category == "CACHES" ->
                            ideDir.listFiles()
                                ?.filter { it.name != "log" }
                                ?.sumOf { FolderFileUtils.calculateFolderSize(it) }
                                ?: 0L

                        else ->
                            FolderFileUtils.calculateFolderSize(targetDir)
                    }

                    val (ideName, version) =
                        extractIdeNameAndVersionByFirstDigit(ideDir.name)
                    IdeInstallation(
                        name = ideDir.name,
                        ideName = ideName,
                        version = version,
                        category = category,
                        path = targetDir.absolutePath,
                        sizeBytes = sizeBytes,
                        sizeReadable = FolderFileUtils.formatSize(sizeBytes),
                        vendor = vendor
                    )
                } ?: emptyList()
        }

        suspend fun buildBasePaths(isWindows: Boolean): Map<String, Map<String, String>> {
            val ideGoogle1 = pathPreferenceManger.ideGooglePath1.first()
            val ideGoogle2 = pathPreferenceManger.ideGooglePath2.first()
            val ideGoogle3 = pathPreferenceManger.ideGooglePath3.first()

            val ideJetBrains1 = pathPreferenceManger.ideJetBrainsPath1.first()
            val ideJetBrains2 = pathPreferenceManger.ideJetBrainsPath2.first()
            val ideJetBrains3 = pathPreferenceManger.ideJetBrainsPath3.first()

            return if (isWindows) {
                mapOf(
                    "Google" to mapOf(
                        "PROGRAM_FILES" to ideGoogle1,
                        "LOCAL" to ideGoogle2,
                        "ROAMING" to ideGoogle3,
                    ),
                    "JetBrains" to mapOf(
                        "PROGRAM_FILES" to ideJetBrains1,
                        "LOCAL" to ideJetBrains2,
                        "ROAMING" to ideJetBrains3,
                    )
                )
            } else { // macOS
                mapOf(
                    "Google" to mapOf(
                        "CACHES" to ideGoogle1,
                        "LOGS" to ideGoogle2,
                        "SUPPORT" to ideGoogle3
                    ),
                    "JetBrains" to mapOf(
                        "CACHES" to ideJetBrains1,
                        "LOGS" to ideJetBrains2,
                        "SUPPORT" to ideJetBrains3
                    )
                )
            }
        }
        try {
            val os = getDesktopOS()
            val isWindows = os.isWindows()
            val isLinux = os.isLinux()
            val basePaths = buildBasePaths(isWindows = isWindows)

            val allInstallations = buildList {
                basePaths.forEach { (vendor, categories) ->
                    categories.forEach { (category, path) ->
                        addAll(
                            scanBase(
                                vendor = vendor,
                                category = category,
                                basePath = path,
                                isLinux = isLinux
                            )
                        )
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
            val totalSizeReadable = FolderFileUtils.formatSize(totalSizeBytes)

            val firstCategorySizeBytes = firstCategoryGroups.sumOf { it.sizeBytes }
            val firstCategorySizeReadable = FolderFileUtils.formatSize(firstCategorySizeBytes)
            val secondCategorySizeBytes = secondCategoryGroups.sumOf { it.sizeBytes }
            val secondCategorySizeReadable = FolderFileUtils.formatSize(secondCategorySizeBytes)
            val thirdCategorySizeBytes = thirdCategoryGroups.sumOf { it.sizeBytes }
            val thirdCategorySizeReadable = FolderFileUtils.formatSize(thirdCategorySizeBytes)

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
            AppLogger.e(tag = tag, throwable = e) { "Error analyzing IDE data" }
            throw e
        }
    }
}
