package com.meet.dev.analyzer.presentation.screen.onboarding

import com.meet.dev.analyzer.data.models.onboarding.OnboardingPageData
import com.meet.dev.analyzer.data.models.onboarding.onboardingPages

data class OnboardingUiState(
    val currentPage: Int = 0,
    val previousPage: Int = 0,
    val totalPages: Int = onboardingPages.size,
    val pages: List<OnboardingPageData> = onboardingPages,
    val isLastPage: Boolean = false,
    val canGoBack: Boolean = false
)
