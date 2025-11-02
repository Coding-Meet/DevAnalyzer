package com.meet.dev.analyzer.data.models.project

enum class BuildFileType(val fileName: String) {
    BUILD_GRADLE_KTS("build.gradle.kts"),
    BUILD_GRADLE("build.gradle"),
}