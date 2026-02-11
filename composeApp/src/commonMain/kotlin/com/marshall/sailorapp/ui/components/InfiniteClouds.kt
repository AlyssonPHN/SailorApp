package com.marshall.sailorapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.marshall.sailorapp.model.Cloud
import kotlin.random.Random

@Composable
fun InfiniteClouds(modifier: Modifier = Modifier, screenWidth: Float, showRain: Boolean, showClouds: Boolean, seaLevelYPx: Float) {
    val clouds = remember { mutableStateListOf<Cloud>() }
    val density = LocalDensity.current.density

    // Define constants for ship\'s dimensions to calculate minimum cloud height
    val shipPivotToWaveTopOffsetDp = 15.dp
    val shipSailHeightAbstract = 32f * 4.5f // Abstract units from ship\'s local coordinates, scaled
    val cloudClearancePaddingDp = 0.dp // Padding between the cloud bottom and ship\'s highest point

    // Calculate these once per recomposition if dependencies change
    val shipPivotToWaveTopOffsetPx = with(LocalDensity.current) { shipPivotToWaveTopOffsetDp.toPx() }
    val shipSailHeightPx = shipSailHeightAbstract * density // Convert abstract units to pixels
    val cloudClearancePaddingPx = with(LocalDensity.current) { cloudClearancePaddingDp.toPx() }

    // This is the global Y of the absolute highest point of the ship (top of sail)
    // The ship\'s pivot can be at `seaLevelYPx - shipPivotToWaveTopOffsetPx` (highest point of the pivot on screen)
    // The sail goes `shipSailHeightPx` upwards from there.
    val actualShipHighestPointYPx = seaLevelYPx - shipPivotToWaveTopOffsetPx - shipSailHeightPx

    // Re-initialize clouds when showClouds or showRain state changes
    LaunchedEffect(showClouds, showRain) {
        clouds.clear()
        if (showRain) {
            // Create a single cloud that spans the entire screen width with undulations.
            val size = screenWidth / density * 1.5f // Adjusted multiplier to ensure full coverage and overlap
            val speed = 0f // Clouds are static when raining

            // Ensure clouds are above the ship
            val alpha = Random.nextFloat() * 0.4f + 0.6f // Denser and more opaque
            val color = Color.Gray // Grayish clouds for rain
//            clouds.add(Cloud(x = 0f, y = y, size = size, speed = speed, alpha = alpha, color = color))
            List(3) { index ->
                clouds.add(Cloud(
                    x = index * 260f,
                    y = 0f,
                    size = size,
                    speed = speed,
                    alpha = alpha,
                    color = color,
                    showRain
                ))
            }
        } else if (showClouds) {
            // When showClouds is true (and not raining), generate an initial set of normal clouds
            repeat(Random.nextInt(3, 6)) { // Generate 3 to 5 normal clouds initially
                val size = Random.nextFloat() * 40f + 30f
                val speed = Random.nextFloat() * 50f + 20f
                val cloudHeightInPixels = (20.0f / 24f) * size * density

                // Ensure clouds are above the ship
                val maxCloudTopYAllowed = actualShipHighestPointYPx - cloudHeightInPixels - cloudClearancePaddingPx

                val y = Random.nextFloat() * maxCloudTopYAllowed.coerceAtLeast(0f) // Y position
                val alpha = Random.nextFloat() * 0.4f + 0.3f
                val color = Color.White.copy(alpha = alpha)
                clouds.add(Cloud(x = Random.nextFloat() * screenWidth, y = y, size = size, speed = speed, alpha = alpha, color = color, showRain)) // Position them anywhere on screen
            }
        }
    }

    LaunchedEffect(Unit) { // This LaunchedEffect handles continuous cloud movement and periodic generation
        var lastFrameTime = 0L
        while (true) {
            withFrameNanos { frameTime ->
                val deltaTimeNs = if (lastFrameTime > 0) frameTime - lastFrameTime else 0L
                lastFrameTime = frameTime
                val deltaTimeSeconds = deltaTimeNs / 1_000_000_000f

                // Update existing clouds
                for (i in clouds.indices.reversed()) {
                    val cloud = clouds[i]
                    if (!showRain) { // Only move clouds if it\'s NOT raining
                        cloud.x -= cloud.speed * deltaTimeSeconds * density // Normal clouds move right to left
                        // Remove if off-screen to the left, adjusted for density.
                        // For rain, clouds are static and not removed this way.
                        if (cloud.x < -cloud.size * density * 2) {
                            clouds.removeAt(i)
                        }
                    }
                    // If showRain is true, clouds do not move and are not removed based on x position.
                }

                // Add new clouds periodically only if clouds are meant to be shown (either normal or rainy)
                // Do not add new clouds periodically if it\'s raining, as they are initially generated to cover the sky.
                if (showClouds && !showRain && Random.nextFloat() < 0.02f) { // Only add new clouds if showClouds is true AND not raining
                    val size = Random.nextFloat() * 40f + 30f
                    val speed = Random.nextFloat() * 50f + 20f
                    val cloudHeightInPixels = (20.0f / 24f) * size * density

                    // Ensure clouds are above the ship
                    val maxCloudTopYAllowed = actualShipHighestPointYPx - cloudHeightInPixels - cloudClearancePaddingPx

                    val y = Random.nextFloat() * maxCloudTopYAllowed.coerceAtLeast(0f) // Y position
                    val alpha = Random.nextFloat() * 0.4f + 0.3f
                    val color = Color.White.copy(alpha = alpha)

                    clouds.add(Cloud(x = screenWidth + size * density * 2, y = y, size = size, speed = speed, alpha = alpha, color = color, showRain)) // Start from right, adjusted for density
                }
            }
        }
    }

    // Cloud rain
    Canvas(modifier = modifier) {
        clouds.forEach { cloud ->
            val cloudSizePx: Float = if (showRain) {
                cloud.size
            } else {
                cloud.size * density
            }

            val iconGridSize = 24f
            val scaleFactor = cloudSizePx / iconGridSize

            val cloudPath = Path().apply {
                // Shape real da nuvem (versão igual ao ícone da imagem sem chuva)

                moveTo(19.35f * scaleFactor, 10.04f * scaleFactor)

                cubicTo(
                    18.67f * scaleFactor, 6.59f * scaleFactor,
                    15.64f * scaleFactor, 4.0f * scaleFactor,
                    12.0f * scaleFactor, 4.0f * scaleFactor
                )

                cubicTo(
                    9.11f * scaleFactor, 4.0f * scaleFactor,
                    6.6f * scaleFactor, 5.64f * scaleFactor,
                    5.35f * scaleFactor, 8.04f * scaleFactor
                )

                cubicTo(
                    2.34f * scaleFactor, 8.36f * scaleFactor,
                    0.0f * scaleFactor, 10.91f * scaleFactor,
                    0.0f * scaleFactor, 14.0f * scaleFactor
                )

                // Parte inferior correta (reta curva suave, NÃO ondulada)
                cubicTo(
                    0.0f * scaleFactor, 17.31f * scaleFactor,
                    2.69f * scaleFactor, 20.0f * scaleFactor,
                    6.0f * scaleFactor, 20.0f * scaleFactor
                )

                lineTo(18.0f * scaleFactor, 20.0f * scaleFactor)

                cubicTo(
                    21.31f * scaleFactor, 20.0f * scaleFactor,
                    24.0f * scaleFactor, 17.31f * scaleFactor,
                    24.0f * scaleFactor, 14.0f * scaleFactor
                )

                cubicTo(
                    24.0f * scaleFactor, 11.03f * scaleFactor,
                    22.05f * scaleFactor, 8.53f * scaleFactor,
                    19.35f * scaleFactor, 10.04f * scaleFactor
                )

                close()
            }

            val isRainCloud = showRain && cloud.isRainCloud // 👈 flag da nuvem

            val rotationDegrees = if (isRainCloud) 180f else 0f
            val alpha = if (isRainCloud) 0.95f else cloud.alpha

            withTransform({
                translate(left = cloud.x, top = cloud.y)

                if (rotationDegrees != 0f) {
                    rotate(
                        degrees = rotationDegrees,
                        pivot = Offset(cloudSizePx / 2f, cloudSizePx / 2.5f)
                    )
                }
            }) {
                drawPath(
                    path = cloudPath,
                    color = cloud.color,
                    alpha = alpha
                )
            }

        }
    }
}
