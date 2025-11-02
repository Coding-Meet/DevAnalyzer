package com.meet.project.analyzer.presentation.screen.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meet.project.analyzer.core.utility.ObserveAsEvents
import com.meet.project.analyzer.presentation.navigation.AppRoute
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import projectanalyzer.composeapp.generated.resources.Res
import projectanalyzer.composeapp.generated.resources.app_logo

@Composable
fun SplashScreen(
    onSplashFinished: (AppRoute) -> Unit,
    splashDurationMillis: Long = 2000,
) {
    val splashViewModel = koinViewModel<SplashViewModel>()
    val uiState by splashViewModel.uiState.collectAsState()
    val startAnimation = uiState.startAnimation

    splashViewModel.effect.ObserveAsEvents { splashEffect ->
        when (splashEffect) {
            is SplashEffect.OnSplashCompleted -> onSplashFinished(splashEffect.appRoute)
        }
    }

    // Logo animation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        )
    )

    // Text animation
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 400,
            easing = FastOutSlowInEasing
        )
    )


    // Progress animation
    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = splashDurationMillis.toInt(),
            easing = FastOutSlowInEasing
        )
    )

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo Image
        Image(
            painter = painterResource(Res.drawable.app_logo),
            contentDescription = "DevAnalyzer Logo",
            modifier = Modifier
                .size(250.dp)
                .scale(logoScale)
                .alpha(logoAlpha)
        )

        Spacer(Modifier.height(16.dp))

        // App Name
        Text(
            text = "DevAnalyzer",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.alpha(textAlpha)
        )

        // Tagline
        Text(
            text = "Deep insights into your development environment",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .alpha(textAlpha)
                .padding(horizontal = 48.dp)
        )

        Spacer(Modifier.height(32.dp))

        // Loading indicator
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        // Loading text
        Text(
            text = "Loading...",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}