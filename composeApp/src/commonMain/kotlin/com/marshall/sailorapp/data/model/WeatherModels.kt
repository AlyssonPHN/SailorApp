package com.marshall.sailorapp.data.model

import androidx.compose.ui.graphics.Color

data class Cloud(
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float,
    val color: Color, // Add color property
    val isRainCloud: Boolean
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
    val x: Float,
    val y: Float,
    val radius: Float,
    val phase: Float,
    val speed: Float,
    val amplitude: Float,
    val baseAlpha: Float
)
