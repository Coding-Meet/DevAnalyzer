package com.meet.dev.analyzer.data.models.storage

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class GradleInfo(
    val rootPath: String,
    val sizeReadable: String,
    val totalSizeBytes: Long,

    val daemonInfo: DaemonInfo,
    val jdkInfo: JdkInfo,
    val wrapperInfo: WrapperInfo,
    val cachesGradleWrapperInfo: CachesGradleWrapperInfo,
    val gradleModulesInfo: GradleModulesInfo,
    val otherGradleFolderInfo: OtherGradleFolderInfo
)

data class DaemonInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val daemonItems: List<DaemonItem>,
)

@OptIn(ExperimentalUuidApi::class)
data class DaemonItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)

data class JdkInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val jdkItems: List<JdkItem>,
)

@OptIn(ExperimentalUuidApi::class)
data class JdkItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String?,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)


data class WrapperInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val wrapperItems: List<WrapperItem>
)

@OptIn(ExperimentalUuidApi::class)
data class WrapperItem(
    val uniqueId: String = Uuid.random().toString(),
    val version: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)

data class CachesGradleWrapperInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val cachesGradleWrapperItems: List<CachesGradleWrapperItem>
)

@OptIn(ExperimentalUuidApi::class)
data class CachesGradleWrapperItem(
    val uniqueId: String = Uuid.random().toString(),
    val version: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)

data class OtherGradleFolderInfo(
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val otherGradleFolderItems: List<OtherGradleFolderItem>
)

@OptIn(ExperimentalUuidApi::class)
data class OtherGradleFolderItem(
    val uniqueId: String = Uuid.random().toString(),
    val version: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)

data class GradleModulesInfo(
    val path: String,
    val sizeBytes: Long,
    val sizeReadable: String,
    val groupList: List<GradleLibraryInfo>,
    val libraries: List<GradleLibraryInfo>
)

@OptIn(ExperimentalUuidApi::class)
data class GradleLibraryInfo(
    val uniqueId: String = Uuid.random().toString(),
    val groupId: String,
    val artifactId: String,
    val versions: List<GradleVersionInfo>,
    val path: String,
    val sizeReadable: String,
    val totalSizeBytes: Long
)

data class GradleVersionInfo(
    val version: String,
    val sizeReadable: String,
    val sizeBytes: Long,
    val path: String,
)
