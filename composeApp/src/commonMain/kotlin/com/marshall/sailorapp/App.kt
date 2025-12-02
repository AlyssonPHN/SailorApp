package com.marshall.sailorapp

import androidx.compose.animation.core.*
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

@Composable
@Preview
fun App() {
    MaterialTheme {
        SailorScreen()
    }
}

@Composable
fun SailorScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E003E)) // Fundo Roxo Escuro
    ) {
        var isExpanded by remember { mutableStateOf(true) }
        var hasAppeared by remember { mutableStateOf(false) }

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
            val screenHeight = maxHeight

            val rotationAbs = abs(rotation)
            val heightOffset = if (rotationAbs > 60f) {
                val progress = ((rotationAbs - 60f) / 30f).coerceIn(0f, 1f)
                screenHeight * 0.25f * progress
            } else {
                0.dp
            }

            // Altura da parte líquida cheia
            val milkBodyHeight = (screenHeight * heightPercentage) - heightOffset

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
    }
}
