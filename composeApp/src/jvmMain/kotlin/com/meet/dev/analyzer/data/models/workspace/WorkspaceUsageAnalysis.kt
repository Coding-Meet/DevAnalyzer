@file:OptIn(ExperimentalUuidApi::class)

package com.meet.dev.analyzer.data.models.workspace

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class WorkspaceProjectInfo(
    val projectName: String,
    val projectPath: String,
    val compileSdk: String?,
    val minSdk: String?,
    val targetSdk: String?,
    val buildToolsVersion: String?,
    val gradleVersion: String?,
    val agpVersion: String?,
    val kotlinVersion: String?
)

enum class ResourceCategory(val displayName: String) {
    ANDROID_SDK_PLATFORM("Android SDK Platforms"),
    ANDROID_BUILD_TOOLS("Android SDK Build Tools"),
    GRADLE_WRAPPER("Gradle Wrappers"),
    GRADLE_DEPENDENCY_CACHE("Gradle Dependency Caches"),
    KOTLIN_NATIVE("Kotlin Native Compilers")
}

data class UnusedResourceItem(
    val uniqueId: String = Uuid.random().toString(),
    val name: String,
    val version: String,
    val category: ResourceCategory,
    val path: String,
    val sizeBytes: Long,
    val sizeFormatted: String,
    val isSelected: Boolean = false,
    val usedByProjects: List<String> = emptyList()
)

data class WorkspaceAnalysisResult(
    val projects: List<WorkspaceProjectInfo>,
    val unusedResources: List<UnusedResourceItem>,
    val activeResources: List<UnusedResourceItem>
)
