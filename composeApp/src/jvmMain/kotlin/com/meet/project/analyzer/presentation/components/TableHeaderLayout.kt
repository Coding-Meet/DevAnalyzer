package com.meet.project.analyzer.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.awt.Cursor

@Composable
fun TableHeaderLayout(
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun RowScope.TableHeaderCell(
    title: String,
    description: String,
    weight: Float,
    isSelected: Boolean,
    sortAscending: Boolean,
    onSort: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        isPressed -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isHovered -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 8.dp)
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        onClick = onSort,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        interactionSource = interactionSource,
    ) {
        CustomToolTip(
            title = title,
            description = description
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = when {
                        isSelected -> FontWeight.Bold
                        isHovered -> FontWeight.SemiBold
                        else -> FontWeight.Medium
                    },
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = when {
                        isSelected && sortAscending -> Icons.Filled.KeyboardArrowUp
                        isSelected && !sortAscending -> Icons.Filled.KeyboardArrowDown
                        else -> Icons.Filled.KeyboardArrowUp
                    },
                    contentDescription = when {
                        isSelected -> if (sortAscending)
                            "Sorted ascending, click for descending"
                        else "Sorted descending, click for ascending"

                        else -> "Click to sort by $title"
                    },
                    modifier = Modifier.size(18.dp),
                    tint = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isHovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )
            }
        }
    }
}