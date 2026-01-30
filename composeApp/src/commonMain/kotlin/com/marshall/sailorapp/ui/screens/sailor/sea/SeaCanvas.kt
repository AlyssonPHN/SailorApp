package com.marshall.sailorapp.ui.screens.sailor.sea

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SeaLayer(
    rotation: Float,
    onToggleExpand: () -> Unit,
    onSeaLevelCalculated: (Float) -> Unit
) {
    BoxWithConstraints {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clickable { onToggleExpand() }
        ) {
            drawSea(
                rotation = rotation,
                onSeaLevelCalculated = onSeaLevelCalculated
            )
        }
    }
}
