package com.marshall.sailorapp.ui.screens.sailor.background

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.marshall.sailorapp.data.model.SkyState

@Composable
fun SkyBackground(
    skyState: SkyState
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (skyState) {
            SkyState.Day -> Color(0xFF00013E)
            SkyState.Sunset -> Color(0xFFFF8C42)
            SkyState.Night -> Color.Black
            SkyState.Sunrise -> Color(0xFFFFB347)
        },
        animationSpec = tween(durationMillis = 2500),
        label = "SkyBackgroundColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    )
}
