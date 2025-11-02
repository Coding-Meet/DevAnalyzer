package com.meet.project.analyzer.di

import com.meet.project.analyzer.presentation.screen.app.AppViewModel
import com.meet.project.analyzer.presentation.screen.onboarding.OnboardingViewModel
import com.meet.project.analyzer.presentation.screen.scanner.ProjectScannerViewModel
import com.meet.project.analyzer.presentation.screen.splash.SplashViewModel
import com.meet.project.analyzer.presentation.screen.storage.StorageAnalyzerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::AppViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::ProjectScannerViewModel)
    viewModelOf(::StorageAnalyzerViewModel)
}