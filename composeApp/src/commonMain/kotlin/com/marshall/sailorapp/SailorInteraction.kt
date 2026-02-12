package com.marshall.sailorapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.marshall.sailorapp.model.SkyState

class SailorInteractionState(
    var showClouds: Boolean,
    var showRain: Boolean,
    var skyState: SkyState,
    var isExpanded: Boolean,
    var hasAppeared: Boolean
)

@Composable
fun rememberSailorInteractionState(
    initialShowClouds: Boolean = false,
    initialShowRain: Boolean = false,
    initialSkyState: SkyState = SkyState.Day,
    initialIsExpanded: Boolean = true,
    initialHasAppeared: Boolean = false
): SailorInteractionState {
    return remember {
        SailorInteractionState(
            showClouds = initialShowClouds,
            showRain = initialShowRain,
            skyState = initialSkyState,
            isExpanded = initialIsExpanded,
            hasAppeared = initialHasAppeared
        )
    }
}
