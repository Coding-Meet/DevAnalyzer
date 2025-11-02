package com.meet.dev.analyzer.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.awt.Cursor

@Composable
fun TableBodyLayout(
    isEven: Boolean,
    innerContent: @Composable RowScope.() -> Unit,
    outerContent: @Composable ColumnScope.() -> Unit
) {
    val backgroundColor = if (isEven) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .animateContentSize(animationSpec = spring()),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            content = innerContent
        )

        outerContent()
    }
}

@Composable
fun RowScope.TableBodyCell(
    weight: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .weight(weight)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}

@Composable
fun TableBodyCellText(
    text: String,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    Text(
        text = text,
        style = style,
        color = color,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun TableBodyCellChip(
    text: String,
    backgroundColor: Color,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Medium,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .then(
                if (onClick != null) {
                    Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                } else {
                    Modifier
                }
            ),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = fontWeight,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (trailingContent != null) Modifier.weight(1f) else Modifier
            )
            trailingContent?.invoke()
        }
    }
}

@Composable
fun TableBodyCellColumn(
    primaryText: String,
    secondaryText: String? = null,
    tertiaryText: String? = null,
    primaryStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    secondaryStyle: TextStyle = MaterialTheme.typography.bodySmall,
    tertiaryStyle: TextStyle = MaterialTheme.typography.labelSmall,
    primaryColor: Color = MaterialTheme.colorScheme.onSurface,
    secondaryColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    tertiaryColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
    primaryFontWeight: FontWeight = FontWeight.SemiBold
) {
    Column {
        Spacer(Modifier.padding(vertical = 2.dp))
        if (secondaryText != null) {
            Text(
                text = secondaryText,
                style = secondaryStyle,
                color = secondaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = primaryText,
            style = primaryStyle,
            fontWeight = primaryFontWeight,
            color = primaryColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (tertiaryText != null) {
            Text(
                text = tertiaryText,
                style = tertiaryStyle,
                color = tertiaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.padding(vertical = 2.dp))
    }
}