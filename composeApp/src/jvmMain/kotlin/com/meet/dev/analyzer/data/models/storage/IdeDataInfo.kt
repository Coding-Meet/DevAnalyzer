package com.meet.dev.analyzer.data.models.storage

import com.meet.dev.analyzer.core.utility.ExpandableSection
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class IdeDataInfo(
    val totalSizeReadable: String,
    val totalSizeBytes: Long,
    val totalInstallations: Int,
    val firstCategoryGroup: IdeGroup, // mac: CACHES | win: PROGRAM_FILES
    val secondCategoryGroup: IdeGroup, // mac: LOGS   | win: LOCAL
    val thirdCategoryGroup: IdeGroup, // mac: SUPPORT| win: ROAMING
)

@Serializable
data class IdeGroup(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val type: ExpandableSection,
    val totalLabel: String,
    val installations: List<IdeInstallation>
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class IdeInstallation(
    val uniqueId: String = Uuid.random().toString(),
    val vendor: String,
    val name: String,
    val ideName: String,          // e.g., "AndroidStudio2025.1.3"
    val version: String,          // e.g., "1.3"
    val category: String, // LOCAL, ROAMING, CACHES, LOGS
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)
