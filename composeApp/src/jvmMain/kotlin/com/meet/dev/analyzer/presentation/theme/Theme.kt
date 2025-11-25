package com.meet.dev.analyzer.presentation.theme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D36),

    secondary = Color(0xFF00687A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFACE9FD),
    onSecondaryContainer = Color(0xFF001F26),

    tertiary = Color(0xFF0061A4),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD0E4FF),
    onTertiaryContainer = Color(0xFF001D36),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFFCFCFF),
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFCFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF42474E),

    surfaceTint = Color(0xFF0061A4),

    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFF64B5F6),

    outline = Color(0xFF72777F),
    outlineVariant = Color(0xFFC2C7CF),

    scrim = Color(0xFF000000),

    surfaceBright = Color(0xFFFCFCFF),
    surfaceDim = Color(0xFFDCDCE0),

    surfaceContainer = Color(0xFFF0F0F4),
    surfaceContainerHigh = Color(0xFFEAEAEE),
    surfaceContainerHighest = Color(0xFFE4E5E9),
    surfaceContainerLow = Color(0xFFF6F6FA),
    surfaceContainerLowest = Color(0xFFFFFFFF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD0E4FF),

    secondary = Color(0xFF4FC3F7),
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFFB8E8F5),

    tertiary = Color(0xFF4FC3F7),
    onTertiary = Color(0xFF00363F),
    tertiaryContainer = Color(0xFF004F58),
    onTertiaryContainer = Color(0xFFB8E8F5),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),

    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF),

    surfaceTint = Color(0xFF64B5F6),

    inverseSurface = Color(0xFFE2E2E6),
    inverseOnSurface = Color(0xFF2F3033),
    inversePrimary = Color(0xFF0061A4),

    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF42474E),

    scrim = Color(0xFF000000),

    surfaceBright = Color(0xFF3A3D40),
    surfaceDim = Color(0xFF1A1C1E),

    surfaceContainer = Color(0xFF1E2022),
    surfaceContainerHigh = Color(0xFF282A2D),
    surfaceContainerHighest = Color(0xFF333538),
    surfaceContainerLow = Color(0xFF1A1C1E),
    surfaceContainerLowest = Color(0xFF0F1113)
)

@Composable
fun DevAnalyzerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
    ) {
        // this temporary code for UI not updating after StateFlow update when Compose Desktop window is inactive
        RepaintHack()
        content()
    }
}

@Composable
fun RepaintHack() {
    val infiniteTransition = rememberInfiniteTransition(label = "repaint")
    val s = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000), // 1 second
            repeatMode = RepeatMode.Restart
        ),
        label = "dummy"
    )
}



//@Composable
//fun CustomTypography() = Typography().run {
//    val fontFamily = FontFamily(Font(Res.font.nunito_sans))
//
//    copy(
//        displayLarge = displayLarge.copy(fontFamily = fontFamily),
//        displayMedium = displayMedium.copy(fontFamily = fontFamily),
//        displaySmall = displaySmall.copy(fontFamily = fontFamily),
//        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
//        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
//        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
//        titleLarge = titleLarge.copy(fontFamily = fontFamily),
//        titleMedium = titleMedium.copy(fontFamily = fontFamily),
//        titleSmall = titleSmall.copy(fontFamily = fontFamily),
//        bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
//        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
//        bodySmall = bodySmall.copy(fontFamily = fontFamily),
//        labelLarge = labelLarge.copy(fontFamily = fontFamily),
//        labelMedium = labelMedium.copy(fontFamily = fontFamily),
//        labelSmall = labelSmall.copy(fontFamily = fontFamily)
//    )
//}