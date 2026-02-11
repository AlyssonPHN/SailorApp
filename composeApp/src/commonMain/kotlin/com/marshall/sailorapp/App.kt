package com.marshall.sailorapp


import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI

import kotlin.math.cos

import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.roundToInt // Import added for roundToInt
import kotlin.random.Random // Import for Random

// Imports for the new feature
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Grain // Represents rain
import androidx.compose.material.icons.filled.Brightness2 // Represents sunset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity // Import LocalDensity
import androidx.compose.ui.geometry.Size
import com.marshall.sailorapp.model.Cloud
import com.marshall.sailorapp.model.RainDrop
import com.marshall.sailorapp.model.SkyState
import com.marshall.sailorapp.model.Star
import com.marshall.sailorapp.ui.components.StarrySky
import com.marshall.sailorapp.ui.components.Waves





@Composable
@Preview
fun App() {
    MaterialTheme {
        SailorScreen()
    }
}

@Composable
fun SailorScreen() {
    var showClouds by remember { mutableStateOf(false) } // New state for clouds
//    var showSun by remember { mutableStateOf(false) } // New state for sun
    var showRain by remember { mutableStateOf(false) } // New state for rain
//    var showMoon by remember { mutableStateOf(false) } // New state for moon

    var skyState by remember { mutableStateOf(SkyState.Day) }

    Box(modifier = Modifier.fillMaxSize()) { // Wrap content in a Box
        SailorBackground(skyState = skyState, onSkyStateChange = { skyState = it }) {
            var isExpanded by remember { mutableStateOf(true) }
            var hasAppeared by remember { mutableStateOf(false) }


            val localDensity = LocalDensity.current // Declare LocalDensity here

            // Use mutableStateOf for screen dimensions so they can be updated from BoxWithConstraints
            var screenWidthPx by remember { mutableStateOf(0f) }
            var screenHeightPx by remember { mutableStateOf(0f) }

            // Animação de entrada
            LaunchedEffect(Unit) {
                hasAppeared = true
            }

            // Define a porcentagem da altura: 0% -> 30% (entrada), depois alterna entre 30% e 10%
            val targetPercentage = if (!hasAppeared) 0f else if (isExpanded) 0.3f else 0.1f

            val heightPercentage by animateFloatAsState(
                targetValue = targetPercentage,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )

            // Animação infinita para as ondas
            val infiniteTransition = rememberInfiniteTransition()
            val phase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2 * PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            // Rotação do dispositivo (com suavização)
            val deviceRotation by rememberDeviceRotation()
            val rotation by animateFloatAsState(
                targetValue = -deviceRotation,
                animationSpec = spring(stiffness = Spring.StiffnessVeryLow, dampingRatio = Spring.DampingRatioLowBouncy)
            )

            // Declaring totalMilkHeightPx here to make it accessible outside BoxWithConstraints
            var totalMilkHeightPx by remember { mutableStateOf(0f) }

            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val currentScreenHeightDp = maxHeight
                val currentScreenWidthDp = maxWidth

                // Update screen dimensions in pixels
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

                // Altura da parte líquida cheia
                val milkBodyHeight = (currentScreenHeightDp * heightPercentage) - heightOffset

                // Altura extra para as ondas não serem cortadas
                // Ajustamos a amplitude das ondas com base na rotação:
                // quando chega perto de 90 graus (landscape), reduzimos a amplitude.
                val rad = rotation.toDouble() * (PI / 180.0)
                val amplitudeFactor = 0.3f + 0.7f * abs(cos(rad)).toFloat()
                val waveAmplitude = 15.dp * amplitudeFactor

                // Altura total do container do leite
                val totalMilkHeight = if (milkBodyHeight > 0.dp) milkBodyHeight + waveAmplitude else 0.dp

                // Update totalMilkHeightPx from here
                LaunchedEffect(totalMilkHeight) {
                    totalMilkHeightPx = with(localDensity) { totalMilkHeight.toPx() }
                }

                                Waves(
                    totalMilkHeight = totalMilkHeight,
                    isExpanded = isExpanded,
                    onExpandedChange = { isExpanded = it },
                    rotation = rotation,
                    phase = phase,
                    waveAmplitude = waveAmplitude,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Calculate sea level in Px here, after BoxWithConstraints has set screenHeightPx and totalMilkHeightPx
            val currentSeaLevelYPx = screenHeightPx - totalMilkHeightPx

            // Position Sun
            val sunStartY = 80f
            val sunEndY = currentSeaLevelYPx + 200f

            // Position moon
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
                    SkyState.Night -> moonEndY        // lua no céu
                    SkyState.Sunrise -> moonStartY    // 👈 lua DESCENDO
                    SkyState.Day, SkyState.Sunset -> moonStartY
                },
                animationSpec = tween(2600, easing = FastOutSlowInEasing)
            )




            // Renderization Sun
            if (skyState != SkyState.Night && !showRain) {
                InfiniteSun(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            clip = true
                            shape = GenericShape { size, _ ->
                                // Área visível: tudo ACIMA do nível do mar
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


            // Renderetion Moon
            if (skyState == SkyState.Night || skyState == SkyState.Sunrise) {
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
                    offsetY = moonY, // 👈 mesma lógica do sunY
                    moonSize = with(localDensity) { 120.dp.toPx() },
                    color = Color(0xFFCFD8DC)
                )
            }


            // Renderention Stars
            if (skyState == SkyState.Night || skyState == SkyState.Sunrise) {
                StarrySky(
                    modifier = Modifier.fillMaxSize(),
                    screenWidthPx = screenWidthPx,
                    screenHeightPx = screenHeightPx,
                    seaLevelYPx = currentSeaLevelYPx
                )
            }





            // --- Novo Menu Interativo ---
            var isMenuExpanded by remember { mutableStateOf(false) }

            val menuTransition = updateTransition(targetState = isMenuExpanded, label = "menuTransition")

            val menuOffset by menuTransition.animateValue(
                typeConverter = DpOffset.VectorConverter,
                transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) }, label = "menuOffset"
            ) { expanded ->
                if (expanded) DpOffset(0.dp, 0.dp) else DpOffset(0.dp, (-50).dp) // Adjusted to start higher
            }

            val menuAlpha by menuTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 300) }, label = "menuAlpha"
            ) { expanded ->
                if (expanded) 1f else 0f
            }

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

            // Removed LocalDensity.current from here, it\'s now at the top of SailorScreen

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd) // Adjusted padding here
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
                val icons = listOf(Icons.Default.Cloud, Icons.Default.WbSunny, Icons.Default.Grain, Icons.Default.Brightness2)
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
                            // .blur(16.dp) // <<< REMOVED THIS LINE
                            .offset { // Apply animated offset here
                                with(localDensity) {
                                    IntOffset(x = menuOffset.x.toPx().roundToInt(), y = menuOffset.y.toPx().roundToInt())
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
                                        Icons.Default.Cloud -> {
                                            showClouds = !showClouds
                                        }

                                        Icons.Default.WbSunny -> {
                                            skyState = when (skyState) {
                                                SkyState.Night -> {
                                                    SkyState.Sunrise   // lua começa a descer
                                                }

                                                SkyState.Sunset -> {
                                                    SkyState.Day
                                                }

                                                else -> {
                                                    SkyState.Day
                                                }
                                            }

                                            showClouds = false
                                        }


                                        Icons.Default.Grain -> {
                                            showRain = !showRain
                                        }

                                        Icons.Default.Brightness2 -> {
                                            skyState = when (skyState) {
                                                SkyState.Day -> SkyState.Sunset
                                                SkyState.Night -> SkyState.Sunrise
                                                else -> skyState
                                            }
                                        }
                                    }

                                },
                                modifier = Modifier // Removed fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    // You can add Text here to describe the button
                                    // Text(text = icon.name.substringAfterLast("."), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Infinite Clouds
        if (showClouds || showRain) { // Show clouds if either showClouds or showRain is true
            InfiniteClouds(
                modifier = Modifier
                    .fillMaxSize().zIndex(0.3f)
                    .align(Alignment.TopStart),
                screenWidth = screenWidthPx,
                showRain = showRain, // Pass new state
                showClouds = showClouds, // Pass showClouds state
                seaLevelYPx = currentSeaLevelYPx // Pass sea level
            )
        }

        // Rain Effect
        if (showRain) {
            RainEffect(
                modifier = Modifier.fillMaxSize().zIndex(0.6f),
                screenWidthPx = screenWidthPx,
                seaLevelYPx = currentSeaLevelYPx
            )
        }

        // Starry Sky
        if (skyState == SkyState.Night || skyState == SkyState.Sunrise) {

            StarrySky(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0.2f), // Behind the moon, but above background
                screenWidthPx = screenWidthPx,
                screenHeightPx = screenHeightPx,
                seaLevelYPx = currentSeaLevelYPx // Pass sea level to StarrySky
            )
        }

