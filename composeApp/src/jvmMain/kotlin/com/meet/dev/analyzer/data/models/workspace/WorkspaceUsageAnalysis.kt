@file:OptIn(ExperimentalUuidApi::class)

package com.meet.dev.analyzer.data.models.workspace

import com.meet.dev.analyzer.data.models.project.ProjectOverviewInfo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class ResourceCategory(
    val displayName: String,
    val description: String
) {
    ANDROID_SDK_PLATFORM(
        displayName = "Android SDK Platforms",
        description = "Installed Android SDK platform versions. Older unused versions can be safely deleted."
    ),
    ANDROID_BUILD_TOOLS(
        displayName = "Android Build Tools",
        description = "Command-line tools needed to compile Android apps. Versions not used by active projects can be cleaned up."
    ),
    GRADLE_WRAPPER(
        displayName = "Gradle Wrappers",
        description = "Downloaded Gradle wrapper distributions and their caches. Safe to delete; Gradle will re-download them if needed."
    ),
    GRADLE_DEPENDENCY_CACHE(
        displayName = "Gradle Dependency Cache",
        description = "Cached third-party library dependencies. Safe to clean; Gradle will re-download dependencies on the next project build."
    ),
    KOTLIN_NATIVE(
        displayName = "Kotlin Native",
        description = "Prebuilt Kotlin Native compilers used for multiplatform projects. Can be safely deleted to free space."
    ),
    ANDROID_NDK(
        displayName = "Android NDK",
        description = "Native Development Kit compilers used for C/C++ compilation. Often very large; safe to delete if not used by active projects."
    ),
    ANDROID_CMAKE(
        displayName = "Android CMake",
        description = "CMake compiler versions used for building native C/C++ libraries. Safe to delete if not used."
    ),
    ANDROID_SDK_SOURCES(
        displayName = "Android SDK Sources",
        description = "Source code downloads for Android APIs, used for reference in the IDE. Safe to delete if not used."
    )
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
    val projects: List<ProjectOverviewInfo>,
    val unusedResources: List<UnusedResourceItem>,
    val activeResources: List<UnusedResourceItem>
)
