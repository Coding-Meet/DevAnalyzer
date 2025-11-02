package com.meet.project.analyzer.data.models.storage

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class AndroidAvdInfo(
    val avdItemList: List<AvdItem>,
    val sizeReadable: String,
    val totalSizeBytes: Long,
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class AvdItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val apiLevel: String?,
    val device: String?,
    val path: String,
    val configuredStorage: String,
    val actualStorage: String,
    val sizeBytes: Long,
)