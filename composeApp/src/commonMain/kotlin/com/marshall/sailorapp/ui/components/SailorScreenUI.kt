package com.marshall.sailorapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.marshall.sailorapp.data.model.SkyState
import com.marshall.sailorapp.ui.components.clouds.InfiniteClouds
import com.marshall.sailorapp.ui.components.moon.InfiniteMoon
import com.marshall.sailorapp.ui.components.rain.RainEffect
import com.marshall.sailorapp.ui.components.sky.StarrySky
import com.marshall.sailorapp.ui.components.sun.InfiniteSun
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SailorScreenUI() {
    var showClouds by remember { mutableStateOf(false) } // New state for clouds
    var showRain by remember { mutableStateOf(false) } // New state for rain
    var skyState by remember { mutableStateOf(SkyState.Day) }

    val backgroundColor by animateColorAsState(
        targetValue = when (skyState) {
            SkyState.Day -> Color(0xFF00013E)
            SkyState.Sunset -> Color(0xFFFF8C42)
            SkyState.Night -> Color.Black
            SkyState.Sunrise -> Color(0xFFFFB347)
        },
        animationSpec = tween(2500)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        var isExpanded by remember { mutableStateOf(true) }
        var hasAppeared by remember { mutableStateOf(false) }

        val localDensity = LocalDensity.current
        var screenWidthPx by remember { mutableStateOf(0f) }
        var screenHeightPx by remember { mutableStateOf(0f) }

        LaunchedEffect(Unit) { hasAppeared = true }

        val targetPercentage = if (!hasAppeared) 0f else if (isExpanded) 0.3f else 0.1f

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

        val deviceRotation by rememberDeviceRotation() // Assuming this is defined elsewhere
        val rotation by animateFloatAsState(
            targetValue = -deviceRotation,
            animationSpec = spring(
                stiffness = Spring.StiffnessVeryLow,
                dampingRatio = Spring.DampingRatioLowBouncy
            )
        )

        var totalMilkHeightPx by remember { mutableStateOf(0f) }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalMilkHeight)
                    .align(Alignment.BottomCenter)
                    .clickable { isExpanded = !isExpanded }
                    .zIndex(0.5f)
            ) {
                val width = size.width
                val height = size.height

                withTransform({
                    rotate(degrees = -rotation, pivot = Offset(width / 2, height / 2))
                    scale(scaleX = 1.2f, scaleY = 1.2f, pivot = Offset(width / 2, height / 2))
                }) {
                    val midLineY = waveAmplitude.toPx()
                    val drawRange = max(width, height) * 4f
                    val startX = (width - drawRange) / 2
                    val endX = startX + drawRange

                    val pathBack = Path().apply {
                        moveTo(startX, height + 4000f)
                        lineTo(startX, midLineY)
                        var x = startX
                        while (x <= endX) {
                            val sine = sin((x / width) * 4 * PI + phase.toDouble() + 1.0).toFloat()
                            val yPos = midLineY + (waveAmplitude.toPx() * 0.7f * sine)
                            lineTo(x, yPos)
                            x += 10f
                        }
                        lineTo(endX, midLineY)
                        lineTo(endX, height + 4000f)
                        close()
                    }
                    drawPath(pathBack, color = Color(0xFF4FC3F7))

                    val shipX = width / 2
                    val waveXFactor = (shipX / width) * 2.5 * PI + phase.toDouble()
                    val shipY =
                        midLineY + (waveAmplitude.toPx() * sin(waveXFactor)).toFloat() - 15.dp.toPx()

                    val delta = 5f
                    val waveNextXFactor = ((shipX + delta) / width) * 2.5 * PI + phase.toDouble()
                    val wavePrevXFactor = ((shipX - delta) / width) * 2.5 * PI + phase.toDouble()
                    val yNext = midLineY + (waveAmplitude.toPx() * sin(waveNextXFactor)).toFloat()
                    val yPrev = midLineY + (waveAmplitude.toPx() * sin(wavePrevXFactor)).toFloat()
                    val angle =
                        atan2((yNext - yPrev).toDouble(), (2 * delta).toDouble()) * (180 / PI)

                    withTransform({
                        translate(left = shipX, top = shipY)
                        rotate(degrees = angle.toFloat(), pivot = Offset.Zero)
                        scale(scaleX = 4.5f, scaleY = 4.5f, pivot = Offset.Zero)
                    }) {
                        val hullPath = Path().apply {
                            moveTo(-40f, -10f); lineTo(40f, -10f); lineTo(20f, 15f); lineTo(
                            -20f,
                            15f
                        ); close()
                        }
                        drawPath(hullPath, color = Color.White)
                        val sailPath = Path().apply {
                            moveTo(0f, -10f); lineTo(0f, -55f); lineTo(30f, -10f); close()
                        }
                        drawPath(sailPath, color = Color(0xFF1565C0))
                        val foldPath = Path().apply {
                            moveTo(0f, -10f); lineTo(0f, -40f); lineTo(-20f, -10f); close()
                        }
                        drawPath(foldPath, color = Color(0xFF90CAF9))
                    }

                    val pathFront = Path().apply {
                        moveTo(startX, height + 4000f)
                        lineTo(startX, midLineY)
                        var x = startX
                        while (x <= endX) {
                            val sine = sin((x / width) * 2.5 * PI + phase.toDouble()).toFloat()
                            val yPos = midLineY + (waveAmplitude.toPx() * sine)
                            lineTo(x, yPos)
                            x += 10f
                        }
                        lineTo(endX, midLineY)
                        lineTo(endX, height + 4000f)
                        close()
                    }
                    drawPath(pathFront, color = Color(0xFF039BE5))
                }
            }
        }

        val currentSeaLevelYPx = screenHeightPx - totalMilkHeightPx
        val sunStartY = 80f
        val sunEndY = currentSeaLevelYPx + 200f
        val moonStartY = currentSeaLevelYPx + 200f
        val moonEndY = 80f

        val sunY by animateFloatAsState(
            targetValue = when (skyState) {
                SkyState.Day, SkyState.Sunrise -> sunStartY
                SkyState.Night, SkyState.Sunset -> sunEndY
            },
            animationSpec = tween(2600, easing = FastOutSlowInEasing)
        )

        val moonY by animateFloatAsState(
            targetValue = when (skyState) {
                SkyState.Night -> moonEndY
                SkyState.Sunrise -> moonStartY
                SkyState.Day, SkyState.Sunset -> moonStartY
            },
            animationSpec = tween(2600, easing = FastOutSlowInEasing)
        )

        LaunchedEffect(skyState) {
            when (skyState) {
                SkyState.Sunset -> {
                    delay(2600); skyState = SkyState.Night
                }

                SkyState.Sunrise -> {
                    delay(2600); skyState = SkyState.Day
                }

                else -> Unit
            }
        }

        LaunchedEffect(skyState, moonY) {
            if (skyState == SkyState.Sunrise && moonY >= moonStartY - 1f) {
                skyState = SkyState.Day
            }
        }

        if (skyState != SkyState.Night && !showRain) {
            InfiniteSun(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        clip = true
                        shape = GenericShape { size, _ ->
                            addRect(Rect(0f, 0f, size.width, currentSeaLevelYPx))
                        }
                    },
                offsetX = with(localDensity) { 16.dp.toPx() },
                offsetY = sunY,
                sunSize = with(localDensity) { 150.dp.toPx() },
                color = Color(0xFFFFC107)
            )
        }

        if (skyState == SkyState.Night || skyState == SkyState.Sunrise) {
            InfiniteMoon(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        clip = true
                        shape = GenericShape { size, _ ->
                            addRect(Rect(0f, 0f, size.width, currentSeaLevelYPx))
                        }
                    }
                    .zIndex(0.2f),
                offsetX = with(localDensity) { 40.dp.toPx() },
                offsetY = moonY,
                moonSize = with(localDensity) { 120.dp.toPx() },
                color = Color(0xFFCFD8DC)
            )
        }

        if (skyState == SkyState.Night || skyState == SkyState.Sunrise) {
            StarrySky(
                modifier = Modifier.fillMaxSize(),
                screenWidthPx = screenWidthPx,
                screenHeightPx = screenHeightPx,
                seaLevelYPx = currentSeaLevelYPx
            )
        }

        var isMenuExpanded by remember { mutableStateOf(false) }
        val menuTransition =
            updateTransition(targetState = isMenuExpanded, label = "menuTransition")
        val menuOffset by menuTransition.animateValue(
            typeConverter = DpOffset.VectorConverter,
            transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) }, label = "menuOffset"
        ) { expanded -> if (expanded) DpOffset(0.dp, 0.dp) else DpOffset(0.dp, (-50).dp) }
        val menuAlpha by menuTransition.animateFloat(
            transitionSpec = { tween(durationMillis = 300) },
            label = "menuAlpha"
        ) { expanded -> if (expanded) 1f else 0f }
        val iconRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        val orbRadius = 18.dp
        val iconSize = 24.dp
        val centerCircleSize = 48.dp

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 48.dp)
                .zIndex(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(centerCircleSize)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable { isMenuExpanded = !isMenuExpanded }
                    .align(Alignment.Center)
            )

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

            if (isMenuExpanded) {
                Card(
                    modifier = Modifier
                        .wrapContentSize(Alignment.CenterEnd)
                        .offset {
                            IntOffset(
                                menuOffset.x.toPx().roundToInt(),
                                menuOffset.y.toPx().roundToInt()
                            )
                        }
                        .graphicsLayer {
                            alpha = menuAlpha
                            shape = RoundedCornerShape(16.dp)
                            clip = true
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),

                    ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        icons.forEach { icon ->
                            IconButton(
                                onClick = {
                                    isMenuExpanded = false
                                    when (icon) {
                                        Icons.Default.Cloud -> showClouds = !showClouds
                                        Icons.Default.WbSunny -> {
                                            skyState = when (skyState) {
                                                SkyState.Night -> SkyState.Sunrise
                                                SkyState.Sunset -> SkyState.Day
                                                else -> SkyState.Day
                                            }
                                            showClouds = false
                                        }

                                        Icons.Default.Grain -> showRain = !showRain
                                        Icons.Default.Brightness2 -> {
                                            skyState = when (skyState) {
                                                SkyState.Day -> SkyState.Sunset
                                                SkyState.Night -> SkyState.Sunrise
                                                else -> skyState
                                            }
                                        }
                                    }
                                },
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

        if (showClouds || showRain) {
            InfiniteClouds(
                modifier = Modifier.fillMaxSize().zIndex(0.3f).align(Alignment.TopStart),
                screenWidth = screenWidthPx,
                showRain = showRain,
                showClouds = showClouds,
                seaLevelYPx = currentSeaLevelYPx
            )
        }

        if (showRain) {
            RainEffect(
                modifier = Modifier.fillMaxSize().zIndex(0.6f),
                screenWidthPx = screenWidthPx,
                screenHeightPx = screenHeightPx,
                seaLevelYPx = currentSeaLevelYPx
            )
        }
    }
}
