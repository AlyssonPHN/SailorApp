package com.marshall.sailorapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sin

@Composable
fun Waves(
    totalMilkHeight: Dp,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    rotation: Float,
    phase: Float,
    waveAmplitude: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(totalMilkHeight)
            .clickable { onExpandedChange(!isExpanded) }
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

            val pathBack = Path()
            pathBack.moveTo(startX, height + 4000f)
            pathBack.lineTo(startX, midLineY)

            var x = startX
            while (x <= endX) {
                val sine = sin((x / width) * 4 * PI + phase.toDouble() + 1.0).toFloat()
                val yPos = midLineY + (waveAmplitude.toPx() * 0.7f * sine)
                pathBack.lineTo(x, yPos)
                x += 10f
            }
            pathBack.lineTo(endX, midLineY)
            pathBack.lineTo(endX, height + 4000f)
            pathBack.close()

            drawPath(pathBack, color = Color(0xFF4FC3F7))

            val shipX = width / 2
            val waveXFactor = (shipX / width) * 2.5 * PI + phase.toDouble()
            val shipY = midLineY + (waveAmplitude.toPx() * sin(waveXFactor)).toFloat() - 15f

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
                val hullPath = Path()
                hullPath.moveTo(-40f, -10f)
                hullPath.lineTo(40f, -10f)
                hullPath.lineTo(20f, 15f)
                hullPath.lineTo(-20f, 15f)
                hullPath.close()
                drawPath(hullPath, color = Color.White)

                val sailPath = Path()
                sailPath.moveTo(0f, -10f)
                sailPath.lineTo(0f, -55f)
                sailPath.lineTo(30f, -10f)
                sailPath.close()
                drawPath(sailPath, color = Color(0xFF1565C0))

                val foldPath = Path()
                foldPath.moveTo(0f, -10f)
                foldPath.lineTo(0f, -40f)
                foldPath.lineTo(-20f, -10f)
                foldPath.close()
                drawPath(foldPath, color = Color(0xFF90CAF9))
            }

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

            drawPath(pathFront, color = Color(0xFF039BE5))
        }
    }
}
