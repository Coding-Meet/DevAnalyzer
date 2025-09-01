package com.meet.project.analyzer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import projectanalyzer.composeapp.generated.resources.Res
import projectanalyzer.composeapp.generated.resources.nunito_sans

//private val DarkColorScheme = darkColorScheme(
//    primary = Color(0xFF90CAF9),
//    onPrimary = Color(0xFF003258),
//    primaryContainer = Color(0xFF004881),
//    onPrimaryContainer = Color(0xFFD1E4FF),
//    secondary = Color(0xFFBCC7DB),
//    onSecondary = Color(0xFF263141),
//    secondaryContainer = Color(0xFF3C4858),
//    onSecondaryContainer = Color(0xFFD8E3F8),
//    tertiary = Color(0xFFD6BEE4),
//    onTertiary = Color(0xFF3B2948),
//    tertiaryContainer = Color(0xFF523F5F),
//    onTertiaryContainer = Color(0xFFF2DAFF),
//    error = Color(0xFFFFB4AB),
//    errorContainer = Color(0xFF93000A),
//    onError = Color(0xFF690005),
//    onErrorContainer = Color(0xFFFFDAD6),
//    background = Color(0xFF0F1419),
//    onBackground = Color(0xFFE6E1E5),
//    surface = Color(0xFF0F1419),
//    onSurface = Color(0xFFE6E1E5),
//    surfaceVariant = Color(0xFF44474F),
//    onSurfaceVariant = Color(0xFFC4C7C5),
//    outline = Color(0xFF8E9192),
//    inverseOnSurface = Color(0xFF0F1419),
//    inverseSurface = Color(0xFFE6E1E5),
//    inversePrimary = Color(0xFF0061A4),
//    surfaceTint = Color(0xFF90CAF9)
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Color(0xFF0061A4),
//    onPrimary = Color(0xFFFFFFFF),
//    primaryContainer = Color(0xFFD1E4FF),
//    onPrimaryContainer = Color(0xFF001D36),
//    secondary = Color(0xFF535F70),
//    onSecondary = Color(0xFFFFFFFF),
//    secondaryContainer = Color(0xFFD7E3F7),
//    onSecondaryContainer = Color(0xFF101C2B),
//    tertiary = Color(0xFF6B5777),
//    onTertiary = Color(0xFFFFFFFF),
//    tertiaryContainer = Color(0xFFF2DAFF),
//    onTertiaryContainer = Color(0xFF251431),
//    error = Color(0xFFBA1A1A),
//    errorContainer = Color(0xFFFFDAD6),
//    onError = Color(0xFFFFFFFF),
//    onErrorContainer = Color(0xFF410002),
//    background = Color(0xFFFDFCFF),
//    onBackground = Color(0xFF1A1C1E),
//    surface = Color(0xFFFDFCFF),
//    onSurface = Color(0xFF1A1C1E),
//    surfaceVariant = Color(0xFFDFE2EB),
//    onSurfaceVariant = Color(0xFF43474E),
//    outline = Color(0xFF73777F),
//    inverseOnSurface = Color(0xFFF1F0F4),
//    inverseSurface = Color(0xFF2F3033),
//    inversePrimary = Color(0xFF90CAF9),
//    surfaceTint = Color(0xFF0061A4)
//)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF00695C),
    tertiary = Color(0xFFFF9800),
    onTertiary = Color.White,
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF757575),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color(0xFF00695C),
    secondaryContainer = Color(0xFF00838F),
    onSecondaryContainer = Color(0xFFE0F7FA),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFFE65100),
    error = Color(0xFFEF5350),
    onError = Color(0xFFB71C1C),
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color(0xFFFFEBEE),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD)
)

@Composable
fun ProjectAnalyzerTheme(
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
        content = content
    )
}

@Composable
fun CustomTypography() = Typography().run {
    val fontFamily = FontFamily(Font(Res.font.nunito_sans))

    copy(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily)
    )
}