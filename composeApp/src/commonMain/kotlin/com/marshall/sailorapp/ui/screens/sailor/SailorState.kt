package com.marshall.sailorapp.ui.screens.sailor

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.marshall.sailorapp.data.model.SkyState
import com.marshall.sailorapp.ui.components.rememberDeviceRotation
import com.marshall.sailorapp.ui.screens.sailor.menu.MenuAction

@Stable
class SailorState(
    var skyState: SkyState,
    var showClouds: Boolean,
    var showRain: Boolean,
) {
    var isSeaExpanded by mutableStateOf(true)
        private set

    var seaLevelYPx by mutableStateOf(0f)
        private set

    var rotation by mutableStateOf(0f)
        private set

    var screenWidthPx by mutableStateOf(0f)
        private set

    var screenHeightPx by mutableStateOf(0f)
        private set

    fun toggleSea() {
        isSeaExpanded = !isSeaExpanded
    }

    fun updateSeaLevel(value: Float) {
        seaLevelYPx = value
    }

    fun updateRotation(value: Float) {
        rotation = value
    }

    fun updateScreenSize(width: Float, height: Float) {
        screenWidthPx = width
        screenHeightPx = height
    }

    fun onMenuAction(action: MenuAction) {
        when (action) {
            MenuAction.ToggleClouds -> showClouds = !showClouds
            MenuAction.ToggleRain -> showRain = !showRain
            MenuAction.Day -> skyState = SkyState.Day
            MenuAction.Night -> skyState = SkyState.Night
            MenuAction.Sunrise -> skyState = SkyState.Sunrise
            MenuAction.Sunset -> skyState = SkyState.Sunset
        }
    }
}


@Composable
fun rememberSailorState(): SailorState {
    val state = remember {
        SailorState(
            skyState = SkyState.Day,
            showClouds = false,
            showRain = false
        )
    }

    val deviceRotation by rememberDeviceRotation()
    val animatedRotation by animateFloatAsState(
        targetValue = -deviceRotation,
        animationSpec = spring(
            stiffness = Spring.StiffnessVeryLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ),
        label = "SailorRotation"
    )

    LaunchedEffect(animatedRotation) {
        state.updateRotation(animatedRotation)
    }

    return state
}
