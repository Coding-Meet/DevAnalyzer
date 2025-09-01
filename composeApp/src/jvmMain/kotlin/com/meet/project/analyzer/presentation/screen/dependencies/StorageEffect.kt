package com.meet.project.analyzer.presentation.screen.dependencies

import com.meet.project.analyzer.core.utility.UiText

sealed interface StorageEffect {
    data class ShowSuccess(val message: UiText) : StorageEffect
    data class ShowError(val message: UiText) : StorageEffect
}