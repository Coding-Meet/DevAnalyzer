package com.meet.dev.analyzer.presentation.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BoxWithConstraintsScope.VerticalScrollBarLayout(
    adapter: ScrollbarAdapter
) {
    VerticalScrollbar(
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        adapter = adapter,
        style = defaultScrollbarStyle().copy(
            hoverColor = MaterialTheme.colorScheme.outline,
            unhoverColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    )
}