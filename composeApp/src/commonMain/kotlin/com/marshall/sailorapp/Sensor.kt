package com.marshall.sailorapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
expect fun rememberDeviceRotation(): State<Float>
