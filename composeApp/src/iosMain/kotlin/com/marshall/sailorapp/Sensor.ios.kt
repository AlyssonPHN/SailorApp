package com.marshall.sailorapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
actual fun rememberDeviceRotation(): State<Float> {
    // No iOS implementation for now, returning 0
    return remember { mutableStateOf(0f) }
}
