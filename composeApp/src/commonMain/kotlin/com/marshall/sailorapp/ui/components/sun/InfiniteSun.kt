package com.marshall.sailorapp.ui.components.sun

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform

@Composable
fun InfiniteSun(modifier: Modifier = Modifier, offsetX: Float, offsetY: Float, sunSize: Float, color: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        // Calculate the center of the sun based on the overall sunSize
        val sunCenterX = offsetX + sunSize / 2f
        val sunCenterY = offsetY + sunSize / 2f

        // Draw the central circle
        val circleRadius = sunSize * 0.25f
        drawCircle(color = color, radius = circleRadius, center = Offset(sunCenterX, sunCenterY))

        // Draw 8 rays around the central circle
        val rayWidth = sunSize * 0.08f
        val rayHeight = sunSize * 0.3f
        val rayCornerRadius = sunSize * 0.04f
        val rayOffsetFromCenter = circleRadius + (rayHeight / 2f) - (sunSize * 0.0f) // Adjusted for more detachment

        for (i in 0 until 8) {
            val angleDegrees = i * 45f
            withTransform({
                translate(left = sunCenterX, top = sunCenterY)
                rotate(degrees = sunRotation + angleDegrees, pivot = Offset.Zero)
                translate(left = -rayWidth / 2f, top = -rayOffsetFromCenter)
            }) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset.Zero,
                    size = Size(rayWidth, rayHeight),
                    cornerRadius = CornerRadius(rayCornerRadius, rayCornerRadius),
                    alpha = 1f
                )
            }
        }
    }
}
