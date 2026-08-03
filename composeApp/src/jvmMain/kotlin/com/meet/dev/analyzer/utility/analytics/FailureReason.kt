package com.meet.dev.analyzer.utility.analytics

/**
 * Anonymous, non-sensitive failure categorization for analytics events.
 * Never captures exception messages, stack traces, or file paths.
 */
enum class FailureReason(val value: String) {
    /** Invalid input, path, or project structure — caught before execution */
    VALIDATION("validation"),

    /** User action or coroutine cancellation */
    CANCELLED("cancelled"),

    /** Unhandled exception — catch(Exception) fallback */
    UNEXPECTED("unexpected")
}
