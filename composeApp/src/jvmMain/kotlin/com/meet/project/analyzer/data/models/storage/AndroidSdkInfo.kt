package com.meet.project.analyzer.data.models.storage

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class AndroidSdkInfo(
    val sdkPath: String,
    val sizeReadable: String,
    val totalSizeBytes: Long,

    val ndkInfo: NdkInfo,
    val systemImageInfo: SystemImageInfo,
    val buildToolInfo: BuildToolInfo,
    val platformInfo: PlatformInfo,
    val sourcesInfo: SourcesInfo,
    val cmakeInfo: CmakeInfo,
    val extrasInfo: ExtrasInfo
)

@Serializable
data class PlatformInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val platforms: List<PlatformItem>,
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class PlatformItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)

@Serializable
data class BuildToolInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val buildTools: List<BuildToolItem>,
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class BuildToolItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)

@Serializable
data class SystemImageInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val systemImages: List<SystemImageInfoItem>,
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class SystemImageInfoItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)

@Serializable
data class SourcesInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val sources: List<SourcesInfoItem>,
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class SourcesInfoItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)

@Serializable
data class CmakeInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val cmakeItems: List<CmakeInfoItem>,
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class CmakeInfoItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)

@Serializable
data class ExtrasInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val extrasInfoItems: List<ExtrasInfoItem>,
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class ExtrasInfoItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)


@Serializable
data class NdkInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val ndkItems: List<NdkItem>,
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class NdkItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)