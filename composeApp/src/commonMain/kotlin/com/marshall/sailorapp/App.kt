package com.marshall.sailorapp

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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalDensity // Import LocalDensity
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.TileMode

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

@Composable
fun SailorScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E003E)) // Fundo Roxo Escuro
    ) {
        var isExpanded by remember { mutableStateOf(true) }
        var hasAppeared by remember { mutableStateOf(false) }
        var showClouds by remember { mutableStateOf(false) } // New state for clouds
        var showSun by remember { mutableStateOf(false) } // New state for sun

        val localDensity = LocalDensity.current // Declare LocalDensity here
        var screenWidthPx by remember { mutableStateOf(0f) }
        var screenHeightDp by remember { mutableStateOf(0.dp) }

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

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            screenHeightDp = maxHeight // Assign maxHeight to screenHeightDp
            val currentScreenWidthDp = maxWidth

            LaunchedEffect(currentScreenWidthDp) {
                screenWidthPx = with(localDensity) { currentScreenWidthDp.toPx() }
            }

            val rotationAbs = abs(rotation)
            val heightOffset = if (rotationAbs > 60f) {
                val progress = ((rotationAbs - 60f) / 30f).coerceIn(0f, 1f)
                screenHeightDp * 0.25f * progress
            } else {
                0.dp
            }

            // Altura da parte líquida cheia
            val milkBodyHeight = (screenHeightDp * heightPercentage) - heightOffset

            // Altura extra para as ondas não serem cortadas
            // Ajustamos a amplitude das ondas com base na rotação:
            // quando chega perto de 90 graus (landscape), reduzimos a amplitude.
            val rad = rotation.toDouble() * (PI / 180.0)
            val amplitudeFactor = 0.3f + 0.7f * abs(cos(rad)).toFloat()
            val waveAmplitude = 15.dp * amplitudeFactor

            // Altura total do container do leite
            val totalMilkHeight = if (milkBodyHeight > 0.dp) milkBodyHeight + waveAmplitude else 0.dp

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalMilkHeight)
                    .align(Alignment.BottomCenter)
                    .clickable { isExpanded = !isExpanded }
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
                    // Subtraindo um valor para elevar o navio acima da linha d'água
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

        // Removed LocalDensity.current from here, it's now at the top of SailorScreen

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
                                        Icons.Default.Cloud -> showClouds = !showClouds
                                        Icons.Default.WbSunny -> showSun = !showSun
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
        if (showClouds) {
            InfiniteClouds(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopStart),
                screenWidth = screenWidthPx
            )
        }

        // Infinite Sun
        if (showSun) {
            val sunSizeDp = 100.dp
            val sunSizePx = with(localDensity) { sunSizeDp.toPx() }
            val sunOffsetX = with(localDensity) { (screenWidthPx - sunSizePx) / 2 } // Center horizontally
            val sunOffsetY = with(localDensity) { (screenHeightDp * 0.2f).toPx() } // Top part
            InfiniteSun(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopStart),
                offsetX = sunOffsetX,
                offsetY = sunOffsetY,
                sunSize = sunSizePx,
                color = Color(0xFFFFC107) // Amarelo para o sol
            )
        }
    }
}

