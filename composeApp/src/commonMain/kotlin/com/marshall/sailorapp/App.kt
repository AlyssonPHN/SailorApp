package com.marshall.sailorapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.marshall.sailorapp.ui.sailorscreen.SailorScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        SailorScreen()
    }
}
