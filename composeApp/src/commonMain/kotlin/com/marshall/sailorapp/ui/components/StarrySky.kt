package com.marshall.sailorapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.marshall.sailorapp.model.Star
import kotlin.math.PI
import kotlin.random.Random

@Composable
fun StarrySky(
    modifier: Modifier = Modifier,
    screenWidthPx: Float,
    screenHeightPx: Float,
    seaLevelYPx: Float
) {
    val density = LocalDensity.current.density
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

    Canvas(modifier = modifier.fillMaxSize()) {
        val time = System.nanoTime() / 1_000_000_000f


        stars.forEach { star ->
            val twinkle =
                kotlin.math.sin(time * star.speed + star.phase).toFloat() * star.amplitude


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
