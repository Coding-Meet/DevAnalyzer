package com.meet.project.analyzer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GradleModulesInfo(
    val path: String,
    val sizeReadable: String,
    val libraries: List<GradleLibraryInfo>,
    val sizeBytes: Long = 0L
)