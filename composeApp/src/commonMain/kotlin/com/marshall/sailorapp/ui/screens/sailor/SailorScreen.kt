package com.marshall.sailorapp.ui.screens.sailor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.marshall.sailorapp.ui.screens.sailor.background.SkyBackground
import com.marshall.sailorapp.ui.screens.sailor.celestial.CelestialLayer
import com.marshall.sailorapp.ui.screens.sailor.effects.EffectsLayer
import com.marshall.sailorapp.ui.screens.sailor.menu.FloatingMenu
import com.marshall.sailorapp.ui.screens.sailor.sea.SeaLayer
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun SailorScreenUI() {
    val state = rememberSailorState()

    Box(Modifier.fillMaxSize()) {

        SkyBackground(state.skyState)

        SeaLayer(
            rotation = state.rotation,
            onToggleExpand = state::toggleSea
        ) { seaLevelYPx ->
            state.updateSeaLevel(seaLevelYPx)
        }

        CelestialLayer(state)

        EffectsLayer(state)

        FloatingMenu(
            skyState = state.skyState,
            showClouds = state.showClouds,
            showRain = state.showRain,
            onAction = state::onMenuAction
        )
    }
}
