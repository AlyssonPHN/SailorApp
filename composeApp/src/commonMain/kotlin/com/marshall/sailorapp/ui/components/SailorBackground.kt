package com.marshall.sailorapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.marshall.sailorapp.model.SkyState
import kotlinx.coroutines.delay

@Composable
fun SailorBackground(
    skyState: SkyState,
    onSkyStateChange: (SkyState) -> Unit,
    content: @Composable () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (skyState) {
            SkyState.Day -> Color(0xFF00013E)
            SkyState.Sunset -> Color(0xFFFF8C42)
            SkyState.Night -> Color.Black
            SkyState.Sunrise -> Color(0xFFFFB347)
        },
        animationSpec = tween(2500)
    )

    LaunchedEffect(skyState) {
        when (skyState) {
            SkyState.Sunset -> {
                delay(2600)
                onSkyStateChange(SkyState.Night)
            }
            SkyState.Sunrise -> {
                delay(2600)
                onSkyStateChange(SkyState.Day)
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        content()
    }
}