//        // Infinite Moon
//        if (skyState == SkyState.Night || skyState == SkyState.Sunrise) {
//
//            val moonSizeDp = 120.dp
//            val moonSizePx = with(localDensity) { moonSizeDp.toPx() }
//            val paddingDp = 40.dp
////            val moonOffsetX = with(localDensity) { screenWidthPx - moonSizePx - paddingDp.toPx() } // Alinhado à direita com padding
//            val moonOffsetX = with(localDensity) { paddingDp.toPx() } // Alinhado à direita com padding
//            val moonOffsetY = with(localDensity) { paddingDp.toPx() } // Alinhado ao topo com padding
//            InfiniteMoon(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .align(Alignment.TopStart)
//                    .zIndex(0.2f), // ZIndex slightly higher than stars
//                offsetX = moonOffsetX,
//                offsetY = moonOffsetY,
//                moonSize = moonSizePx,
//                color = Color(0xFFCFD8DC) // Light grey for the moon
//            )
//        }
        }
    }
}

@Composable
fun InfiniteClouds(modifier: Modifier = Modifier, screenWidth: Float, showRain: Boolean, showClouds: Boolean, seaLevelYPx: Float) {
    val clouds = remember { mutableStateListOf<Cloud>() }
    val density = LocalDensity.current.density

    // Define constants for ship\'s dimensions to calculate minimum cloud height
    val shipPivotToWaveTopOffsetDp = 15.dp
    val shipSailHeightAbstract = 32f * 4.5f // Abstract units from ship\'s local coordinates, scaled
    val cloudClearancePaddingDp = 0.dp // Padding between the cloud bottom and ship\'s highest point

    // Calculate these once per recomposition if dependencies change
    val shipPivotToWaveTopOffsetPx = with(LocalDensity.current) { shipPivotToWaveTopOffsetDp.toPx() }
    val shipSailHeightPx = shipSailHeightAbstract * density // Convert abstract units to pixels
    val cloudClearancePaddingPx = with(LocalDensity.current) { cloudClearancePaddingDp.toPx() }

    // This is the global Y of the absolute highest point of the ship (top of sail)
    // The ship\'s pivot can be at `seaLevelYPx - shipPivotToWaveTopOffsetPx` (highest point of the pivot on screen)
    // The sail goes `shipSailHeightPx` upwards from there.
    val actualShipHighestPointYPx = seaLevelYPx - shipPivotToWaveTopOffsetPx - shipSailHeightPx

    // Re-initialize clouds when showClouds or showRain state changes
    LaunchedEffect(showClouds, showRain) {
        clouds.clear()
        if (showRain) {
            // Create a single cloud that spans the entire screen width with undulations.
            val size = screenWidth / density * 1.5f // Adjusted multiplier to ensure full coverage and overlap
            val speed = 0f // Clouds are static when raining

            // Ensure clouds are above the ship
            val alpha = Random.nextFloat() * 0.4f + 0.6f // Denser and more opaque
            val color = Color.Gray // Grayish clouds for rain
//            clouds.add(Cloud(x = 0f, y = y, size = size, speed = speed, alpha = alpha, color = color))
            List(3) { index ->
                clouds.add(Cloud(
                    x = index * 260f,
                    y = 0f,
                    size = size,
                    speed = speed,
                    alpha = alpha,
                    color = color,
                    showRain
                ))
            }
        } else if (showClouds) {
            // When showClouds is true (and not raining), generate an initial set of normal clouds
            repeat(Random.nextInt(3, 6)) { // Generate 3 to 5 normal clouds initially
                val size = Random.nextFloat() * 40f + 30f
                val speed = Random.nextFloat() * 50f + 20f
                val cloudHeightInPixels = (20.0f / 24f) * size * density

                // Ensure clouds are above the ship
                val maxCloudTopYAllowed = actualShipHighestPointYPx - cloudHeightInPixels - cloudClearancePaddingPx

                val y = Random.nextFloat() * maxCloudTopYAllowed.coerceAtLeast(0f) // Y position
                val alpha = Random.nextFloat() * 0.4f + 0.3f
                val color = Color.White.copy(alpha = alpha)
                clouds.add(Cloud(x = Random.nextFloat() * screenWidth, y = y, size = size, speed = speed, alpha = alpha, color = color, showRain)) // Position them anywhere on screen
            }
        }
    }

    LaunchedEffect(Unit) { // This LaunchedEffect handles continuous cloud movement and periodic generation
        var lastFrameTime = 0L
        while (true) {
            withFrameNanos { frameTime ->
                val deltaTimeNs = if (lastFrameTime > 0) frameTime - lastFrameTime else 0L
                lastFrameTime = frameTime
                val deltaTimeSeconds = deltaTimeNs / 1_000_000_000f

                // Update existing clouds
                for (i in clouds.indices.reversed()) {
                    val cloud = clouds[i]
                    if (!showRain) { // Only move clouds if it\'s NOT raining
                        cloud.x -= cloud.speed * deltaTimeSeconds * density // Normal clouds move right to left
                        // Remove if off-screen to the left, adjusted for density.
                        // For rain, clouds are static and not removed this way.
                        if (cloud.x < -cloud.size * density * 2) {
                            clouds.removeAt(i)
                        }
                    }
                    // If showRain is true, clouds do not move and are not removed based on x position.
                }

                // Add new clouds periodically only if clouds are meant to be shown (either normal or rainy)
                // Do not add new clouds periodically if it\'s raining, as they are initially generated to cover the sky.
                if (showClouds && !showRain && Random.nextFloat() < 0.02f) { // Only add new clouds if showClouds is true AND not raining
                    val size = Random.nextFloat() * 40f + 30f
                    val speed = Random.nextFloat() * 50f + 20f
                    val cloudHeightInPixels = (20.0f / 24f) * size * density

                    // Ensure clouds are above the ship
                    val maxCloudTopYAllowed = actualShipHighestPointYPx - cloudHeightInPixels - cloudClearancePaddingPx

                    val y = Random.nextFloat() * maxCloudTopYAllowed.coerceAtLeast(0f) // Y position
                    val alpha = Random.nextFloat() * 0.4f + 0.3f
                    val color = Color.White.copy(alpha = alpha)

                    clouds.add(Cloud(x = screenWidth + size * density * 2, y = y, size = size, speed = speed, alpha = alpha, color = color, showRain)) // Start from right, adjusted for density
                }
            }
        }
    }

    // Cloud rain
    Canvas(modifier = modifier) {
        clouds.forEach { cloud ->
            val cloudSizePx: Float = if (showRain) {
                cloud.size
            } else {
                cloud.size * density
            }

            val iconGridSize = 24f
            val scaleFactor = cloudSizePx / iconGridSize

            val cloudPath = Path().apply {
                // Shape real da nuvem (versão igual ao ícone da imagem sem chuva)

                moveTo(19.35f * scaleFactor, 10.04f * scaleFactor)

                cubicTo(
                    18.67f * scaleFactor, 6.59f * scaleFactor,
                    15.64f * scaleFactor, 4.0f * scaleFactor,
                    12.0f * scaleFactor, 4.0f * scaleFactor
                )

                cubicTo(
                    9.11f * scaleFactor, 4.0f * scaleFactor,
                    6.6f * scaleFactor, 5.64f * scaleFactor,
                    5.35f * scaleFactor, 8.04f * scaleFactor
                )

                cubicTo(
                    2.34f * scaleFactor, 8.36f * scaleFactor,
                    0.0f * scaleFactor, 10.91f * scaleFactor,
                    0.0f * scaleFactor, 14.0f * scaleFactor
                )

                // Parte inferior correta (reta curva suave, NÃO ondulada)
                cubicTo(
                    0.0f * scaleFactor, 17.31f * scaleFactor,
                    2.69f * scaleFactor, 20.0f * scaleFactor,
                    6.0f * scaleFactor, 20.0f * scaleFactor
                )

                lineTo(18.0f * scaleFactor, 20.0f * scaleFactor)

                cubicTo(
                    21.31f * scaleFactor, 20.0f * scaleFactor,
                    24.0f * scaleFactor, 17.31f * scaleFactor,
                    24.0f * scaleFactor, 14.0f * scaleFactor
                )

                cubicTo(
                    24.0f * scaleFactor, 11.03f * scaleFactor,
                    22.05f * scaleFactor, 8.53f * scaleFactor,
                    19.35f * scaleFactor, 10.04f * scaleFactor
                )

                close()
            }

            val isRainCloud = showRain && cloud.isRainCloud // 👈 flag da nuvem

            val rotationDegrees = if (isRainCloud) 180f else 0f
            val alpha = if (isRainCloud) 0.95f else cloud.alpha

            withTransform({
                translate(left = cloud.x, top = cloud.y)

                if (rotationDegrees != 0f) {
                    rotate(
                        degrees = rotationDegrees,
                        pivot = Offset(cloudSizePx / 2f, cloudSizePx / 2.5f)
                    )
                }
            }) {
                drawPath(
                    path = cloudPath,
                    color = cloud.color,
                    alpha = alpha
                )
            }

        }
    }
}

