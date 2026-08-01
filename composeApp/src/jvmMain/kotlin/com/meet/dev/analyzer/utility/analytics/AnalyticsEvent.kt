package com.meet.dev.analyzer.utility.analytics

/**
 * Compile-safe, sealed hierarchy of all analytics events.
 *
 * Each event declares its own [name] — no hardcoded strings scattered anywhere else.
 * Privacy rule: properties contain only anonymous aggregated data (counts, sizes, durations).
 * No paths, names, identifiers, or exception messages are ever included.
 */
sealed interface AnalyticsEvent {
    val name: String

    // ── Application ───────────────────────────────────────────────────────────

    data object AppOpened : AnalyticsEvent {
        override val name = "app_opened"
    }

    data object AppClosed : AnalyticsEvent {
        override val name = "app_closed"
    }

    /**
     * Fired once when the app version changes on startup.
     * Skipped on first install (when [fromVersion] would be blank).
     */
    data class AppUpdated(
        val fromVersion: String,
        val toVersion: String,
    ) : AnalyticsEvent {
        override val name = "app_updated"
    }

    // ── Project Analyzer ──────────────────────────────────────────────────────

    data class ProjectAnalysisCompleted(
        val moduleCount: Int,
        val dependencyCount: Int,
        val pluginCount: Int,
        val durationMs: Long,
    ) : AnalyticsEvent {
        override val name = "project_analysis_completed"
    }

    data class ProjectAnalysisFailed(val reason: FailureReason) : AnalyticsEvent {
        override val name = "project_analysis_failed"
    }

    // ── Storage Analyzer ──────────────────────────────────────────────────────

    data class StorageAnalysisCompleted(
        val sdkSizeBytes: Long,
        val gradleSizeBytes: Long,
        val konanSizeBytes: Long,
        val durationMs: Long,
    ) : AnalyticsEvent {
        override val name = "storage_analysis_completed"
    }

    data class StorageAnalysisFailed(val reason: FailureReason) : AnalyticsEvent {
        override val name = "storage_analysis_failed"
    }

    // ── Workspace Analyzer ────────────────────────────────────────────────────

    data class WorkspaceAnalysisCompleted(
        val workspaceCount: Int,
        val projectCount: Int,
        val potentialSavingsBytes: Long,
        val unusedResourceCount: Int,
        val durationMs: Long,
    ) : AnalyticsEvent {
        override val name = "workspace_analysis_completed"
    }

    data class WorkspaceAnalysisFailed(val reason: FailureReason) : AnalyticsEvent {
        override val name = "workspace_analysis_failed"
    }

    // ── Clean Build ───────────────────────────────────────────────────────────

    data class CleanBuildCompleted(
        val deletedBuildFolders: Int,
        val reclaimedBytes: Long,
        val durationMs: Long,
    ) : AnalyticsEvent {
        override val name = "clean_build_completed"
    }

    data class CleanBuildFailed(val reason: FailureReason) : AnalyticsEvent {
        override val name = "clean_build_failed"
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    data object SettingsOpened : AnalyticsEvent {
        override val name = "settings_opened"
    }

    /** Fired when the user explicitly enables analytics. */
    data object AnalyticsEnabled : AnalyticsEvent {
        override val name = "analytics_enabled"
    }

    /** Fired when the user explicitly disables analytics. */
    data object AnalyticsDisabled : AnalyticsEvent {
        override val name = "analytics_disabled"
    }

    // ── Feedback ──────────────────────────────────────────────────────────────

    /** Fired when the review / feedback prompt is shown to the user. */
    data object ReviewPromptShown : AnalyticsEvent {
        override val name = "review_prompt_shown"
    }

    /** Fired when the user opens a feedback dialog (crash log, review). */
    data object FeedbackOpened : AnalyticsEvent {
        override val name = "feedback_opened"
    }

    /** Fired when the user dismisses the feedback dialog without submitting. */
    data object FeedbackCancelled : AnalyticsEvent {
        override val name = "feedback_cancelled"
    }

    /** Fired when the user submits feedback / review. */
    data object FeedbackSubmitted : AnalyticsEvent {
        override val name = "feedback_submitted"
    }

    // ── Support / Monetization ────────────────────────────────────────────────

    data object GitHubSponsorClicked : AnalyticsEvent {
        override val name = "github_sponsor_clicked"
    }

    data object BuyMeCoffeeClicked : AnalyticsEvent {
        override val name = "buy_me_coffee_clicked"
    }

    data object PaypalClicked : AnalyticsEvent {
        override val name = "paypal_clicked"
    }
}
