package com.meet.dev.analyzer.presentation.screen.setting.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.core.utility.Utils.openFile
import com.meet.dev.analyzer.presentation.screen.setting.PathStatus
import java.awt.Cursor

@Composable
fun PathSettingItem(
    label: String,
    path: String,
    status: PathStatus,
    icon: ImageVector,
    onEditClick: (String) -> Unit,
    onValidateClick: () -> Unit,
    onResetDefaultClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = status.containerColor()
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = path.ifEmpty { "Not set" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (path.isNotBlank()) Modifier.clickable { path.openFile() }
                            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        else Modifier)
                }
                // Validate
                if (status != PathStatus.VALID) {
                    OutlinedButton(
                        onClick = onValidateClick,
                        modifier = Modifier
                            .height(36.dp)
                            .pointerHoverIcon(
                                PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
                            ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = status.tint()
                        ),
                        border = BorderStroke(
                            1.dp,
                            status.tint()
                        )
                    ) {
                        Icon(
                            imageVector = status.imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Validate",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                // Reset
                OutlinedButton(
                    onClick = onResetDefaultClick,
                    modifier = Modifier
                        .height(36.dp)
                        .pointerHoverIcon(
                            PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
                        ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                IconButton(
                    onClick = {
                        onEditClick(path)
                    },
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        )
                    ),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = status.imageVector,
                    contentDescription = status.message,
                    tint = status.tint(),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = status.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = status.tint()
                )
            }
        }
    }
}