@Composable
fun RainEffect(
    modifier: Modifier = Modifier,
    screenWidthPx: Float,
    seaLevelYPx: Float
) {
    val rainDrops = remember { mutableStateListOf<RainDrop>() }
    val density = LocalDensity.current.density

    LaunchedEffect(Unit) {
        var lastFrameTime = 0L
        while (true) {
            withFrameNanos { frameTime ->
                val deltaTimeNs = if (lastFrameTime > 0) frameTime - lastFrameTime else 0L
                lastFrameTime = frameTime
                val deltaTimeSeconds = deltaTimeNs / 1_000_000_000f

                // Atualiza pingos
                for (i in rainDrops.indices.reversed()) {
                    val drop = rainDrops[i]
                    drop.y += drop.speed * deltaTimeSeconds * density
                    if (drop.y > seaLevelYPx) {
                        rainDrops.removeAt(i)
                    }
                }

                // Gera novos pingos
                if (Random.nextFloat() < 0.8f) {
                    rainDrops.add(
                        RainDrop(
                            x = Random.nextFloat() * screenWidthPx,
                            y = Random.nextFloat() * (seaLevelYPx * 0.4f),
                            length = Random.nextFloat() * 12f + 8f,
                            speed = Random.nextFloat() * 400f + 300f,
                            alpha = Random.nextFloat() * 0.6f + 0.4f
                        )
                    )
                }
            }
        }
    }

    // 🔥 ISSO É O QUE ESTAVA FALTANDO
    Canvas(modifier = modifier.fillMaxSize()) {
        rainDrops.forEach { drop ->
            drawLine(
                color = Color.White.copy(alpha = drop.alpha),
                start = Offset(drop.x, drop.y),
                end = Offset(drop.x, drop.y + drop.length),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun InfiniteSun(modifier: Modifier = Modifier, offsetX: Float, offsetY: Float, sunSize: Float, color: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        // Calculate the center of the sun based on the overall sunSize
        val sunCenterX = offsetX + sunSize / 2f
        val sunCenterY = offsetY + sunSize / 2f

        // Draw the central circle
        val circleRadius = sunSize * 0.25f
        drawCircle(color = color, radius = circleRadius, center = Offset(sunCenterX, sunCenterY))

        // Draw 8 rays around the central circle
        val rayWidth = sunSize * 0.08f
        val rayHeight = sunSize * 0.3f
        val rayCornerRadius = sunSize * 0.04f
        val rayOffsetFromCenter = circleRadius + (rayHeight / 2f) - (sunSize * 0.0f) // Adjusted for more detachment

        for (i in 0 until 8) {
            val angleDegrees = i * 45f
            withTransform({
                translate(left = sunCenterX, top = sunCenterY)
                rotate(degrees = sunRotation + angleDegrees, pivot = Offset.Zero)
                translate(left = -rayWidth / 2f, top = -rayOffsetFromCenter)
            }) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset.Zero,
                    size = Size(rayWidth, rayHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(rayCornerRadius, rayCornerRadius),
                    alpha = 1f
                )
            }
        }
    }
}

@Composable
fun InfiniteMoon(modifier: Modifier = Modifier, offsetX: Float, offsetY: Float, moonSize: Float, color: Color) {
    val moonPhase = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        moonPhase.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 8000,
                easing = LinearEasing
            )
        )
    }

    Canvas(modifier = modifier) {
        val moonCenterX = offsetX + moonSize / 2f
        val moonCenterY = offsetY + moonSize / 2f
        val moonRadius = moonSize / 2f

        // Draw the main moon circle (this is the full moon behind the covering dark circle)
        drawCircle(color = color, radius = moonRadius, center = Offset(moonCenterX, moonCenterY))

        // Draw a subtle \"crater\" effect
        drawCircle(
            color = Color.DarkGray.copy(alpha = 0.3f),
            radius = moonRadius * 0.2f,
            center = Offset(moonCenterX - moonRadius * 0.3f, moonCenterY - moonRadius * 0.3f)
        )
        drawCircle(
            color = Color.DarkGray.copy(alpha = 0.2f),
            radius = moonRadius * 0.15f,
            center = Offset(moonCenterX + moonRadius * 0.4f, moonCenterY)
        )
        drawCircle(
            color = Color.DarkGray.copy(alpha = 0.25f),
            radius = moonRadius * 0.25f,
            center = Offset(moonCenterX, moonCenterY + moonRadius * 0.35f)
        )

        // Add a subtle glow effect
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = moonRadius * 1.1f,
            center = Offset(moonCenterX, moonCenterY)
        )

        // Simulating the moon phase by drawing a dark circle over a part of it
        // The `moonPhase` will control how much of the moon is \"covered\"

        // Animate the x-coordinate of the center of the covering dark circle.
        // At moonPhase = 0f, the dark circle is centered on the moon, covering it entirely (new moon).
        // As moonPhase progresses to 1f, the dark circle moves to the right,
        // eventually moving completely off-screen to the right, revealing the full moon.

        if (moonPhase.value < 0.98f) {
            val coveringCircleCenterX =
                moonCenterX + moonRadius * 2.5f * moonPhase.value

            drawCircle(
                color = Color.Black,
                radius = moonRadius,
                center = Offset(coveringCircleCenterX, moonCenterY)
            )
        }
    }
}
