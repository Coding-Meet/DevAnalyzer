package com.meet.dev.analyzer.data.models.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Workspaces
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
        subtitle = "Analyze, optimize, and clean your development environment",
        description = "Everything you need to analyze projects, manage development resources, and optimize disk usage in one place."
    ),
    OnboardingPageData(
        icon = Icons.Default.AccountTree,
        title = "Project Analyzer",
        subtitle = "Analyze Your Project Structure",
        description = "Get comprehensive insights into your Kotlin and Android projects",
        features = listOf(
            "Module inspection & configuration",
            "Plugin analysis across the project",
            "Dependency analysis with version details",
            "Direct build file access"
        )
    ),
    OnboardingPageData(
        icon = Icons.Default.Storage,
        title = "Storage Analyzer",
        subtitle = "Analyze Development Storage",
        description = "Analyze and manage disk space used by your development tools.",
        features = listOf(
            "Android SDK platforms & build tools",
            "Gradle cache & dependencies analysis",
            "IDE data (JetBrains & Google)",
            "JDK installations monitoring",
            "Kotlin/Native & Konan analysis",
            "AVD storage management"
        )
    ),
    OnboardingPageData(
        icon = Icons.Default.Workspaces,
        title = "Workspace Analyzer",
        subtitle = "Find Unused SDKs & Tools",
        description = "Scan multiple workspaces to identify which SDKs, Gradle, NDK, CMake, and Kotlin/Native versions are actively used, helping you safely remove unused resources.",
        features = listOf(
            "Analyze multiple workspace folders",
            "Detect resources used by your projects",
            "Automatically protect active SDKs and tools",
            "Safely identify unused versions",
            "Recover gigabytes of disk space"
        )
    ),
    OnboardingPageData(
        icon = Icons.Default.CleaningServices,
        title = "Clean Build",
        subtitle = "Free Up Disk Space Instantly",
        description = "Scan and remove build folders from all your Android Studio projects in one place.",
        features = listOf(
            "Multi-project analysis",
            "Module-level build folder detection",
            "Selective deletion with preview",
            "Batch delete operations",
            "Confirmation before deletion"
        )
    ),
    OnboardingPageData(
        icon = Icons.Default.FolderOpen,
        title = "Multi-Path Support",
        subtitle = "Configure Multiple Development Paths",
        description = "Configure multiple JDK and IDE locations for more comprehensive analysis.",
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
        subtitle = "Track Analysis in Real Time",
        description = "Monitor scan progress in real time with detailed status updates and elapsed time tracking.",
        highlights = listOf(
            "Live progress" to Icons.AutoMirrored.Filled.TrendingUp,
            "Time tracking" to Icons.Default.Schedule,
            "Detailed status" to Icons.Default.Info
        )
    ),
    OnboardingPageData(
        icon = Icons.Default.Settings,
        title = "Flexible Configuration",
        subtitle = "Customize Your Development Environment",
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
        subtitle = "Start Optimizing Your Development Environment",
        description = "Explore your projects, optimize development resources, and keep your workspace organized with DevAnalyzer.",
        highlights = listOf(
            "Save disk space" to Icons.Default.Storage,
            "Faster insights" to Icons.Default.Speed,
            "Better management" to Icons.Default.ManageAccounts
        )
    )
)