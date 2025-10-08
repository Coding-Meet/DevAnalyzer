package com.meet.project.analyzer.data.models.scanner

enum class BuildFileType(val fileName: String) {
    BUILD_GRADLE_KTS("build.gradle.kts"),
    BUILD_GRADLE("build.gradle"),
}