package com.meet.project.analyzer.presentation.screen.onboarding

import com.meet.project.analyzer.data.models.onboarding.OnboardingPageData
import com.meet.project.analyzer.data.models.onboarding.onboardingPages

data class OnboardingUiState(
    val currentPage: Int = 0,
    val previousPage: Int = 0,
    val totalPages: Int = onboardingPages.size,
    val pages: List<OnboardingPageData> = onboardingPages,
    val isLastPage: Boolean = false,
    val canGoBack: Boolean = false
)
