package com.meet.project.analyzer.core.utility

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.jetbrains.compose.resources.painterResource

sealed interface IconResource {
    data class ImageVector(val imageVector: androidx.compose.ui.graphics.vector.ImageVector) :
        IconResource

    data class DrawableResource(val resID: org.jetbrains.compose.resources.DrawableResource) :
        IconResource

    @Composable
    fun asPainterResource(): Painter {
        return when (this) {
            is DrawableResource -> painterResource(resID)
            is ImageVector -> rememberVectorPainter(image = imageVector)
        }
    }

}