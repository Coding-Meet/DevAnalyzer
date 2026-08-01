package com.meet.dev.analyzer.presentation.screen.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.awt.Cursor

@Composable
fun UpdateDialog(
    state: UpdateDialogState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .width(480.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Header
                val (headerBg, headerIcon, headerIconTint, headerTitle, headerSubtitle) = when (state) {
                    is UpdateDialogState.Checking ->
                        HeaderData(
                            bg = MaterialTheme.colorScheme.primaryContainer,
                            icon = Icons.Default.Sync,
                            iconTint = MaterialTheme.colorScheme.primary,
                            title = "Checking for Updates",
                            subtitle = "Please wait..."
                        )
                    is UpdateDialogState.Available ->
                        HeaderData(
                            bg = MaterialTheme.colorScheme.primaryContainer,
                            icon = Icons.Default.SystemUpdate,
                            iconTint = MaterialTheme.colorScheme.primary,
                            title = "Update Available",
                            subtitle = "Version ${state.version} is ready"
                        )
                    is UpdateDialogState.UpToDate ->
                        HeaderData(
                            bg = MaterialTheme.colorScheme.secondaryContainer,
                            icon = Icons.Default.CheckCircle,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            title = "You're Up to Date",
                            subtitle = "No updates available right now"
                        )
                    is UpdateDialogState.Error ->
                        HeaderData(
                            bg = MaterialTheme.colorScheme.errorContainer,
                            icon = Icons.Default.ErrorOutline,
                            iconTint = MaterialTheme.colorScheme.error,
                            title = "Update Failed",
                            subtitle = "Something went wrong"
                        )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = headerIcon,
                            contentDescription = null,
                            tint = headerIconTint,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = headerSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.pointerHoverIcon(
                            PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Body content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (state) {
                        is UpdateDialogState.Checking -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Looking for the latest version...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        is UpdateDialogState.Available -> {
                            val uriHandler = LocalUriHandler.current
                            Icon(
                                imageVector = Icons.Default.NewReleases,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "A new version (${state.version}) is available!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            if (state.releaseNotes.isNotBlank()) {
                                Text(
                                    text = "What's New:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                        val scrollState = rememberScrollState()
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .verticalScroll(scrollState)
                                        ) {
                                            Text(
                                                text = state.releaseNotes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { uriHandler.openUri(state.htmlUrl) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Open Release Page", style = MaterialTheme.typography.titleSmall)
                            }
                        }

                        is UpdateDialogState.UpToDate -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Your app is already on the latest version. Check back later for updates.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .pointerHoverIcon(
                                        PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
                                    ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Got it", style = MaterialTheme.typography.titleSmall)
                            }
                        }

                        is UpdateDialogState.Error -> {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .pointerHoverIcon(
                                        PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
                                    ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Dismiss", style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class HeaderData(
    val bg: Color,
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val subtitle: String
)