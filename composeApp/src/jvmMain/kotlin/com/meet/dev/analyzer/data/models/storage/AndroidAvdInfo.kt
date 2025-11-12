package com.meet.dev.analyzer.data.models.storage

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AndroidAvdInfo(
    val avdItemList: List<AvdItem>,
    val sizeReadable: String,
    val totalSizeBytes: Long,
)

@OptIn(ExperimentalUuidApi::class)
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