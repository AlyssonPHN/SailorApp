package com.marshall.sailorapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import com.marshall.sailorapp.model.SkyState
import com.marshall.sailorapp.ui.components.InfiniteClouds
import com.marshall.sailorapp.ui.components.InteractiveMenu
import com.marshall.sailorapp.ui.components.RainEffect
import com.marshall.sailorapp.ui.components.StarrySky
import com.marshall.sailorapp.ui.components.SunAndMoon
import com.marshall.sailorapp.ui.components.Waves
import com.marshall.sailorapp.ui.state.rememberSailorState


@Composable
fun SailorScreen() {
    val viewModel = remember { SailorViewModel() }

    Box(modifier = Modifier.fillMaxSize()) {
        SailorBackground(
            skyState = viewModel.skyState,
            onSkyStateChange = { viewModel.setSkyState(it) }
        ) {
            val localDensity = LocalDensity.current

            var screenWidthPx by remember { mutableStateOf(0f) }
            var screenHeightPx by remember { mutableStateOf(0f) }
            var totalMilkHeightPx by remember { mutableStateOf(0f) }

            LaunchedEffect(Unit) {
                viewModel.setHasAppeared(true)
            }

            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val sailorState = rememberSailorState(
                    hasAppeared = viewModel.hasAppeared,
                    isExpanded = viewModel.isExpanded,
                    currentScreenHeightDp = maxHeight
                )

                LaunchedEffect(sailorState.totalMilkHeight) {
                    totalMilkHeightPx = with(localDensity) { sailorState.totalMilkHeight.toPx() }
                }

                val currentScreenHeightDp = maxHeight
                val currentScreenWidthDp = maxWidth

                LaunchedEffect(currentScreenWidthDp, currentScreenHeightDp) {
                    screenWidthPx = with(localDensity) { currentScreenWidthDp.toPx() }
                    screenHeightPx = with(localDensity) { currentScreenHeightDp.toPx() }
                }

                Waves(
                    totalMilkHeight = sailorState.totalMilkHeight,
                    isExpanded = viewModel.isExpanded,
                    onExpandedChange = { viewModel.setExpanded(it) },
                    rotation = sailorState.rotation,
                    phase = sailorState.phase,
                    waveAmplitude = sailorState.waveAmplitude,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                val currentSeaLevelYPx = screenHeightPx - totalMilkHeightPx

                SunAndMoon(
                    skyState = viewModel.skyState,
                    showRain = viewModel.showRain,
                    currentSeaLevelYPx = currentSeaLevelYPx,
                    screenWidthPx = screenWidthPx
                )

                if (viewModel.skyState == SkyState.Night || viewModel.skyState == SkyState.Sunrise) {
                    StarrySky(
                        modifier = Modifier.fillMaxSize(),
                        screenWidthPx = screenWidthPx,
                        screenHeightPx = screenHeightPx,
                        seaLevelYPx = currentSeaLevelYPx
                    )
                }

                InteractiveMenu(
                    skyState = viewModel.skyState,
                    onSkyStateChange = { viewModel.setSkyState(it) },
                    onToggleClouds = { viewModel.toggleClouds() },
                    onToggleRain = { viewModel.toggleRain() },
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                if (viewModel.showClouds || viewModel.showRain) {
                    InfiniteClouds(
                        modifier = Modifier
                            .fillMaxSize().zIndex(0.3f)
                            .align(Alignment.TopStart),
                        screenWidth = screenWidthPx,
                        showRain = viewModel.showRain,
                        showClouds = viewModel.showClouds,
                        seaLevelYPx = currentSeaLevelYPx
                    )
                }

                if (viewModel.showRain) {
                    RainEffect(
                        modifier = Modifier.fillMaxSize().zIndex(0.6f),
                        screenWidthPx = screenWidthPx,
                        seaLevelYPx = currentSeaLevelYPx
                    )
                }

                if (viewModel.skyState == SkyState.Night || viewModel.skyState == SkyState.Sunrise) {
                    StarrySky(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(0.2f),
                        screenWidthPx = screenWidthPx,
                        screenHeightPx = screenHeightPx,
                        seaLevelYPx = currentSeaLevelYPx
                    )
                }
            }
        }
    }
}
