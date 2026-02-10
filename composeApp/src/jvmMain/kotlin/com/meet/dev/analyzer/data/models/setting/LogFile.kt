package com.meet.dev.analyzer.data.models.setting

data class LogFile(
    val name: String,
    val path: String,
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val lines: Int,
    val timestamp: Long,
    val content: String,
    val totalCharacters: Int,
)