package com.marshall.sailorapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import com.marshall.sailorapp.model.RainDrop
import kotlin.random.Random

@Composable
fun RainEffect(
    modifier: Modifier = Modifier,
    screenWidthPx: Float,
    seaLevelYPx: Float
) {
    val rainDrops = remember { mutableStateListOf<RainDrop>() }
    val density = LocalDensity.current.density

    LaunchedEffect(Unit) {
        var lastFrameTime = 0L
        while (true) {
            withFrameNanos { frameTime ->
                val deltaTimeNs = if (lastFrameTime > 0) frameTime - lastFrameTime else 0L
                lastFrameTime = frameTime
                val deltaTimeSeconds = deltaTimeNs / 1_000_000_000f

                // Atualiza pingos
                for (i in rainDrops.indices.reversed()) {
                    val drop = rainDrops[i]
                    drop.y += drop.speed * deltaTimeSeconds * density
                    if (drop.y > seaLevelYPx) {
                        rainDrops.removeAt(i)
                    }
                }

                // Gera novos pingos
                if (Random.nextFloat() < 0.8f) {
                    rainDrops.add(
                        RainDrop(
                            x = Random.nextFloat() * screenWidthPx,
                            y = Random.nextFloat() * (seaLevelYPx * 0.4f),
                            length = Random.nextFloat() * 12f + 8f,
                            speed = Random.nextFloat() * 400f + 300f,
                            alpha = Random.nextFloat() * 0.6f + 0.4f
                        )
                    )
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        rainDrops.forEach { drop ->
            drawLine(
                color = Color.White.copy(alpha = drop.alpha),
                start = Offset(drop.x, drop.y),
                end = Offset(drop.x, drop.y + drop.length),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}
