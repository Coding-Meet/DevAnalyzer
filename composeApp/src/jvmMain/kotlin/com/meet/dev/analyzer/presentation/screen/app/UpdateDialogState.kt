package com.meet.dev.analyzer.presentation.screen.app

sealed class UpdateDialogState {
    data object Checking : UpdateDialogState()
    data class Available(val version: String, val releaseNotes: String, val htmlUrl: String) : UpdateDialogState()
    data object UpToDate : UpdateDialogState()
    data class Error(val message: String) : UpdateDialogState()
}