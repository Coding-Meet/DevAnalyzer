package com.meet.project.analyzer.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import java.awt.Cursor

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    labelText: String,
    leadingIcon: ImageVector,
    readOnly: Boolean = false,
    placeholder: @Composable (() -> Unit)? = null,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = {
            Text(labelText)
        },
        placeholder = placeholder,
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        readOnly = readOnly,
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                            )
                        )
                    ),
                    onClick = onClear
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}