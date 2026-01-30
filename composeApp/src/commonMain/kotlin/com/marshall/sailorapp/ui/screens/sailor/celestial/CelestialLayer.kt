package com.marshall.sailorapp.ui.screens.sailor.celestial

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.marshall.sailorapp.data.model.SkyState
import com.marshall.sailorapp.ui.components.moon.InfiniteMoon
import com.marshall.sailorapp.ui.components.sky.StarrySky
import com.marshall.sailorapp.ui.components.sun.InfiniteSun
import com.marshall.sailorapp.ui.screens.sailor.SailorState

@Composable
fun CelestialLayer(
    state: SailorState
) {
    val density = LocalDensity.current

    val seaLevelYPx = state.seaLevelYPx
    if (seaLevelYPx <= 0f) return

    // posições verticais
    val sunStartY = 80f
    val sunEndY = seaLevelYPx + 200f

    val moonStartY = seaLevelYPx + 200f
    val moonEndY = 80f

    val sunY by animateFloatAsState(
        targetValue = when (state.skyState) {
            SkyState.Day, SkyState.Sunrise -> sunStartY
            SkyState.Night, SkyState.Sunset -> sunEndY
        },
        animationSpec = tween(2600, easing = FastOutSlowInEasing),
        label = "SunY"
    )

    val moonY by animateFloatAsState(
        targetValue = when (state.skyState) {
            SkyState.Night -> moonEndY
            SkyState.Sunrise -> moonStartY
            SkyState.Day, SkyState.Sunset -> moonStartY
        },
        animationSpec = tween(2600, easing = FastOutSlowInEasing),
        label = "MoonY"
    )

    val clipModifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
            clip = true
            shape = androidx.compose.foundation.shape.GenericShape { size, _ ->
                addRect(Rect(0f, 0f, size.width, seaLevelYPx))
            }
        }

    // ☀️ Sun
    if (state.skyState != SkyState.Night && !state.showRain) {
        InfiniteSun(
            modifier = clipModifier,
            offsetX = with(density) { 16.dp.toPx() },
            offsetY = sunY,
            sunSize = with(density) { 150.dp.toPx() },
            color = Color(0xFFFFC107)
        )
    }

    // 🌙 Mon
    if (state.skyState == SkyState.Night || state.skyState == SkyState.Sunrise) {
        InfiniteMoon(
            modifier = clipModifier,
            offsetX = with(density) { 40.dp.toPx() },
            offsetY = moonY,
            moonSize = with(density) { 120.dp.toPx() },
            color = Color(0xFFCFD8DC)
        )
    }

    // ✨ Stars
    if (state.skyState == SkyState.Night || state.skyState == SkyState.Sunrise) {
        StarrySky(
            modifier = Modifier.fillMaxSize(),
            screenWidthPx = state.screenWidthPx,
            screenHeightPx = state.screenHeightPx,
            seaLevelYPx = seaLevelYPx
        )
    }
}
