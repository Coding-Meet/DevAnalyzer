package com.meet.dev.analyzer.presentation.screen.splash

import com.meet.dev.analyzer.presentation.navigation.AppRoute

interface SplashEffect {
    data class OnSplashCompleted(val appRoute: AppRoute) : SplashEffect
}
