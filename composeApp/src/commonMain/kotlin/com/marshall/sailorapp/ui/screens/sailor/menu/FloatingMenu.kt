package com.marshall.sailorapp.ui.screens.sailor.menu

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.marshall.sailorapp.data.model.SkyState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FloatingMenu(
    skyState: SkyState,
    showClouds: Boolean,
    showRain: Boolean,
    onAction: (MenuAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val transition = updateTransition(
        targetState = expanded,
        label = "FloatingMenuTransition"
    )

    val menuOffset by transition.animateValue(
        typeConverter = DpOffset.VectorConverter,
        transitionSpec = { spring(stiffness = 300f) },
        label = "MenuOffset"
    ) { if (it) DpOffset.Zero else DpOffset(0.dp, (-40).dp) }

    val menuAlpha by transition.animateFloat(
        transitionSpec = { tween(250) },
        label = "MenuAlpha"
    ) { if (it) 1f else 0f }

    val infiniteTransition = rememberInfiniteTransition(label = "MenuOrbit")
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitRotation"
    )

    val icons = listOf(
        Icons.Default.Cloud to MenuAction.ToggleClouds,
        Icons.Default.WbSunny to MenuAction.Day,
        Icons.Default.Grain to MenuAction.ToggleRain,
        Icons.Default.Brightness2 to MenuAction.Night
    )

    val orbRadius = 18.dp
    val iconSize = 24.dp
    val centerSize = 48.dp

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(top = 80.dp, end = 48.dp),
        contentAlignment = Alignment.Center
    ) {

        // Botão central invisível
        Box(
            modifier = Modifier
                .size(centerSize)
                .clip(CircleShape)
                .background(Color.Transparent)
                .clickable { expanded = !expanded }
        )

        // Ícones orbitando quando fechado
        if (!expanded) {
            icons.forEachIndexed { index, (icon, _) ->
                val angle = (orbitRotation + index * (360f / icons.size)) * PI / 180f
                val offset = with(LocalDensity.current) {
                    Offset(
                        (orbRadius.toPx() * cos(angle)).toFloat(),
                        (orbRadius.toPx() * sin(angle)).toFloat()
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(iconSize)
                        .align(Alignment.Center)
                        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                )
            }
        }

        // Menu expandido
        if (expanded) {
            Card(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            menuOffset.x.toPx().roundToInt(),
                            menuOffset.y.toPx().roundToInt()
                        )
                    }
                    .wrapContentSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    icons.forEach { (icon, action) ->
                        IconButton(
                            onClick = {
                                expanded = false
                                onAction(action)
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
