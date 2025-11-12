package com.meet.dev.analyzer.data.models.storage

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class KonanInfo(
    val rootPath: String,
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val dependenciesInfo: DependenciesInfo,
    val kotlinNativeInfo: KotlinNativeInfo,
)

data class DependenciesInfo(
    val name: String,
    val sizeReadable: String,
    val sizeBytes: Long,
    val dependenciesItems: List<DependenciesItem>,
)

@OptIn(ExperimentalUuidApi::class)
data class DependenciesItem(
    val uniqueId: String = Uuid.random().toString(),
    val version: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long
)


data class KotlinNativeInfo(
    val name: String,
    val sizeReadable: String,
    val sizeBytes: Long,
    val kotlinNativeItems: List<KotlinNativeItem>
)

@OptIn(ExperimentalUuidApi::class)
data class KotlinNativeItem(
    val uniqueId: String = Uuid.random().toString(),
    val version: String?,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)
