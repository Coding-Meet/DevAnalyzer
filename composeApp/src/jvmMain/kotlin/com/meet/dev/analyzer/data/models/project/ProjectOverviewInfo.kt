package com.meet.dev.analyzer.data.models.project

data class ProjectOverviewInfo(
    val projectPath: String,
    val projectName: String,
    val sizeReadable: String,
    val totalSizeBytes: Long,
    val gradleVersion: String?,
    val kotlinVersion: String?,
    val androidGradlePluginVersion: String?,
    val compileSdkVersion: String?,
    val targetSdkVersion: String?,
    val minSdkVersion: String?,
    val isMultiModule: Boolean
)