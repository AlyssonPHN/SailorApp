package com.marshall.sailorapp.ui.state

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marshall.sailorapp.rememberDeviceRotation
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

@Composable
fun rememberSailorState(
    hasAppeared: Boolean,
    isExpanded: Boolean,
    currentScreenHeightDp: Dp,
): SailorState {
    val deviceRotation by rememberDeviceRotation()

    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val rotation by animateFloatAsState(
        targetValue = -deviceRotation,
        animationSpec = spring(
            stiffness = Spring.StiffnessVeryLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        )
    )

    val targetPercentage = if (!hasAppeared) 0f else if (isExpanded) 0.3f else 0.1f
    val heightPercentage by animateFloatAsState(
        targetValue = targetPercentage,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
    )

    return remember(
        currentScreenHeightDp,
        heightPercentage,
        rotation,
        phase
    ) {
        SailorState(
            currentScreenHeightDp = currentScreenHeightDp,
            heightPercentage = heightPercentage,
            rotation = rotation,
            phase = phase
        )
    }
}

class SailorState(
    private val currentScreenHeightDp: Dp,
    private val heightPercentage: Float,
    val rotation: Float,
    val phase: Float
) {
    private val rotationAbs = abs(rotation)

    private val heightOffset: Dp = if (rotationAbs > 60f) {
        val progress = ((rotationAbs - 60f) / 30f).coerceIn(0f, 1f)
        currentScreenHeightDp * 0.25f * progress
    } else {
        0.dp
    }

    private val milkBodyHeight: Dp = (currentScreenHeightDp * heightPercentage) - heightOffset

    private val rad = rotation.toDouble() * (PI / 180.0)
    private val amplitudeFactor = 0.3f + 0.7f * abs(cos(rad)).toFloat()
    val waveAmplitude: Dp = 15.dp * amplitudeFactor

    val totalMilkHeight: Dp = if (milkBodyHeight > 0.dp) milkBodyHeight + waveAmplitude else 0.dp
}
