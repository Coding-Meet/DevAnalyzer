package com.meet.project.analyzer.core.utility

enum class ColumnWeight(val value: Float) {
    NAME(0.25f),
    CURRENT(0.10f),
    VERSIONS(0.10f),
    CONFIGURATION(0.14f),
    MODULE(0.10f)
}
