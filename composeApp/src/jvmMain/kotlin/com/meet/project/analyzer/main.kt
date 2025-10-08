package com.meet.project.analyzer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.meet.project.analyzer.di.initKoin
import com.meet.project.analyzer.presentation.navigation.AppNavigation
import com.meet.project.analyzer.presentation.theme.ProjectAnalyzerTheme
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.rememberMarkdownState
import io.github.vinceglb.filekit.FileKit
import java.awt.Dimension

fun main() {
    initKoin()
    FileKit.init(appId = "Project_Analyzer")
    System.setProperty("apple.awt.application.appearance", "system")
    application {
        val windowState = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 1024.dp, height = 760.dp)
        )
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Project Analyzer",
        ) {
            window.minimumSize = Dimension(1024, 760)
            ProjectAnalyzerTheme(true) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun MarkdownExample() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // In your composable (use the appropriate Markdown implementation for your theme)
        val markdownState = rememberMarkdownState(
            """fun main() {
    println("Hello, world!")
}""".trimIndent()
        )
        Markdown(
            markdownState,
            colors = DefaultMarkdownColors(
                text = MaterialTheme.colorScheme.onBackground,
                codeBackground = MaterialTheme.colorScheme.surface,
                inlineCodeBackground = MaterialTheme.colorScheme.surface,
                dividerColor = MaterialTheme.colorScheme.outline,
                tableBackground = MaterialTheme.colorScheme.surface,
                tableText = MaterialTheme.colorScheme.onSurface,
                linkText = MaterialTheme.colorScheme.primary,
                inlineCodeText = MaterialTheme.colorScheme.primary,
                codeText = MaterialTheme.colorScheme.onSurface,
            ),
            typography = DefaultMarkdownTypography(
                h1 = MaterialTheme.typography.headlineLarge,
                h2 = MaterialTheme.typography.headlineMedium,
                h3 = MaterialTheme.typography.headlineSmall,
                h4 = MaterialTheme.typography.titleLarge,
                h5 = MaterialTheme.typography.titleMedium,
                h6 = MaterialTheme.typography.titleSmall,
                text = MaterialTheme.typography.bodyLarge,
                code = MaterialTheme.typography.bodyMedium,
                inlineCode = MaterialTheme.typography.bodySmall,
                quote = MaterialTheme.typography.bodyMedium,
                paragraph = MaterialTheme.typography.bodyLarge,
                bullet = MaterialTheme.typography.bodyMedium,
                list = MaterialTheme.typography.bodyMedium,
                ordered = MaterialTheme.typography.bodyMedium,
                table = MaterialTheme.typography.bodyMedium,
                link = MaterialTheme.typography.bodyMedium,
                textLink = TextLinkStyles(),
            ),
            Modifier.fillMaxSize(),
        )
    }
}