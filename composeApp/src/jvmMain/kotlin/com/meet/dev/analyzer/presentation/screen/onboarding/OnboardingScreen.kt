package com.meet.dev.analyzer.presentation.screen.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.core.utility.ObserveAsEvents
import com.meet.dev.analyzer.data.models.onboarding.OnboardingPageData
import com.meet.dev.analyzer.presentation.components.TabSlideAnimation
import com.meet.dev.analyzer.presentation.screen.onboarding.components.AnimatedIcon
import com.meet.dev.analyzer.presentation.screen.onboarding.components.FeatureItem
import com.meet.dev.analyzer.presentation.screen.onboarding.components.HighlightCard
import com.meet.dev.analyzer.presentation.screen.onboarding.components.OnboardingBottomBar
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
) {
    val viewModel = koinViewModel<OnboardingViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    viewModel.effect.ObserveAsEvents { onBoardingUiEffect ->
        when (onBoardingUiEffect) {
            is OnboardingUiEffect.NavigateToMain -> onComplete()
        }
    }

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                pages = uiState.pages,
                currentPage = uiState.currentPage,
                canGoBack = uiState.canGoBack,
                isLastPage = uiState.isLastPage,
                onBackClick = {
                    viewModel.onIntent(OnboardingUiIntent.PreviousPage)
                },
                onNextClick = {
                    if (uiState.isLastPage) {
                        viewModel.onIntent(OnboardingUiIntent.Complete)
                    } else {
                        viewModel.onIntent(OnboardingUiIntent.NextPage)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Skip Button
            if (!uiState.isLastPage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    TextButton(
                        onClick = {
                            viewModel.onIntent(OnboardingUiIntent.Skip)
                        }
                    ) {
                        Text(
                            "Skip",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                TabSlideAnimation(
                    selectedTabIndex = uiState.currentPage,
                    previousTabIndex = uiState.previousPage,
                    targetState = uiState.pages[uiState.currentPage],
                ) { page ->
                    OnboardingPageContent(page = page)
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPageData) {
    var titleVisible by remember { mutableStateOf(false) }

    LaunchedEffect(page) {
        titleVisible = false
        delay(100)
        titleVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Icon
        AnimatedIcon(icon = page.icon)

        Spacer(Modifier.height(24.dp))

        // Animated Title
        AnimatedVisibility(
            visible = titleVisible,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(initialOffsetY = { it / 4 })
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(16.dp))

        // Animated Subtitle
        AnimatedVisibility(
            visible = titleVisible,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                    slideInVertically(initialOffsetY = { it / 4 })
        ) {
            Text(
                text = page.subtitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        // Animated Description
        AnimatedVisibility(
            visible = titleVisible,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                    slideInVertically(initialOffsetY = { it / 4 })
        ) {
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 600.dp)
            )
        }

        Spacer(Modifier.height(40.dp))

        // Animated Features List
        page.features?.let { features ->
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                features.forEachIndexed { index, feature ->
                    var featureVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(page) {
                        featureVisible = false
                        delay(300L + (index * 100L))
                        featureVisible = true
                    }

                    AnimatedVisibility(
                        visible = featureVisible,
                        enter = fadeIn(animationSpec = tween(400)) +
                                slideInHorizontally(initialOffsetX = { -it / 2 })
                    ) {
                        FeatureItem(feature)
                    }
                }
            }
        }

        // Animated Highlights Grid
        page.highlights?.let { highlights ->
            var highlightsVisible by remember { mutableStateOf(false) }
            LaunchedEffect(page) {
                highlightsVisible = false
                delay(300)
                highlightsVisible = true
            }

            AnimatedVisibility(
                visible = highlightsVisible,
                enter = fadeIn(animationSpec = tween(600)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(600))
            ) {
                Row(
                    modifier = Modifier.widthIn(max = 600.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    highlights.forEach { (text, icon) ->
                        HighlightCard(
                            text = text,
                            icon = icon,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}



