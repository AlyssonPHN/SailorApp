package com.marshall.sailorapp.model

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
