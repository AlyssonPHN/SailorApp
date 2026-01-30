package com.marshall.sailorapp.ui.screens.sailor.effects

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.marshall.sailorapp.ui.components.clouds.InfiniteClouds
import com.marshall.sailorapp.ui.components.rain.RainEffect
import com.marshall.sailorapp.ui.screens.sailor.SailorState

@Composable
fun EffectsLayer(
    state: SailorState
) {
    val seaLevelYPx = state.seaLevelYPx
    if (seaLevelYPx <= 0f) return

    // ☁️ Clouds (with or without rain)
    if (state.showClouds || state.showRain) {
        InfiniteClouds(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0.3f),
            screenWidth = state.screenWidthPx,
            showRain = state.showRain,
            showClouds = state.showClouds,
            seaLevelYPx = seaLevelYPx
        )
    }

    // 🌧 Cloud with rain
    if (state.showRain) {
        RainEffect(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0.6f),
            screenWidthPx = state.screenWidthPx,
            screenHeightPx = state.screenHeightPx,
            seaLevelYPx = seaLevelYPx
        )
    }
}
