package com.meet.dev.analyzer.presentation.screen.app

sealed class UpdateDialogState {
    data object Checking : UpdateDialogState()
    data class Available(val version: String) : UpdateDialogState()
    data class Downloading(val percent: Int) : UpdateDialogState()
    data object UpToDate : UpdateDialogState()
    data class Error(val message: String) : UpdateDialogState()
}