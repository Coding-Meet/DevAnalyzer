package com.meet.project.analyzer.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomToolTip(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                text = {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall
                    )
                },

                caretSize = DpSize(32.dp, 16.dp),
                colors = TooltipDefaults.richTooltipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        state = rememberTooltipState(
            isPersistent = true
        ),
        content = content
    )
}