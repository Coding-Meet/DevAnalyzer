package com.meet.dev.analyzer.presentation.screen.setting.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.presentation.screen.setting.PathPickerType
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import java.awt.Cursor

@Composable
fun PathPickerDialog(
    type: PathPickerType,
    currentPath: String,
    onDismiss: () -> Unit,
    onPathSelected: (String) -> Unit
) {
    var pathInput by remember { mutableStateOf(currentPath) }

    val coroutineScope = rememberCoroutineScope()

    val directoryPickerLauncher = rememberDirectoryPickerLauncher(
        directory = PlatformFile(currentPath)
    ) { directory ->
        if (directory != null) {
            coroutineScope.launch {
                pathInput = directory.path
            }
        }
    }

    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(type.title)
    }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = pathInput,
                    onValueChange = { pathInput = it },
                    label = { Text("Path") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    singleLine = true,
                )
                Button(
                    onClick = { directoryPickerLauncher.launch() },
                    modifier = Modifier.height(56.dp)
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose")
                }
            }
            Text(
                text = "You can type manually or pick folder",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }, confirmButton = {
        Button(
            onClick = { onPathSelected(pathInput) },
            enabled = pathInput.isNotBlank(),
            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        ) {
            Text("Confirm")
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        ) {
            Text("Cancel")
        }
    })
}
