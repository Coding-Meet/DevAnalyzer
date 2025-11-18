package com.meet.dev.analyzer.presentation.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meet.dev.analyzer.core.utility.AppLogger
import com.meet.dev.analyzer.core.utility.Utils.tagName
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager
import com.meet.dev.analyzer.presentation.navigation.AppRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(
    appPreferenceManager: AppPreferenceManager
) : ViewModel() {
    private val tag = tagName(javaClass)

    private val appUiState = appPreferenceManager.isOnboardingDone.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect = _effect.asSharedFlow()

    init {
        startAnimation()
    }

    private fun startAnimation() {
        viewModelScope.launch {
            repeat(100) { number ->
                delay(5)
                _uiState.update {
                    it.copy(progress = number / 100f)
                }
            }
            _uiState.update {
                it.copy(progress = 1f)
            }
            val isOnboardingDone = appUiState.value
            val appRoute =
                if (isOnboardingDone) AppRoute.MainGraph else AppRoute.Onboarding
            _effect.emit(SplashEffect.OnSplashCompleted(appRoute))
            AppLogger.d(tag) { "startAnimation end $isOnboardingDone" }
        }
    }

}