@Composable
fun InfiniteClouds(modifier: Modifier = Modifier, screenWidth: Float) {
    val clouds = remember { mutableStateListOf<Cloud>() }
    val density = LocalDensity.current.density

    LaunchedEffect(Unit) {
        var lastFrameTime = 0L
        while (true) {
            withFrameNanos { frameTime ->
                val deltaTimeNs = if (lastFrameTime > 0) frameTime - lastFrameTime else 0L
                lastFrameTime = frameTime
                val deltaTimeSeconds = deltaTimeNs / 1_000_000_000f

                // Update existing clouds
                for (i in clouds.indices.reversed()) {
                    val cloud = clouds[i]
                    cloud.x += cloud.speed * deltaTimeSeconds * density // Adjust speed by density
                    if (cloud.x > screenWidth + cloud.size * 2) { // Remove if off-screen to the right
                        clouds.removeAt(i)
                    }
                }

                // Add new clouds periodically
                if (Random.nextFloat() < 0.02f) { // Probability of adding a new cloud each frame
                    val size = Random.nextFloat() * 40f + 30f // Cloud size between 30dp and 70dp
                    val speed = Random.nextFloat() * 50f + 20f // Cloud speed between 20dp/s and 70dp/s
                    val y = Random.nextFloat() * (300f) // Y position between 0dp and 300dp from top
                    val alpha = Random.nextFloat() * 0.4f + 0.3f // Alpha between 0.3 and 0.7
                    val color = Color.White.copy(alpha = alpha) // White clouds with varying alpha

                    clouds.add(Cloud(x = -size * 2, y = y, size = size, speed = speed, alpha = alpha, color = color))
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        clouds.forEach { cloud ->
            val cloudSizePx = cloud.size * density
            val iconGridSize = 24f // Material icons are typically based on a 24x24 grid
            val scaleFactor = cloudSizePx / iconGridSize

            // We need to keep track of the current point for relative commands
            var currentLocalX = 0f
            var currentLocalY = 0f

            val cloudPath = Path().apply {
                // Initial moveTo
                val startLocalX = 19.35f * scaleFactor
                val startLocalY = 10.04f * scaleFactor
                moveTo(startLocalX, startLocalY)
                currentLocalX = startLocalX
                currentLocalY = startLocalY

                // curveTo(18.67f, 6.59f, 15.64f, 4.0f, 12.0f, 4.0f)
                val c1x_abs1 = 18.67f * scaleFactor
                val c1y_abs1 = 6.59f * scaleFactor
                val c2x_abs1 = 15.64f * scaleFactor
                val c2y_abs1 = 4.0f * scaleFactor
                val endLocalX1 = 12.0f * scaleFactor
                val endLocalY1 = 4.0f * scaleFactor
                cubicTo(c1x_abs1, c1y_abs1, c2x_abs1, c2y_abs1, endLocalX1, endLocalY1)
                currentLocalX = endLocalX1
                currentLocalY = endLocalY1

                // curveTo(9.11f, 4.0f, 6.6f, 5.64f, 5.35f, 8.04f)
                val c1x_abs2 = 9.11f * scaleFactor
                val c1y_abs2 = 4.0f * scaleFactor
                val c2x_abs2 = 6.6f * scaleFactor
                val c2y_abs2 = 5.64f * scaleFactor
                val endLocalX2 = 5.35f * scaleFactor
                val endLocalY2 = 8.04f * scaleFactor
                cubicTo(c1x_abs2, c1y_abs2, c2x_abs2, c2y_abs2, endLocalX2, endLocalY2)
                currentLocalX = endLocalX2
                currentLocalY = endLocalY2

                // curveTo(2.34f, 8.36f, 0.0f, 10.91f, 0.0f, 14.0f)
                val c1x_abs3 = 2.34f * scaleFactor
                val c1y_abs3 = 8.36f * scaleFactor
                val c2x_abs3 = 0.0f * scaleFactor
                val c2y_abs3 = 10.91f * scaleFactor
                val endLocalX3 = 0.0f * scaleFactor
                val endLocalY3 = 14.0f * scaleFactor
                cubicTo(c1x_abs3, c1y_abs3, c2x_abs3, c2y_abs3, endLocalX3, endLocalY3)
                currentLocalX = endLocalX3
                currentLocalY = endLocalY3

                // curveToRelative(0.0f, 3.31f, 2.69f, 6.0f, 6.0f, 6.0f)
                val dc1x_rel1 = 0.0f * scaleFactor
                val dc1y_rel1 = 3.31f * scaleFactor
                val dc2x_rel1 = 2.69f * scaleFactor
                val dc2y_rel1 = 6.0f * scaleFactor
                val dex_rel1 = 6.0f * scaleFactor
                val dey_rel1 = 6.0f * scaleFactor
                cubicTo(
                    currentLocalX + dc1x_rel1, currentLocalY + dc1y_rel1,
                    currentLocalX + dc2x_rel1, currentLocalY + dc2y_rel1,
                    currentLocalX + dex_rel1, currentLocalY + dey_rel1
                )
                currentLocalX += dex_rel1
                currentLocalY += dey_rel1

                // horizontalLineToRelative(13.0f)
                val dhx_rel = 13.0f * scaleFactor
                lineTo(currentLocalX + dhx_rel, currentLocalY)
                currentLocalX += dhx_rel

                // curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                val dc1x_rel2 = 2.76f * scaleFactor
                val dc1y_rel2 = 0.0f * scaleFactor
                val dc2x_rel2 = 5.0f * scaleFactor
                val dc2y_rel2 = -2.24f * scaleFactor
                val dex_rel2 = 5.0f * scaleFactor
                val dey_rel2 = -5.0f * scaleFactor
                cubicTo(
                    currentLocalX + dc1x_rel2, currentLocalY + dc1y_rel2,
                    currentLocalX + dc2x_rel2, currentLocalY + dc2y_rel2,
                    currentLocalX + dex_rel2, currentLocalY + dey_rel2
                )
                currentLocalX += dex_rel2
                currentLocalY += dey_rel2

                // curveToRelative(0.0f, -2.64f, -2.05f, -4.78f, -4.65f, -4.96f)
                val dc1x_rel3 = 0.0f * scaleFactor
                val dc1y_rel3 = -2.64f * scaleFactor
                val dc2x_rel3 = -2.05f * scaleFactor
                val dc2y_rel3 = -4.78f * scaleFactor
                val dex_rel3 = -4.65f * scaleFactor
                val dey_rel3 = -4.96f * scaleFactor
                cubicTo(
                    currentLocalX + dc1x_rel3, currentLocalY + dc1y_rel3,
                    currentLocalX + dc2x_rel3, currentLocalY + dc2y_rel3,
                    currentLocalX + dex_rel3, currentLocalY + dey_rel3
                )

                close()
            }

            // Apply translation when drawing the path
            withTransform({
                translate(left = cloud.x * density, top = cloud.y * density)
            }) {
                drawPath(cloudPath, color = cloud.color, alpha = cloud.alpha)
            }
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
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val iconGridSize = 24f // Material icons are typically based on a 24x24 grid
    val scaleFactor = sunSize / iconGridSize

    // Pivot for rotation, center of the 24x24 grid is (12, 12)
    val pivotX = 12f * scaleFactor
    val pivotY = 12f * scaleFactor

    val sunPath = remember(scaleFactor) {
        Path().apply {
            var currentX = 0f
            var currentY = 0f
            var lastC2X = 0f
            var lastC2Y = 0f

            // Helper to update currentX/Y after a command
            fun updateCurrent(x: Float, y: Float) {
                currentX = x
                currentY = y
            }

            // Central Circle
            // moveTo(12.0f, 5.5f)
            moveTo(12.0f * scaleFactor, 5.5f * scaleFactor)
            updateCurrent(12.0f * scaleFactor, 5.5f * scaleFactor)

            // curveToRelative(-3.31f, 0.0f, -6.0f, 2.69f, -6.0f, 6.0f)
            val c1x_r1 = currentX + (-3.31f * scaleFactor)
            val c1y_r1 = currentY + (0.0f * scaleFactor)
            val c2x_r1 = currentX + (-6.0f * scaleFactor)
            val c2y_r1 = currentY + (2.69f * scaleFactor)
            val endX_r1 = currentX + (-6.0f * scaleFactor)
            val endY_r1 = currentY + (6.0f * scaleFactor)
            cubicTo(c1x_r1, c1y_r1, c2x_r1, c2y_r1, endX_r1, endY_r1)
            updateCurrent(endX_r1, endY_r1)
            lastC2X = c2x_r1
            lastC2Y = c2y_r1

            // reflectiveCurveToRelative(2.69f, 6.0f, 6.0f, 6.0f)
            val rc1x_r2 = currentX + (currentX - lastC2X) // Reflection
            val rc1y_r2 = currentY + (currentY - lastC2Y) // Reflection
            val rc2x_r2 = currentX + (2.69f * scaleFactor)
            val rc2y_r2 = currentY + (6.0f * scaleFactor)
            val rendX_r2 = currentX + (6.0f * scaleFactor)
            val rendY_r2 = currentY + (6.0f * scaleFactor)
            cubicTo(rc1x_r2, rc1y_r2, rc2x_r2, rc2y_r2, rendX_r2, rendY_r2)
            updateCurrent(rendX_r2, rendY_r2)
            lastC2X = rc2x_r2
            lastC2Y = rc2y_r2

            // reflectiveCurveToRelative(6.0f, -2.69f, 6.0f, -6.0f)
            val rc1x_r3 = currentX + (currentX - lastC2X)
            val rc1y_r3 = currentY + (currentY - lastC2Y)
            val rc2x_r3 = currentX + (6.0f * scaleFactor)
            val rc2y_r3 = currentY + (-2.69f * scaleFactor)
            val rendX_r3 = currentX + (6.0f * scaleFactor)
            val rendY_r3 = currentY + (-6.0f * scaleFactor)
            cubicTo(rc1x_r3, rc1y_r3, rc2x_r3, rc2y_r3, rendX_r3, rendY_r3)
            updateCurrent(rendX_r3, rendY_r3)
            lastC2X = rc2x_r3
            lastC2Y = rc2y_r3

            // reflectiveCurveToRelative(-2.69f, -6.0f, -6.0f, -6.0f)
            val rc1x_r4 = currentX + (currentX - lastC2X)
            val rc1y_r4 = currentY + (currentY - lastC2Y)
            val rc2x_r4 = currentX + (-2.69f * scaleFactor)
            val rc2y_r4 = currentY + (-6.0f * scaleFactor)
            val rendX_r4 = currentX + (-6.0f * scaleFactor)
            val rendY_r4 = currentY + (-6.0f * scaleFactor)
            cubicTo(rc1x_r4, rc1y_r4, rc2x_r4, rc2y_r4, rendX_r4, rendY_r4)
            updateCurrent(rendX_r4, rendY_r4)
            // lastC2X and lastC2Y not strictly needed as we `close()` next
            close() // Closes the central circle


            // Rays - Each ray is a new subpath starting with moveTo
            // Ray 1 (top-left diagonal)
            // moveTo(6.76f, 4.84f)
            moveTo(6.76f * scaleFactor, 4.84f * scaleFactor)
            updateCurrent(6.76f * scaleFactor, 4.84f * scaleFactor)
            // lineToRelative(-1.8f, -1.79f)
            lineTo(currentX + (-1.8f * scaleFactor), currentY + (-1.79f * scaleFactor))
            updateCurrent(currentX + (-1.8f * scaleFactor), currentY + (-1.79f * scaleFactor))
            // lineToRelative(-1.41f, 1.41f)
            lineTo(currentX + (-1.41f * scaleFactor), currentY + (1.41f * scaleFactor))
            updateCurrent(currentX + (-1.41f * scaleFactor), currentY + (1.41f * scaleFactor))
            // lineToRelative(1.79f, 1.79f)
            lineTo(currentX + (1.79f * scaleFactor), currentY + (1.79f * scaleFactor))
            updateCurrent(currentX + (1.79f * scaleFactor), currentY + (1.79f * scaleFactor))
            // lineToRelative(1.42f, -1.41f)
            lineTo(currentX + (1.42f * scaleFactor), currentY + (-1.41f * scaleFactor))
            updateCurrent(currentX + (1.42f * scaleFactor), currentY + (-1.41f * scaleFactor))
            close() // Closes the first ray

            // Ray 2 (left horizontal)
            // moveTo(4.0f, 10.5f)
            moveTo(4.0f * scaleFactor, 10.5f * scaleFactor)
            updateCurrent(4.0f * scaleFactor, 10.5f * scaleFactor)
            // lineTo(1.0f, 10.5f)
            lineTo(1.0f * scaleFactor, 10.5f * scaleFactor)
            updateCurrent(1.0f * scaleFactor, 10.5f * scaleFactor)
            // verticalLineToRelative(2.0f)
            lineTo(currentX, currentY + (2.0f * scaleFactor))
            updateCurrent(currentX, currentY + (2.0f * scaleFactor))
            // horizontalLineToRelative(3.0f)
            lineTo(currentX + (3.0f * scaleFactor), currentY)
            updateCurrent(currentX + (3.0f * scaleFactor), currentY)
            // The original implicitly closes the path, explicitly connect to the start for clarity
            lineTo(4.0f * scaleFactor, 10.5f * scaleFactor)
            close()

            // Ray 3 (top vertical)
            // moveTo(13.0f, 0.55f)
            moveTo(13.0f * scaleFactor, 0.55f * scaleFactor)
            updateCurrent(13.0f * scaleFactor, 0.55f * scaleFactor)
            // horizontalLineToRelative(-2.0f)
            lineTo(currentX + (-2.0f * scaleFactor), currentY)
            updateCurrent(currentX + (-2.0f * scaleFactor), currentY)
            // lineTo(11.0f, 3.5f)
            lineTo(11.0f * scaleFactor, 3.5f * scaleFactor)
            updateCurrent(11.0f * scaleFactor, 3.5f * scaleFactor)
            // horizontalLineToRelative(2.0f)
            lineTo(currentX + (2.0f * scaleFactor), currentY)
            updateCurrent(currentX + (2.0f * scaleFactor), currentY)
            // lineTo(13.0f, 0.55f)
            lineTo(13.0f * scaleFactor, 0.55f * scaleFactor)
            close()

            // Ray 4 (top-right diagonal)
            // moveTo(20.45f, 4.46f)
            moveTo(20.45f * scaleFactor, 4.46f * scaleFactor)
            updateCurrent(20.45f * scaleFactor, 4.46f * scaleFactor)
            // lineToRelative(-1.41f, -1.41f)
            lineTo(currentX + (-1.41f * scaleFactor), currentY + (-1.41f * scaleFactor))
            updateCurrent(currentX + (-1.41f * scaleFactor), currentY + (-1.41f * scaleFactor))
            // lineToRelative(-1.79f, 1.79f)
            lineTo(currentX + (-1.79f * scaleFactor), currentY + (1.79f * scaleFactor))
            updateCurrent(currentX + (-1.79f * scaleFactor), currentY + (1.79f * scaleFactor))
            // lineToRelative(1.41f, 1.41f)
            lineTo(currentX + (1.41f * scaleFactor), currentY + (1.41f * scaleFactor))
            updateCurrent(currentX + (1.41f * scaleFactor), currentY + (1.41f * scaleFactor))
            // lineToRelative(1.79f, -1.79f)
            lineTo(currentX + (1.79f * scaleFactor), currentY + (-1.79f * scaleFactor))
            updateCurrent(currentX + (1.79f * scaleFactor), currentY + (-1.79f * scaleFactor))
            close()

            // Ray 5 (bottom-right diagonal)
            // moveTo(17.24f, 18.16f)
            moveTo(17.24f * scaleFactor, 18.16f * scaleFactor)
            updateCurrent(17.24f * scaleFactor, 18.16f * scaleFactor)
            // lineToRelative(1.79f, 1.8f)
            lineTo(currentX + (1.79f * scaleFactor), currentY + (1.8f * scaleFactor))
            updateCurrent(currentX + (1.79f * scaleFactor), currentY + (1.8f * scaleFactor))
            // lineToRelative(1.41f, -1.41f)
            lineTo(currentX + (1.41f * scaleFactor), currentY + (-1.41f * scaleFactor))
            updateCurrent(currentX + (1.41f * scaleFactor), currentY + (-1.41f * scaleFactor))
            // lineToRelative(-1.8f, -1.79f)
            lineTo(currentX + (-1.8f * scaleFactor), currentY + (-1.79f * scaleFactor))
            updateCurrent(currentX + (-1.8f * scaleFactor), currentY + (-1.79f * scaleFactor))
            // lineToRelative(-1.4f, 1.4f)
            lineTo(currentX + (-1.4f * scaleFactor), currentY + (1.4f * scaleFactor))
            updateCurrent(currentX + (-1.4f * scaleFactor), currentY + (1.4f * scaleFactor))
            close()

            // Ray 6 (right horizontal)
            // moveTo(20.0f, 10.5f)
            moveTo(20.0f * scaleFactor, 10.5f * scaleFactor)
            updateCurrent(20.0f * scaleFactor, 10.5f * scaleFactor)
            // verticalLineToRelative(2.0f)
            lineTo(currentX, currentY + (2.0f * scaleFactor))
            updateCurrent(currentX, currentY + (2.0f * scaleFactor))
            // horizontalLineToRelative(3.0f)
            lineTo(currentX + (3.0f * scaleFactor), currentY)
            updateCurrent(currentX + (3.0f * scaleFactor), currentY)
            // verticalLineToRelative(-2.0f)
            lineTo(currentX, currentY + (-2.0f * scaleFactor))
            updateCurrent(currentX, currentY + (-2.0f * scaleFactor))
            // horizontalLineToRelative(-3.0f)
            lineTo(currentX + (-3.0f * scaleFactor), currentY)
            updateCurrent(currentX + (-3.0f * scaleFactor), currentY)
            close()

            // Ray 7 (bottom vertical)
            // moveTo(11.0f, 22.45f)
            moveTo(11.0f * scaleFactor, 22.45f * scaleFactor)
            updateCurrent(11.0f * scaleFactor, 22.45f * scaleFactor)
            // horizontalLineToRelative(2.0f)
            lineTo(currentX + (2.0f * scaleFactor), currentY)
            updateCurrent(currentX + (2.0f * scaleFactor), currentY)
            // lineTo(13.0f, 19.5f)
            lineTo(13.0f * scaleFactor, 19.5f * scaleFactor)
            updateCurrent(13.0f * scaleFactor, 19.5f * scaleFactor)
            // horizontalLineToRelative(-2.0f)
            lineTo(currentX + (-2.0f * scaleFactor), currentY)
            updateCurrent(currentX + (-2.0f * scaleFactor), currentY)
            // verticalLineToRelative(2.95f)
            lineTo(currentX, currentY + (2.95f * scaleFactor))
            updateCurrent(currentX, currentY + (2.95f * scaleFactor))
            close()

            // Ray 8 (bottom-left diagonal)
            // moveTo(3.55f, 18.54f)
            moveTo(3.55f * scaleFactor, 18.54f * scaleFactor)
            updateCurrent(3.55f * scaleFactor, 18.54f * scaleFactor)
            // lineToRelative(1.41f, 1.41f)
            lineTo(currentX + (1.41f * scaleFactor), currentY + (1.41f * scaleFactor))
            updateCurrent(currentX + (1.41f * scaleFactor), currentY + (1.41f * scaleFactor))
            // lineToRelative(1.79f, -1.8f)
            lineTo(currentX + (1.79f * scaleFactor), currentY + (-1.8f * scaleFactor))
            updateCurrent(currentX + (1.79f * scaleFactor), currentY + (-1.8f * scaleFactor))
            // lineToRelative(-1.41f, -1.41f)
            lineTo(currentX + (-1.41f * scaleFactor), currentY + (-1.41f * scaleFactor))
            updateCurrent(currentX + (-1.41f * scaleFactor), currentY + (-1.41f * scaleFactor))
            // lineToRelative(-1.79f, 1.8f)
            lineTo(currentX + (-1.79f * scaleFactor), currentY + (1.8f * scaleFactor))
            updateCurrent(currentX + (-1.79f * scaleFactor), currentY + (1.8f * scaleFactor))
            close()
        }
    }

    Canvas(modifier = modifier) {
        // Draw the sun path with translation and rotation
        withTransform({
            translate(left = offsetX, top = offsetY)
            rotate(degrees = sunRotation, pivot = Offset(pivotX, pivotY))
        }) {
            drawPath(sunPath, color = color, alpha = 1f) // Alpha fixed to 1f for the sun
        }
    }
}