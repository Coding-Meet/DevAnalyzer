package com.meet.dev.analyzer.presentation.screen.setting.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
            modifier = Modifier.fillMaxWidth().padding(10.dp)
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

                // Reset Default
                Button(
                    onClick = onResetDefaultClick,
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        )
                    ),
                ) {
                    Text("Reset")
                }

                IconButton(
                    onClick = if (status != PathStatus.VALID) onValidateClick else ({}),
                    modifier = Modifier.pointerHoverIcon(
                        PointerIcon(
                            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        )
                    ),
                ) {
                    Icon(
                        imageVector = status.imageVector,
                        contentDescription = status.message,
                        tint = status.tint(),
                        modifier = Modifier.size(24.dp)
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
