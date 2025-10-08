package com.meet.project.analyzer.data.models.storage

import kotlinx.serialization.Serializable

@Serializable
data class GradleModulesInfo(
    val path: String,
    val sizeReadable: String,
    val libraries: List<GradleLibraryInfo>,
    val sizeBytes: Long
)