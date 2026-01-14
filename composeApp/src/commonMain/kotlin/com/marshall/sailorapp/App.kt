package com.marshall.sailorapp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.graphics.RenderEffect // Added for BlurEffect
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity // Import LocalDensity
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlinx.coroutines.delay


@Composable
@Preview
fun App() {
    MaterialTheme {
        SailorScreen()
    }
}

data class Cloud(
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float,
    val color: Color // Add color property
)

// New data class for RainDrop
data class RainDrop(
    var x: Float,
    var y: Float,
    val length: Float,
    val speed: Float,
    val alpha: Float
)

// New data class for Star
data class Star(
    val id: Int, // Unique ID for each star to help with animations
    val x: Float,
    val y: Float,
    val size: Float,
    val twinkleDuration: Int, // Duration of one twinkle cycle
    val twinkleOffset: Int,   // Offset to stagger twinkling
    val isSouthernCross: Boolean = false // Flag for special handling if needed
)

enum class SkyState {
    Day,
    Sunset,
    Night,
    Sunrise
}

@Composable
fun SailorScreen() {
    var showClouds by remember { mutableStateOf(false) } // New state for clouds
//    var showSun by remember { mutableStateOf(false) } // New state for sun
    var showRain by remember { mutableStateOf(false) } // New state for rain
//    var showMoon by remember { mutableStateOf(false) } // New state for moon

    var skyState by remember { mutableStateOf(SkyState.Day) }


//    val backgroundColor = if (showMoon) Color.Black else Color(0xFF00013E) // Change background based on moon

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
            .background(backgroundColor) // Fundo Roxo Escuro ou Preto
    ) {
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

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalMilkHeight)
                    .align(Alignment.BottomCenter)
                    .clickable { isExpanded = !isExpanded }
                    .zIndex(0.5f) // Added zIndex here
            ) {
                val width = size.width
                val height = size.height

                // Aplicamos a rotação do dispositivo a todo o desenho
                withTransform({
                    rotate(degrees = -rotation, pivot = Offset(width / 2, height / 2))
                    scale(scaleX = 1.2f, scaleY = 1.2f, pivot = Offset(width / 2, height / 2))
                }) {

                    // O nível médio da superfície do leite
                    val midLineY = waveAmplitude.toPx()

                    // Definindo limites de desenho expandidos para cobrir a tela ao rotacionar
                    // Calculamos uma largura que cubra a diagonal com folga
                    val drawRange = max(width, height) * 4f
                    val startX = (width - drawRange) / 2
                    val endX = startX + drawRange

                    // Onda de trás (Sombra/Mais escura para dar profundidade)
                    val pathBack = Path()
                    pathBack.moveTo(startX, height + 4000f) // Começa bem embaixo
                    pathBack.lineTo(startX, midLineY) // Sobe até o inicio da onda

                    // Loop estendido
                    var x = startX
                    while (x <= endX) {
                        // Fase deslocada e frequência um pouco diferente
                        val sine = sin((x / width) * 4 * PI + phase.toDouble() + 1.0).toFloat()
                        val yPos = midLineY + (waveAmplitude.toPx() * 0.7f * sine)
                        pathBack.lineTo(x, yPos)
                        x += 10f
                    }
                    pathBack.lineTo(endX, midLineY)
                    pathBack.lineTo(endX, height + 4000f)
                    pathBack.close()

                    drawPath(pathBack, color = Color(0xFF4FC3F7)) // Azul claro

                    // --- Navio ---
                    val shipX = width / 2
                    // Usando a onda da frente para posicionar o navio
                    val waveXFactor = (shipX / width) * 2.5 * PI + phase.toDouble()
                    // Subtraindo um valor para elevar o navio acima da linha d\'água
                    val shipY = midLineY + (waveAmplitude.toPx() * sin(waveXFactor)).toFloat() - 15.dp.toPx()

                    // Calculando a inclinação (derivada aproximada) para o balanço
                    val delta = 5f
                    val waveNextXFactor = ((shipX + delta) / width) * 2.5 * PI + phase.toDouble()
                    val wavePrevXFactor = ((shipX - delta) / width) * 2.5 * PI + phase.toDouble()

                    val yNext = midLineY + (waveAmplitude.toPx() * sin(waveNextXFactor)).toFloat()
                    val yPrev = midLineY + (waveAmplitude.toPx() * sin(wavePrevXFactor)).toFloat()

                    val angle = atan2((yNext - yPrev).toDouble(), (2 * delta).toDouble()) * (180 / PI)

                    withTransform({
                        translate(left = shipX, top = shipY)
                        rotate(degrees = angle.toFloat(), pivot = Offset.Zero)
                        scale(scaleX = 4.5f, scaleY = 4.5f, pivot = Offset.Zero)
                    }) {
                        // Casco do navio de papel (Branco)
                        val hullPath = Path()
                        hullPath.moveTo(-40f, -10f) // Ponta esquerda
                        hullPath.lineTo(40f, -10f)  // Ponta direita
                        hullPath.lineTo(20f, 15f)   // Base direita
                        hullPath.lineTo(-20f, 15f)  // Base esquerda
                        hullPath.close()
                        drawPath(hullPath, color = Color.White)

                        // Vela Principal (Azul Escuro)
                        val sailPath = Path()
                        sailPath.moveTo(0f, -10f)
                        sailPath.lineTo(0f, -55f) // Topo
                        sailPath.lineTo(30f, -10f)
                        sailPath.close()
                        drawPath(sailPath, color = Color(0xFF1565C0)) // Azul escuro

                        // Dobra menor da vela (Azul mais claro para profundidade)
                        val foldPath = Path()
                        foldPath.moveTo(0f, -10f)
                        foldPath.lineTo(0f, -40f)
                        foldPath.lineTo(-20f, -10f)
                        foldPath.close()
                        drawPath(foldPath, color = Color(0xFF90CAF9))
                    }

                    // Onda da frente (Branco Puro / Azul Oceano)
                    val pathFront = Path()
                    pathFront.moveTo(startX, height + 4000f)
                    pathFront.lineTo(startX, midLineY)

                    x = startX
                    while (x <= endX) {
                        val sine = sin((x / width) * 2.5 * PI + phase.toDouble()).toFloat()
                        val yPos = midLineY + (waveAmplitude.toPx() * sine)
                        pathFront.lineTo(x, yPos)
                        x += 10f
                    }
                    pathFront.lineTo(endX, midLineY)
                    pathFront.lineTo(endX, height + 4000f)
                    pathFront.close()

                    drawPath(pathFront, color = Color(0xFF039BE5)) // Azul oceano
                }
            }
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

        LaunchedEffect(skyState) {
            when (skyState) {
                SkyState.Sunset -> {
                    delay(2600)
                    skyState = SkyState.Night
                }
                SkyState.Sunrise -> {
                    delay(2600)
                    skyState = SkyState.Day
                }
                else -> Unit
            }
        }

        LaunchedEffect(skyState, moonY) {
            if (skyState == SkyState.Sunrise && moonY >= moonStartY - 1f) {
                skyState = SkyState.Day
            }
        }

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
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 48.dp) // Adjusted padding here
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
                                            when (skyState) {
                                                SkyState.Night -> {
                                                    skyState = SkyState.Sunrise   // lua começa a descer
                                                }

                                                SkyState.Sunset -> {
                                                    skyState = SkyState.Day
                                                }

                                                else -> {
                                                    skyState = SkyState.Day
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
                    .fillMaxSize()
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
                modifier = Modifier.fillMaxSize(),
                screenWidthPx = screenWidthPx,
                screenHeightPx = screenHeightPx,
                seaLevelYPx = currentSeaLevelYPx
            )
        }



        // Starry Sky
        if (skyState == SkyState.Night || skyState == SkyState.Sunrise) {

            StarrySky(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0.1f), // Behind the moon, but above background
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
            val cloudHeightInPixels = (20.0f / 24f) * size * density // Approximate height of the cloud in pixels

            // Ensure clouds are above the ship
            val maxCloudTopYAllowed = actualShipHighestPointYPx - cloudHeightInPixels - cloudClearancePaddingPx

            // For a single large cloud, we can fix its Y or make it slightly random within a smaller range.
            // Let\'s make it fill more of the top sky by picking a y value lower than usual
            val y = Random.nextFloat() * (maxCloudTopYAllowed * 0.7f).coerceAtLeast(0f)
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
                    color = color
                ));
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
                clouds.add(Cloud(x = Random.nextFloat() * screenWidth, y = y, size = size, speed = speed, alpha = alpha, color = color)) // Position them anywhere on screen
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

                    clouds.add(Cloud(x = screenWidth + size * density * 2, y = y, size = size, speed = speed, alpha = alpha, color = color)) // Start from right, adjusted for density
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

            // --- Apply Rotation Here ---
            val rotationDegrees = if (showRain) 180f else 0f // Apply 180 degrees rotation when raining
            val alpha = if (showRain) .95f else cloud.alpha // Reduce alpha when raining


            withTransform({
                translate(left = cloud.x, top = cloud.y)
                rotate(degrees = rotationDegrees, pivot = Offset(cloudSizePx / 2f, cloudSizePx / 2.5f)) // Rotate around the center of the cloud
            }) {
                drawPath(cloudPath, color = cloud.color, alpha = alpha)
            }
        }
    }
}

