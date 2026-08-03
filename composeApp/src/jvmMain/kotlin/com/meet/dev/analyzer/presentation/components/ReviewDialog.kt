package com.meet.dev.analyzer.presentation.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meet.dev.analyzer.data.models.feedback.FeedbackData
import com.meet.dev.analyzer.data.repository.feedback.FeedbackRepository
import org.koin.compose.koinInject
import java.awt.Cursor
import kotlinx.coroutines.launch

@Composable
fun ReviewDialog(
    appVersion: String,
    onDismiss: () -> Unit,
    onReviewSubmitted: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val feedbackRepository = koinInject<FeedbackRepository>()

    var rating by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var likesAndImprovements by remember { mutableStateOf("") }
    var mostUsedFeatures by remember { mutableStateOf(emptySet<String>()) }
    var futureFeatures by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccess by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    val systemInfo = remember {
        val osName = System.getProperty("os.name")
        val osVersion = System.getProperty("os.version")
        "DevAnalyzer $appVersion | $osName $osVersion"
    }

    val isFormValid = rating > 0 && name.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        modifier = if (!submitSuccess) Modifier.height(700.dp) else Modifier,
        title = {
            if (!submitSuccess) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DevAnalyzerAnimatedLogo(
                        modifier = Modifier.size(36.dp),
                        animateDraw = true,
                        drawDurationMillis = 1200
                    )
                    Text(
                        text = "Share Your Feedback",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            if (submitSuccess) {
                val scale = remember { Animatable(0f) }
                val alpha = remember { Animatable(0f) }

                LaunchedEffect(Unit) {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }

                LaunchedEffect(Unit) {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 600, delayMillis = 100)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DevAnalyzerAnimatedLogo(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(scale.value),
                        animateDraw = true,
                        drawDurationMillis = 1500
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Thank You!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.graphicsLayer(alpha = alpha.value)
                    )

                    Text(
                        text = "Your feedback has been submitted successfully.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.graphicsLayer(alpha = alpha.value)
                    )

                    Text(
                        text = "Your input helps improve DevAnalyzer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.graphicsLayer(alpha = alpha.value)
                    )
                }
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Rating stars
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "How would you rate DevAnalyzer? *",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { index ->
                                val isSelected = index <= rating
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$index Stars",
                                    tint = if (isSelected) Color(0xFFF7C325) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { rating = index }
                                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                                )
                            }
                        }
                    }

                    // Name Field (Required)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Your Name *",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Enter your name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Text(
                            text = "This name may be displayed publicly with your review on the DevAnalyzer website.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    // Likes and improvements (Optional)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "What do you like about DevAnalyzer, or what can be improved? (Optional)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = likesAndImprovements,
                            onValueChange = { likesAndImprovements = it },
                            placeholder = { Text("Share your thoughts...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }

                    // Most used features
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Which features do you use the most?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val featuresList = listOf(
                            "Project Analyzer",
                            "Storage Analyzer",
                            "Workspace Analyzer",
                            "Clean Build"
                        )
                        featuresList.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { feature ->
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                mostUsedFeatures = if (mostUsedFeatures.contains(feature)) {
                                                    mostUsedFeatures - feature
                                                } else {
                                                    mostUsedFeatures + feature
                                                }
                                            }
                                            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = mostUsedFeatures.contains(feature),
                                            onCheckedChange = { checked ->
                                                mostUsedFeatures = if (checked) {
                                                    mostUsedFeatures + feature
                                                } else {
                                                    mostUsedFeatures - feature
                                                }
                                            }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(feature, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }

                    // Future wishlist
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Any feature you'd like to see in future versions?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = futureFeatures,
                            onValueChange = { futureFeatures = it },
                            placeholder = { Text("e.g. duplicate dependency scanner, project comparison...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }

                    // Email (Optional)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Email (Optional)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("your.email@example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Text(
                            text = "Your email address (if provided) will never be displayed publicly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    submitError?.let { error ->
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (submitSuccess) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                ) {
                    Text("Close")
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (isFormValid) {
                                isSubmitting = true
                                submitError = null
                                coroutineScope.launch {
                                    val result = feedbackRepository.submitFeedback(
                                        FeedbackData(
                                            rating = rating,
                                            name = name,
                                            likesAndImprovements = likesAndImprovements,
                                            mostUsedFeatures = mostUsedFeatures.toList(),
                                            futureFeatures = futureFeatures,
                                            systemInfo = systemInfo,
                                            email = email
                                        )
                                    )
                                    if (result.isSuccess) {
                                        onReviewSubmitted(rating)
                                        submitSuccess = true
                                    } else {
                                        submitError = result.exceptionOrNull()?.message ?: "Unknown network error"
                                    }
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = isFormValid && !isSubmitting,
                        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Submitting...")
                        } else {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    )
}
