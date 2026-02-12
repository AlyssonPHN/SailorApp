package com.marshall.sailorapp

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.marshall.sailorapp.model.SkyState
import com.marshall.sailorapp.ui.components.InfiniteClouds
import com.marshall.sailorapp.ui.components.InfiniteMoon
import com.marshall.sailorapp.ui.components.InfiniteSun
import com.marshall.sailorapp.ui.components.InteractiveMenu
import com.marshall.sailorapp.ui.components.RainEffect
import com.marshall.sailorapp.ui.components.StarrySky
import com.marshall.sailorapp.ui.components.Waves
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos


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

            LaunchedEffect(Unit) {
                viewModel.setHasAppeared(true)
            }

            val targetPercentage = if (!viewModel.hasAppeared) 0f else if (viewModel.isExpanded) 0.3f else 0.1f

            val heightPercentage by animateFloatAsState(
                targetValue = targetPercentage,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )

            val infiniteTransition = rememberInfiniteTransition()
            val phase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2 * PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            val deviceRotation by rememberDeviceRotation()
            val rotation by animateFloatAsState(
                targetValue = -deviceRotation,
                animationSpec = spring(
                    stiffness = Spring.StiffnessVeryLow,
                    dampingRatio = Spring.DampingRatioLowBouncy
                )
            )

            var totalMilkHeightPx by remember { mutableStateOf(0f) }

            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val currentScreenHeightDp = maxHeight
                val currentScreenWidthDp = maxWidth

                LaunchedEffect(currentScreenWidthDp, currentScreenHeightDp) {
                    screenWidthPx = with(localDensity) { currentScreenWidthDp.toPx() }
                    screenHeightPx = with(localDensity) { currentScreenHeightDp.toPx() }
                }

                val rotationAbs = abs(rotation)
                val heightOffset = if (rotationAbs > 60f) {
                    val progress = ((rotationAbs - 60f) / 30f).coerceIn(0f, 1f)
                    currentScreenHeightDp * 0.25f * progress
                } else {
                    0.dp
                }

                val milkBodyHeight = (currentScreenHeightDp * heightPercentage) - heightOffset

                val rad = rotation.toDouble() * (PI / 180.0)
                val amplitudeFactor = 0.3f + 0.7f * abs(cos(rad)).toFloat()
                val waveAmplitude = 15.dp * amplitudeFactor

                val totalMilkHeight =
                    if (milkBodyHeight > 0.dp) milkBodyHeight + waveAmplitude else 0.dp

                LaunchedEffect(totalMilkHeight) {
                    totalMilkHeightPx = with(localDensity) { totalMilkHeight.toPx() }
                }

                Waves(
                    totalMilkHeight = totalMilkHeight,
                    isExpanded = viewModel.isExpanded,
                    onExpandedChange = { viewModel.setExpanded(it) },
                    rotation = rotation,
                    phase = phase,
                    waveAmplitude = waveAmplitude,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            val currentSeaLevelYPx = screenHeightPx - totalMilkHeightPx

            val sunStartY = 80f
            val sunEndY = currentSeaLevelYPx + 200f

            val moonStartY = currentSeaLevelYPx + 200f
            val moonEndY = 80f

            val sunY by animateFloatAsState(
                targetValue = when (viewModel.skyState) {
                    SkyState.Day, SkyState.Sunrise -> sunStartY
                    SkyState.Night, SkyState.Sunset -> sunEndY
                },
                animationSpec = tween(2600, easing = FastOutSlowInEasing)
            )

            val moonY by animateFloatAsState(
                targetValue = when (viewModel.skyState) {
                    SkyState.Night -> moonEndY
                    SkyState.Sunrise -> moonStartY
                    SkyState.Day, SkyState.Sunset -> moonStartY
                },
                animationSpec = tween(2600, easing = FastOutSlowInEasing)
            )

            if (viewModel.skyState != SkyState.Night && !viewModel.showRain) {
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

            if (viewModel.skyState == SkyState.Night || viewModel.skyState == SkyState.Sunrise) {
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
