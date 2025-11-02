package com.meet.dev.analyzer.data.models.storage

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class KonanInfo(
    val rootPath: String,
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val dependenciesInfo: DependenciesInfo,
    val kotlinNativeInfo: KotlinNativeInfo,
)

@Serializable
data class DependenciesInfo(
    val name: String,
    val sizeReadable: String,
    val sizeBytes: Long,
    val dependenciesItems: List<DependenciesItem>,
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class DependenciesItem(
    val uniqueId: String = Uuid.random().toString(),
    val version: String,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)

@Serializable
data class KotlinNativeInfo(
    val name: String,
    val sizeReadable: String,
    val sizeBytes: Long,
    val kotlinNativeItems: List<KotlinNativeItem>,

    )

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class KotlinNativeItem(
    val uniqueId: String = Uuid.random().toString(),
    val version: String?,
    val path: String,
    val sizeReadable: String,
    val sizeBytes: Long,
)
