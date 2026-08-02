package com.meet.dev.analyzer.utility.analytics

/**
 * Maps each [AnalyticsEvent] to its PostHog property map.
 *
 * Isolated in its own file so the mapping logic can grow independently
 * of the event definitions and the PostHog implementation.
 *
 * Privacy: only anonymous, aggregated values (counts, sizes in bytes,
 * durations in ms, enum labels). No paths, names, or personal data.
 */
internal fun AnalyticsEvent.toProperties(): Map<String, Any> = when (this) {

    // ── Application ───────────────────────────────────────────────────────────
    AnalyticsEvent.AppOpened -> emptyMap()
    AnalyticsEvent.AppClosed -> emptyMap()
    is AnalyticsEvent.AppUpdated -> mapOf(
        "from_version" to fromVersion,
        "to_version" to toVersion,
    )

    // ── Project Analyzer ──────────────────────────────────────────────────────
    is AnalyticsEvent.ProjectAnalysisCompleted -> mapOf(
        "module_count" to moduleCount,
        "dependency_count" to dependencyCount,
        "plugin_count" to pluginCount,
        "duration_ms" to durationMs,
    )

    is AnalyticsEvent.ProjectAnalysisFailed -> mapOf("reason" to reason.value)

    // ── Storage Analyzer ──────────────────────────────────────────────────────
    is AnalyticsEvent.StorageAnalysisCompleted -> mapOf(
        "sdk_size" to sdkSizeBytes,
        "gradle_size" to gradleSizeBytes,
        "konan_size" to konanSizeBytes,
        "duration_ms" to durationMs,
    )

    is AnalyticsEvent.StorageAnalysisFailed -> mapOf("reason" to reason.value)

    // ── Workspace Analyzer ────────────────────────────────────────────────────
    is AnalyticsEvent.WorkspaceAnalysisCompleted -> mapOf(
        "workspace_count" to workspaceCount,
        "project_count" to projectCount,
        "potential_savings_bytes" to potentialSavingsBytes,
        "unused_resource_count" to unusedResourceCount,
        "duration_ms" to durationMs,
    )

    is AnalyticsEvent.WorkspaceAnalysisFailed -> mapOf("reason" to reason.value)

    // ── Clean Build ───────────────────────────────────────────────────────────
    is AnalyticsEvent.CleanBuildAnalysisCompleted -> mapOf(
        "project_count"      to projectCount,
        "build_folder_count" to buildFolderCount,
        "duration_ms"        to durationMs,
    )

    is AnalyticsEvent.CleanBuildCompleted -> mapOf(
        "deleted_build_folders" to deletedBuildFolders,
        "reclaimed_bytes"       to reclaimedBytes,
        "duration_ms"           to durationMs,
    )

    is AnalyticsEvent.CleanBuildFailed -> mapOf("reason" to reason.value)


    // ── All zero-property events ──────────────────────────────────────────────
    AnalyticsEvent.SettingsOpened,
    AnalyticsEvent.AnalyticsEnabled,
    AnalyticsEvent.AnalyticsDisabled,
    AnalyticsEvent.ReviewPromptShown,
    AnalyticsEvent.FeedbackOpened,
    AnalyticsEvent.FeedbackCancelled,
    AnalyticsEvent.FeedbackSubmitted,
    AnalyticsEvent.GitHubSponsorClicked,
    AnalyticsEvent.BuyMeCoffeeClicked,
    AnalyticsEvent.PaypalClicked,
        -> emptyMap()
}
