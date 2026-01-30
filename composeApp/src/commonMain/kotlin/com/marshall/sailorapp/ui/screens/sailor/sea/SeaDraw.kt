package com.marshall.sailorapp.ui.screens.sailor.sea

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.sin

fun DrawScope.drawSea(
    rotation: Float,
    onSeaLevelCalculated: (Float) -> Unit
) {
    val width = size.width
    val height = size.height

    // nível do mar (exemplo simples)
    val seaLevelY = height * 0.3f
    onSeaLevelCalculated(seaLevelY)

    withTransform({
        rotate(degrees = -rotation, pivot = Offset(width / 2, height / 2))
    }) {
        val path = Path().apply {
            moveTo(0f, height)
            lineTo(0f, seaLevelY)

            var x = 0f
            while (x <= width) {
                val y = seaLevelY + sin((x / width) * 2 * PI).toFloat() * 20f
                lineTo(x, y)
                x += 10f
            }

            lineTo(width, seaLevelY)
            lineTo(width, height)
            close()
        }

        drawPath(
            path = path,
            color = Color(0xFF039BE5)
        )
    }
}
