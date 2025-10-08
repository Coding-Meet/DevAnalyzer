package com.meet.project.analyzer.core.utility


enum class PluginColumn(val title: String, val weight: Float) {
    NAME("Name", 0.34f),
    CURRENT("Current", 0.14f),
    VERSIONS("Versions", 0.14f),
    CONFIGURATION("Configuration", 0.19f),
    MODULE("Module", 0.19f)
}


enum class DependencyColumn(val title: String, val weight: Float) {
    NAME("Name", 0.34f),
    CURRENT("Current", 0.14f),
    VERSIONS("Versions", 0.14f),
    CONFIGURATION("Configuration", 0.19f),
    MODULE("Module", 0.19f)
}
