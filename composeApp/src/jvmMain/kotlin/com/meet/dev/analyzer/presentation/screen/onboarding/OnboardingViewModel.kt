package com.meet.dev.analyzer.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager
import com.meet.dev.analyzer.utility.analytics.AnalyticsEvent
import com.meet.dev.analyzer.utility.analytics.AnalyticsManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val appPreferenceManager: AppPreferenceManager,
    private val analyticsManager: AnalyticsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<OnboardingUiEffect>()
    val effect = _effect.asSharedFlow()

    init {
        analyticsManager.capture(AnalyticsEvent.OnboardingStarted)
    }

    fun onIntent(intent: OnboardingUiIntent) {
        when (intent) {
            OnboardingUiIntent.NextPage -> nextPage()
            OnboardingUiIntent.PreviousPage -> previousPage()
            OnboardingUiIntent.Skip -> complete(skipped = true)
            OnboardingUiIntent.Complete -> complete(skipped = false)
        }
    }

    private fun nextPage() {
        _uiState.update { state ->
            if (state.currentPage < state.totalPages - 1) {
                state.copy(
                    previousPage = state.currentPage,
                    currentPage = state.currentPage + 1,
                    isLastPage = state.currentPage + 1 == state.totalPages - 1,
                    canGoBack = true
                )
            } else {
                state
            }
        }
    }

    private fun previousPage() {
        _uiState.update { state ->
            if (state.currentPage > 0) {
                state.copy(
                    previousPage = state.currentPage,
                    currentPage = state.currentPage - 1,
                    isLastPage = false,
                    canGoBack = state.currentPage - 1 > 0
                )
            } else {
                state
            }
        }
    }

    private fun complete(skipped: Boolean) {
        viewModelScope.launch {
            if (skipped) {
                analyticsManager.capture(AnalyticsEvent.OnboardingSkipped)
            } else {
                analyticsManager.capture(AnalyticsEvent.OnboardingCompleted)
            }
            appPreferenceManager.saveOnboardingDone(true)
            _effect.emit(OnboardingUiEffect.NavigateToMain)
        }
    }
}