package com.marshall.sailorapp.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.marshall.sailorapp.model.SkyState

@Composable
fun SunAndMoon(
    skyState: SkyState,
    showRain: Boolean,
    currentSeaLevelYPx: Float,
    screenWidthPx: Float
) {
    val localDensity = LocalDensity.current

    val sunStartY = 80f
    val sunEndY = currentSeaLevelYPx + 200f

    val moonStartY = currentSeaLevelYPx + 200f
    val moonEndY = 80f

    val sunY by animateFloatAsState(
        targetValue = when (skyState) {
            SkyState.Day, SkyState.Sunrise -> sunStartY
            SkyState.Night, SkyState.Sunset -> sunEndY
        },
        animationSpec = tween(2600, easing = FastOutSlowInEasing)
    )

    val moonY by animateFloatAsState(
        targetValue = when (skyState) {
            SkyState.Night -> moonEndY
            SkyState.Sunrise -> moonStartY
            SkyState.Day, SkyState.Sunset -> moonStartY
        },
        animationSpec = tween(2600, easing = FastOutSlowInEasing)
    )

    if (skyState != SkyState.Night && !showRain) {
        InfiniteSun(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = true
                    shape = GenericShape { size, _ ->
                        addRect(
                            Rect(
                                left = 0f,
                                top = 0f,
                                right = size.width,
                                bottom = currentSeaLevelYPx
                            )
                        )
                    }
                },
            offsetX = with(localDensity) { 16.dp.toPx() },
            offsetY = sunY,
            sunSize = with(localDensity) { 150.dp.toPx() },
            color = Color(0xFFFFC107)
        )
    }

    if (skyState == SkyState.Night || skyState == SkyState.Sunrise) {
        InfiniteMoon(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = true
                    shape = GenericShape { size, _ ->
                        addRect(
                            Rect(
                                left = 0f,
                                top = 0f,
                                right = size.width,
                                bottom = currentSeaLevelYPx
                            )
                        )
                    }
                }
                .zIndex(0.2f),
            offsetX = with(localDensity) { 40.dp.toPx() },
            offsetY = moonY,
            moonSize = with(localDensity) { 120.dp.toPx() },
            color = Color(0xFFCFD8DC)
        )
    }
}