@Composable
fun RainEffect(
    modifier: Modifier = Modifier,
    screenWidthPx: Float,
    screenHeightPx: Float,
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
        val coveringCircleCenterX = moonCenterX + moonRadius * 2.5f * moonPhase.value

        drawCircle(
            color = Color.Black, // Match background color for \"hiding\" a part of the moon
            radius = moonRadius, // The covering circle has the same radius as the moon
            center = Offset(coveringCircleCenterX, moonCenterY)
        )
    }
}

@Composable
fun StarrySky(modifier: Modifier = Modifier, screenWidthPx: Float, screenHeightPx: Float, seaLevelYPx: Float) {
    val stars = remember { mutableStateListOf<Star>() }
    val infiniteTransition = rememberInfiniteTransition()
    val density = LocalDensity.current.density // Get density in composable scope

    LaunchedEffect(screenWidthPx, screenHeightPx, seaLevelYPx, density) { // Added density to dependencies
        if (screenWidthPx > 0 && screenHeightPx > 0) {
            stars.clear() // Clear existing stars on screen size or sea level change

            val starPaddingFromSeaPx = 140.dp.value * density // Use density directly
            val maxStarY = (seaLevelYPx - starPaddingFromSeaPx).coerceAtLeast(0f) // Max Y coordinate for a star

            // Generate random stars
            repeat(100) { // Number of random stars
                stars.add(
                    Star(
                        id = it,
                        x = Random.nextFloat() * screenWidthPx,
                        y = Random.nextFloat() * maxStarY, // Constrain Y to be above the sea
                        size = Random.nextFloat() * 2f + 1f, // Size between 1dp and 3dp
                        twinkleDuration = Random.nextInt(2000, 4000),
                        twinkleOffset = Random.nextInt(0, 2000)
                    )
                )
            }

            // Generate Cruzeiro do Sul (Southern Cross)
            // Relative positions for the 5 stars.
            // Let\'s place the constellation generally in the top-right quadrant
            val constellationBaseX = screenWidthPx * 0.7f
            val initialConstellationBaseY = screenHeightPx * 0.2f

            // Ensure constellationBaseY is also below maxStarY, considering the constellation's height
            val scale = 20f // Scale factor for the constellation\'s size
            val constellationHeight = scale * 2.5f // Approximate height of the constellation
            val adjustedConstellationBaseY = initialConstellationBaseY.coerceAtMost(maxStarY - constellationHeight)
            // Ensure it\'s not negative
            val finalConstellationBaseY = adjustedConstellationBaseY.coerceAtLeast(0f)


            val southernCrossStars = listOf(
                // Alpha Crucis (bottom)
                Star(id = stars.size, x = constellationBaseX, y = finalConstellationBaseY + (scale * 2), size = 3.5f, twinkleDuration = 3000, twinkleOffset = 0, isSouthernCross = true),
                // Beta Crucis (left)
                Star(id = stars.size + 1, x = constellationBaseX - scale, y = finalConstellationBaseY, size = 3.0f, twinkleDuration = 3200, twinkleOffset = 400, isSouthernCross = true),
                // Gamma Crucis (top)
                Star(id = stars.size + 2, x = constellationBaseX, y = finalConstellationBaseY - scale, size = 3.5f, twinkleDuration = 2800, twinkleOffset = 800, isSouthernCross = true),
                // Delta Crucis (right)
                Star(id = stars.size + 3, x = constellationBaseX + scale, y = finalConstellationBaseY + scale, size = 2.5f, twinkleDuration = 3500, twinkleOffset = 1200, isSouthernCross = true),
                // Epsilon Crucis (fainter, below Delta)
                Star(id = stars.size + 4, x = constellationBaseX + scale * 0.5f, y = finalConstellationBaseY + scale * 2.5f, size = 2.0f, twinkleDuration = 4000, twinkleOffset = 1600, isSouthernCross = true)
            )
            stars.addAll(southernCrossStars)
        }
    }

    // Map to hold the animated alpha state for each star
    val starAlphaStates = remember { mutableStateMapOf<Int, State<Float>>() }

    // Animate each star individually in a composable context
    stars.forEach { star ->
        key(star.id) { // Use key to provide a unique composable scope for each star
            val alpha = infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(star.twinkleDuration / 2, easing = LinearEasing, delayMillis = star.twinkleOffset),
                    repeatMode = RepeatMode.Reverse
                )
            )
            // Store the State<Float> in the map
            DisposableEffect(star.id) { // Use DisposableEffect to manage map entries
                starAlphaStates[star.id] = alpha
                onDispose { // Remove from map when the star is no longer composed
                    starAlphaStates.remove(star.id)
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        stars.forEach { star ->
            val alpha = starAlphaStates[star.id]?.value ?: 0f // Safely get the animated alpha value
            drawCircle(
                color = Color.White,
                radius = star.size,
                center = Offset(star.x, star.y),
                alpha = alpha
            )
        }
    }
}