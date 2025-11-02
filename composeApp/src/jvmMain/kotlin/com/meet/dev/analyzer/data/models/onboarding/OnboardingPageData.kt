package com.meet.dev.analyzer.data.models.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPageData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val description: String,
    val features: List<String>? = null,
    val highlights: List<Pair<String, ImageVector>>? = null
)

val onboardingPages = listOf(
    OnboardingPageData(
        icon = Icons.Default.Bolt,
        title = "DevAnalyzer",
        subtitle = "Deep insights into your development environment",
        description = "Analyze your entire development setup with powerful tools for project structure and storage management"
    ),
    OnboardingPageData(
        icon = Icons.Default.AccountTree,
        title = "Project Analyzer",
        subtitle = "Understand Your Project Structure",
        description = "Get comprehensive insights into your Kotlin and Android projects",
        features = listOf(
            "Module inspection & configuration",
            "Plugin analysis across project",
            "Dependency overview with versions",
            "Direct build file access"
        )
    ),
    OnboardingPageData(
        icon = Icons.Default.Storage,
        title = "Storage Analyzer",
        subtitle = "Optimize Your Development Storage",
        description = "Track and manage disk space used by your development tools",
        features = listOf(
            "IDE & SDK storage tracking",
            "Gradle cache analysis",
            "JDK & Kotlin/Native monitoring",
            "Library dependency explorer"
        )
    ),
    OnboardingPageData(
        icon = Icons.Default.Rocket,
        title = "Ready to Analyze",
        subtitle = "Start optimizing your development environment",
        description = "DevAnalyzer will help you understand your projects better and reclaim valuable disk space",
        highlights = listOf(
            "Save disk space" to Icons.Default.Storage,
            "Faster insights" to Icons.Default.Speed,
            "Better management" to Icons.Default.ManageAccounts
        )
    )
)