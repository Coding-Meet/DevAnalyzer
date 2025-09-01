package com.meet.project.analyzer.core.utility

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        val resId: org.jetbrains.compose.resources.StringResource,
        vararg val args: Any
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    suspend fun asStringForSuspend(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> getString(resId, *args)
        }
    }
}