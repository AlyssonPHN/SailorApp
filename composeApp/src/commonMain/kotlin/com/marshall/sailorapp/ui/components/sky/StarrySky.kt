package com.marshall.sailorapp.ui.components.sky

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import com.marshall.sailorapp.data.model.Star

@Composable
fun StarrySky(
    modifier: Modifier = Modifier,
    screenWidthPx: Float,
    screenHeightPx: Float,
    seaLevelYPx: Float
) {
    val density = LocalDensity.current.density

    // Estrelas são criadas UMA VEZ
    val stars = remember(screenWidthPx, screenHeightPx, seaLevelYPx) {
        if (screenWidthPx == 0f || screenHeightPx == 0f) return@remember emptyList()

        val maxY = (seaLevelYPx - 140.dp.value * density).coerceAtLeast(0f)

        buildList {
            repeat(100) {
                add(
                    Star(
                        x = Random.nextFloat() * screenWidthPx,
                        y = Random.nextFloat() * maxY,
                        radius = Random.nextFloat() * 1.5f + 1f,
                        phase = Random.nextFloat() * (2f * PI.toFloat()),
                        speed = Random.nextFloat() * 0.6f + 0.2f, // star slow
                        amplitude = Random.nextFloat() * 0.15f + 0.05f,
                        baseAlpha = Random.nextFloat() * 0.3f + 0.55f
                    )
                )
            }
        }
    }


    var frameTimeNanos by remember { mutableStateOf(0L) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val time = frameTimeNanos / 1_000_000_000f


        stars.forEach { star ->
            val twinkle =
                sin(time * star.speed + star.phase).toFloat() * star.amplitude


            val alpha = (star.baseAlpha + twinkle)
                .coerceIn(0.4f, 0.9f)

            drawCircle(
                color = Color.White,
                radius = star.radius,
                center = Offset(star.x, star.y),
                alpha = alpha
            )
        }
    }
}
