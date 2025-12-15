package com.meet.dev.analyzer.di

import com.meet.dev.analyzer.presentation.screen.app.AppViewModel
import com.meet.dev.analyzer.presentation.screen.cleanbuild.CleanBuildViewModel
import com.meet.dev.analyzer.presentation.screen.onboarding.OnboardingViewModel
import com.meet.dev.analyzer.presentation.screen.project.ProjectAnalyzerViewModel
import com.meet.dev.analyzer.presentation.screen.setting.SettingsViewModel
import com.meet.dev.analyzer.presentation.screen.splash.SplashViewModel
import com.meet.dev.analyzer.presentation.screen.storage.StorageAnalyzerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::AppViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::ProjectAnalyzerViewModel)
    viewModelOf(::StorageAnalyzerViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::CleanBuildViewModel)
}