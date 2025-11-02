package com.meet.dev.analyzer.presentation.screen.onboarding

sealed interface OnboardingUiIntent {
    data object NextPage : OnboardingUiIntent
    data object PreviousPage : OnboardingUiIntent
    data object Skip : OnboardingUiIntent
    data object Complete : OnboardingUiIntent
}