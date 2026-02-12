package com.marshall.sailorapp.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.marshall.sailorapp.model.SkyState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun InteractiveMenu(
    skyState: SkyState,
    onSkyStateChange: (SkyState) -> Unit,
    onToggleClouds: () -> Unit,
    onToggleRain: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val localDensity = LocalDensity.current

    val menuTransition =
        updateTransition(targetState = isMenuExpanded, label = "menuTransition")

    val menuOffset by menuTransition.animateValue(
        typeConverter = DpOffset.VectorConverter,
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
        label = "menuOffset"
    ) { expanded ->
        if (expanded) DpOffset(0.dp, 0.dp) else DpOffset(
            0.dp,
            (-50).dp
        ) // Adjusted to start higher
    }

    val menuAlpha by menuTransition.animateFloat(
        transitionSpec = { tween(durationMillis = 300) }, label = "menuAlpha"
    ) { expanded ->
        if (expanded) 1f else 0f
    }

    val infiniteTransition = rememberInfiniteTransition()
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Orb Radius
    val orbRadius = 18.dp
    val iconSize = 24.dp
    val centerCircleSize = 48.dp

    Box(
        modifier = modifier
            .padding(top = 80.dp, end = 48.dp)
            .zIndex(1f) // Ensure the menu is above other elements
    ) {
        // Central clickable circle
        Box(
            modifier = Modifier
                .size(centerCircleSize)
                .clip(CircleShape)
                .background(Color.Transparent) // Changed background to Transparent
                .clickable { isMenuExpanded = !isMenuExpanded }
                .align(Alignment.Center)
        )

        // Orbiting Icons
        val icons = listOf(
            Icons.Default.Cloud,
            Icons.Default.WbSunny,
            Icons.Default.Grain,
            Icons.Default.Brightness2
        )
        icons.forEachIndexed { index, icon ->
            val angle = (iconRotation + index * (360f / icons.size)) * PI.toFloat() / 180f
            val offsetX = with(localDensity) { (orbRadius.toPx() * cos(angle)).toDp() }
            val offsetY = with(localDensity) { (orbRadius.toPx() * sin(angle)).toDp() }

            if (!isMenuExpanded) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(iconSize)
                        .offset(x = offsetX, y = offsetY)
                        .align(Alignment.Center)
                )
            }
        }

        // Expanded Glassmorphic Card
        if (isMenuExpanded) {
            Card(
                modifier = Modifier
                    .wrapContentSize(Alignment.CenterEnd) // Changed fillMaxWidth to wrapContentSize
                    .offset { // Apply animated offset here
                        with(localDensity) {
                            IntOffset(
                                x = menuOffset.x.toPx().roundToInt(),
                                y = menuOffset.y.toPx().roundToInt()
                            )
                        }
                    }
                    // efeito glassmorphic multiplataforma
                    .graphicsLayer {
                        alpha = menuAlpha // Use animated alpha here
                        shape = RoundedCornerShape(16.dp)
                        clip = true
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),

                ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally), // Aligned column content to center
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.forEach { icon ->
                        IconButton(
                            onClick = {
                                isMenuExpanded = false
                                when (icon) {
                                    Icons.Default.Cloud -> onToggleClouds()
                                    Icons.Default.WbSunny -> {
                                        onSkyStateChange(
                                            when (skyState) {
                                                SkyState.Night -> SkyState.Sunrise
                                                SkyState.Sunset -> SkyState.Day
                                                else -> SkyState.Day
                                            }
                                        )
                                    }
                                    Icons.Default.Grain -> onToggleRain()
                                    Icons.Default.Brightness2 -> {
                                        onSkyStateChange(
                                            when (skyState) {
                                                SkyState.Day -> SkyState.Sunset
                                                SkyState.Night -> SkyState.Sunrise
                                                else -> skyState
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier // Removed fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
