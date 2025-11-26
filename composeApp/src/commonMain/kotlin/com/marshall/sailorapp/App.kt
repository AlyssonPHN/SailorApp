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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.sin

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

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenHeight = maxHeight
            // Altura da parte líquida cheia
            val milkBodyHeight = screenHeight * heightPercentage
            // Altura extra para as ondas não serem cortadas
            val waveAmplitude = 15.dp
            
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
                
                // O nível médio da superfície do leite (onde a onda oscila)
                // Como adicionamos waveAmplitude à altura, o meio da onda fica em waveAmplitude a partir do topo
                val midLineY = waveAmplitude.toPx()

                // Onda de trás (Sombra/Mais escura para dar profundidade)
                val pathBack = Path()
                pathBack.moveTo(0f, height) // Começa embaixo
                pathBack.lineTo(0f, midLineY) // Sobe até o inicio da onda
                
                for (x in 0..width.toInt() step 10) {
                    val xPos = x.toFloat()
                    // Fase deslocada e frequência um pouco diferente
                    val sine = sin((xPos / width) * 4 * PI + phase.toDouble() + 1.0).toFloat()
                    val yPos = midLineY + (waveAmplitude.toPx() * 0.7f * sine)
                    pathBack.lineTo(xPos, yPos)
                }
                pathBack.lineTo(width, midLineY)
                pathBack.lineTo(width, height)
                pathBack.close()
                
                drawPath(pathBack, color = Color(0xFFE0E0E0)) // Branco levemente cinza

                // Onda da frente (Branco Puro)
                val pathFront = Path()
                pathFront.moveTo(0f, height)
                pathFront.lineTo(0f, midLineY)
                
                for (x in 0..width.toInt() step 10) {
                    val xPos = x.toFloat()
                    val sine = sin((xPos / width) * 2.5 * PI + phase.toDouble()).toFloat()
                    val yPos = midLineY + (waveAmplitude.toPx() * sine)
                    pathFront.lineTo(xPos, yPos)
                }
                pathFront.lineTo(width, midLineY)
                pathFront.lineTo(width, height)
                pathFront.close()
                
                drawPath(pathFront, color = Color.White)
            }
        }
    }
}
