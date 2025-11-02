package com.meet.project.analyzer.presentation.screen.splash

import com.meet.project.analyzer.presentation.navigation.AppRoute

interface SplashEffect {
    data class OnSplashCompleted(val appRoute: AppRoute) : SplashEffect
}
