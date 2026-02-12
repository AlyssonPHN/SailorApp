package com.marshall.sailorapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.marshall.sailorapp.model.SkyState

class SailorViewModel {
    private var _showClouds by mutableStateOf(false)
    val showClouds: Boolean get() = _showClouds

    private var _showRain by mutableStateOf(false)
    val showRain: Boolean get() = _showRain

    private var _skyState by mutableStateOf(SkyState.Day)
    val skyState: SkyState get() = _skyState

    private var _isExpanded by mutableStateOf(true)
    val isExpanded: Boolean get() = _isExpanded

    private var _hasAppeared by mutableStateOf(false)
    val hasAppeared: Boolean get() = _hasAppeared

    fun toggleClouds() {
        _showClouds = !_showClouds
    }

    fun toggleRain() {
        _showRain = !_showRain
    }

    fun setSkyState(newSkyState: SkyState) {
        _skyState = newSkyState
    }

    fun setExpanded(newIsExpanded: Boolean) {
        _isExpanded = newIsExpanded
    }

    fun setHasAppeared(newHasAppeared: Boolean) {
        _hasAppeared = newHasAppeared
    }
}
