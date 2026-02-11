package com.marshall.sailorapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun InfiniteMoon(modifier: Modifier = Modifier, offsetX: Float, offsetY: Float, moonSize: Float, color: Color) {
    val moonPhase = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        moonPhase.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 8000,
                easing = LinearEasing
            )
        )
    }

    Canvas(modifier = modifier) {
        val moonCenterX = offsetX + moonSize / 2f
        val moonCenterY = offsetY + moonSize / 2f
        val moonRadius = moonSize / 2f

        // Draw the main moon circle (this is the full moon behind the covering dark circle)
        drawCircle(color = color, radius = moonRadius, center = Offset(moonCenterX, moonCenterY))

        // Draw a subtle \"crater\" effect
        drawCircle(
            color = Color.DarkGray.copy(alpha = 0.3f),
            radius = moonRadius * 0.2f,
            center = Offset(moonCenterX - moonRadius * 0.3f, moonCenterY - moonRadius * 0.3f)
        )
        drawCircle(
            color = Color.DarkGray.copy(alpha = 0.2f),
            radius = moonRadius * 0.15f,
            center = Offset(moonCenterX + moonRadius * 0.4f, moonCenterY)
        )
        drawCircle(
            color = Color.DarkGray.copy(alpha = 0.25f),
            radius = moonRadius * 0.25f,
            center = Offset(moonCenterX, moonCenterY + moonRadius * 0.35f)
        )

        // Add a subtle glow effect
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = moonRadius * 1.1f,
            center = Offset(moonCenterX, moonCenterY)
        )

        // Simulating the moon phase by drawing a dark circle over a part of it
        // The `moonPhase` will control how much of the moon is \"covered\"

        // Animate the x-coordinate of the center of the covering dark circle.
        // At moonPhase = 0f, the dark circle is centered on the moon, covering it entirely (new moon).
        // As moonPhase progresses to 1f, the dark circle moves to the right,
        // eventually moving completely off-screen to the right, revealing the full moon.

        if (moonPhase.value < 0.98f) {
            val coveringCircleCenterX =
                moonCenterX + moonRadius * 2.5f * moonPhase.value

            drawCircle(
                color = Color.Black,
                radius = moonRadius,
                center = Offset(coveringCircleCenterX, moonCenterY)
            )
        }
    }
}
