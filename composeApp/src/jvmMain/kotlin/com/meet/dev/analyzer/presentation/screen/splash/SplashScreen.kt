package com.meet.dev.analyzer.presentation.screen.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meet.dev.analyzer.Res
import com.meet.dev.analyzer.app_logo
import com.meet.dev.analyzer.presentation.navigation.AppRoute
import com.meet.dev.analyzer.utility.ui.ObserveAsEvents
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onSplashFinished: (AppRoute) -> Unit,
) {
    val splashViewModel = koinViewModel<SplashViewModel>()
    val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()
    splashViewModel.effect.ObserveAsEvents { splashEffect ->
        when (splashEffect) {
            is SplashEffect.OnSplashCompleted -> onSplashFinished(splashEffect.appRoute)
        }
    }
    val isVisibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val splashTransition = rememberTransition(isVisibleState, label = "splashTransition")

    val logoScale by splashTransition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = 1000,
                delayMillis = 0,
                easing = EaseOutBack
            )
        },
        label = "logoScale"
    ) { isVisible -> if (isVisible) 1f else 0f }

    val logoAlpha by splashTransition.animateFloat(
        transitionSpec = {
            tween(600, delayMillis = 200, easing = LinearOutSlowInEasing)
        },
        label = "logoAlpha"
    ) { isVisible -> if (isVisible) 1f else 0f }

    val nameAlpha by splashTransition.animateFloat(
        transitionSpec = { tween(600, delayMillis = 200) },
        label = "nameAlpha"
    ) { isVisible -> if (isVisible) 1f else 0f }

    val nameOffset by splashTransition.animateDp(
        transitionSpec = { tween(600, delayMillis = 200, easing = FastOutSlowInEasing) },
        label = "nameOffset"
    ) { isVisible -> if (isVisible) 0.dp else 12.dp }

    val taglineAlpha by splashTransition.animateFloat(
        transitionSpec = { tween(600, delayMillis = 200) },
        label = "taglineAlpha"
    ) { isVisible -> if (isVisible) 1f else 0f }

    val taglineScale by splashTransition.animateFloat(
        transitionSpec = {
            tween(600, delayMillis = 200, easing = FastOutSlowInEasing)
        },
        label = "taglineScale"
    ) { isVisible -> if (isVisible) 1f else 0f }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // LOGO
        Image(
            painter = painterResource(Res.drawable.app_logo),
            contentDescription = "DevAnalyzer Logo",
            modifier = Modifier
                .size(250.dp)
                .graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                    alpha = logoAlpha
                }
        )

        Spacer(Modifier.height(20.dp))

        // App Name
        Text(
            text = "DevAnalyzer",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .offset(y = nameOffset)
                .graphicsLayer {
                    alpha = nameAlpha
                }
        )

        Spacer(Modifier.height(6.dp))

        // TAGLINE
        Text(
            text = "Deep insights into your development environment",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .graphicsLayer {
                    alpha = taglineAlpha
                    scaleX = taglineScale
                    scaleY = taglineScale
                }
                .padding(horizontal = 48.dp)
        )

        Spacer(Modifier.height(32.dp))

        // PROGRESS
        LinearProgressIndicator(
            progress = { uiState.progress },
            modifier = Modifier,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(10.dp))

        // LOADING TEXT
        AnimatedVisibility(visible = uiState.progress < 1f) {
            Text(
                text = "Loading...",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}