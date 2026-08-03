package com.meet.dev.analyzer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser


@Composable
fun DevAnalyzerAnimatedLogo(
    modifier: Modifier = Modifier,
    animateDraw: Boolean = true,
    drawDurationMillis: Int = 1800
) {
    // Theme color
    val animatedColor by animateColorAsState(
        targetValue = MaterialTheme.colors.primary,
        animationSpec = tween(durationMillis = 600),
        label = "LogoColorAnimation"
    )

    val logoPaths = remember {
        listOf(
//            // Main 'DA' Outline and Magnifier body
            "M77.5 135.8C72.4 138.2 66 144.5 63.5 149.5C61.6 153.4 61.5 156.1 61.5 252.2V350.8L64.6 354.7C66.2 356.8 69.6 359.5 72 360.7C76.4 363 76.5 363 128.6 363C160.3 363 183.9 362.6 188.6 361.9C229 356.3 263.5 328.2 276.9 289.8C281.2 277.4 282.9 267 284 247C285.1 227.9 287.1 216.4 292 202.5C295.9 191.4 310.6 160.2 313.1 157.5C314.3 156.3 316.3 155.6 318.5 155.6C320.7 155.6 322.8 156.3 324 157.5C325.6 159.2 360.5 230 366.2 243.2C367.3 245.8 368.7 248 369.2 248C369.7 248 373.6 245.2 377.8 241.8C382 238.4 385.6 235.6 385.8 235.5C386.3 235.2 382.5 227.2 369.5 200.5C362.5 186.2 353.9 168.6 350.5 161.3C343.4 146.4 338.2 140 330.5 136.4C320 131.4 306.9 134.1 298 143C290.7 150.3 273.1 189 268 208.9C264.5 222.9 264 225.8 263 244.3C261.6 268.7 259.3 279.6 252.3 293.5C240.3 317.6 216.6 334.8 188.5 340C184.6 340.7 168.2 341 141.3 340.8L100.2 340.5L117.4 322.6L134.6 304.6L137.8 306.9C146.2 312.8 162.9 318 173.9 318C186.6 318 201.3 313.4 212.5 305.9C232.5 292.5 244 270.9 244 246.7C244 220.7 231.3 198.4 208.8 184.7C175 164.3 129.3 178.2 111.6 214.4C106.2 225.4 104.6 232.7 104.5 246.5C104.5 262.3 107.1 272 115.1 285.2L117.3 288.9L113.7 293.2C111.7 295.6 103.7 304 96 312L82.1 326.5L82 242.8V159.1L84.6 157.1C87.2 155 88.3 155 132.6 155C159.3 155 181.1 155.4 185.7 156.1C215.2 160.1 240.8 178.6 253.4 205C255.4 209.1 257 213 257 213.7C257 216.8 258.8 214.6 259.4 210.7C259.8 208.4 261.7 201.7 263.7 195.9L267.3 185.3L263 179.4C252.1 164.4 233.1 149.2 216.9 142.4C197.9 134.4 200.1 134.6 137.5 134.3C84.7 134 81.3 134.1 77.5 135.8ZM188.4 199C206.4 204.6 219.3 220.4 222.3 240.5C224.3 254.2 218.6 271.4 208.6 281.3C189.8 300.1 160.3 300.7 141.4 282.8C122.4 264.9 120.1 236.9 135.8 216.2C142.3 207.5 153 200.4 163 198C169.7 196.4 181.5 196.9 188.4 199Z",
//            // Magnifier Inner Highlight/Reflection
            "M171.3 208.1C167.8 211.6 171.3 218 176.7 218C180.1 218 188.3 221.4 192.4 224.5C197.1 228 202 237.9 202 243.7C202 250.8 207.5 254.5 211.5 250C212.9 248.5 213.2 246.9 212.8 242.3C211.5 225.2 194.9 208.4 178 207.2C174.4 207 172.1 207.3 171.3 208.1Z",
//            // Arrow and Graph Path
            "M428.299 209.4C416.599 211.1 406.999 212.8 406.999 213.3C406.999 213.8 408.599 216.1 410.499 218.5L414.099 222.8L377.799 252.1C357.899 268.3 336.899 285.3 331.199 289.8C323.199 296.4 320.399 298.1 318.699 297.6C317.499 297.3 310.499 291.8 303.099 285.4L289.599 273.7L287.299 282.1C282.999 297.5 282.299 295.2 294.799 305.9C307.799 317.1 309.499 318.1 317.099 318.7C328.999 319.8 321.399 325.2 410.099 253.4C419.199 246 426.999 240 427.299 240C427.599 240 429.499 242.3 431.499 245C433.599 247.8 435.499 249.9 435.799 249.6C436.799 248.6 450.699 206.9 450.099 206.6C449.799 206.5 439.899 207.7 428.299 209.4Z",
//            // Graph Bar 1
            "M326 251.1C326 282.9 326.1 284.1 327.8 282.8C328.8 282.1 333.8 278.1 338.8 274.1L348 266.6V242.3V218H337H326V251.1Z",
//            // Graph Bar 2
            "M295 253.801V267.601L305.6 277.101C311.4 282.301 316.6 286.701 317.1 286.801C317.7 287.001 317.9 277.601 317.8 263.801L317.5 240.501L306.3 240.201L295 239.901V253.801Z",
//            // Arrow Tail
            "M394.5 276.6C390.1 280.1 386.4 283.4 386.2 283.9C386 284.6 420.9 358 423.3 361.7C424.2 363.2 447 363.6 447 362.1C447 361.7 441.3 349.6 434.4 335.4C427.5 321.2 417.7 300.9 412.8 290.5C407.8 280 403.5 271.2 403.1 270.8C402.8 270.5 398.9 273 394.5 276.6Z"
        ).map { pathString ->
            PathParser().parsePathString(pathString).toPath()
        }
    }

    // Pre-measure lengths
    val pathMeasure = remember { PathMeasure() }
    val pathLengths = remember(logoPaths) {
        logoPaths.map { p ->
            pathMeasure.setPath(p, false)
            pathMeasure.length
        }
    }

    // Global progress 0..1
    val drawProgress = if (animateDraw) {
        val anim = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            anim.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = drawDurationMillis,
                    easing = FastOutSlowInEasing
                )
            )
        }
        anim.value
    } else {
        1f
    }


    Canvas(
        modifier = modifier
            .aspectRatio(1f)
    ) {
        val viewportWidth = 512f
        val scaleRatio = size.width / viewportWidth

        scale(scaleX = scaleRatio, scaleY = scaleRatio, pivot = Offset.Zero) {
            // Fill gradually for all paths
            if (drawProgress > 0f) {
                logoPaths.forEach { path ->
                    drawPath(
                        path = path,
                        color = animatedColor.copy(alpha = drawProgress),
                        style = Fill
                    )
                }
            }

            // Stroke animation : first path (main outline)
            val outlinePath = logoPaths.first()
            val outlineLength = pathLengths.first()
            val currentOutlineLength = outlineLength * drawProgress

            val animatedOutline = Path()
            pathMeasure.setPath(outlinePath, false)
            pathMeasure.getSegment(
                startDistance = 0f,
                stopDistance = currentOutlineLength,
                destination = animatedOutline,
                startWithMoveTo = true
            )

            drawPath(
                path = animatedOutline,
                color = animatedColor,
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Fill gradually all paths
            logoPaths.drop(1).forEachIndexed { index, originalPath ->
                val length = pathLengths[index + 1]
                val currentLength = length * drawProgress

                val animatedSegment = Path()
                pathMeasure.setPath(originalPath, false)
                pathMeasure.getSegment(
                    startDistance = 0f,
                    stopDistance = currentLength,
                    destination = animatedSegment,
                    startWithMoveTo = true
                )

                drawPath(
                    path = animatedSegment,
                    color = animatedColor,
                    style = Stroke(
                        width = 3f,   // patla stroke
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Bevel
                    )
                )
            }
        }
    }
}