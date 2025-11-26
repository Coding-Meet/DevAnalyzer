package com.meet.dev.analyzer.data.models.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
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
            "Android SDK & platform tools tracking",
            "Gradle cache & dependencies analysis",
            "IDE data (JetBrains & Google)",
            "JDK installations monitoring",
            "Kotlin/Native & Konan analysis",
            "AVD storage management"
        )
    ),
    OnboardingPageData(
        icon = Icons.Default.FolderOpen,
        title = "Multi-Path Support",
        subtitle = "Scan Multiple Locations",
        description = "Configure and analyze multiple JDK and IDE locations simultaneously for comprehensive coverage",
        features = listOf(
            "Multiple JDK paths support",
            "JetBrains IDE locations (Caches, Logs, Support)",
            "Google IDE locations",
            "Custom path validation",
            "Auto-detect common paths"
        )
    ),
    OnboardingPageData(
        icon = Icons.Default.Speed,
        title = "Real-Time Insights",
        subtitle = "Live Scanning Progress",
        description = "Watch your scan progress in real-time with detailed status updates and elapsed time tracking",
        highlights = listOf(
            "Live progress" to Icons.AutoMirrored.Filled.TrendingUp,
            "Time tracking" to Icons.Default.Schedule,
            "Detailed status" to Icons.Default.Info
        )
    ),
    OnboardingPageData(
        icon = Icons.Default.Settings,
        title = "Flexible Configuration",
        subtitle = "Customize Your Analysis",
        description = "Configure scan locations, validate paths, and personalize your analysis experience",
        features = listOf(
            "Path validation & verification",
            "Reset to defaults option",
            "Crash reporting toggle",
            "Easy path selection"
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