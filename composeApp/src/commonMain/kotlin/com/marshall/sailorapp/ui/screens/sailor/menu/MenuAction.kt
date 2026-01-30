package com.marshall.sailorapp.ui.screens.sailor.menu

sealed interface MenuAction {
    data object ToggleClouds : MenuAction
    data object ToggleRain : MenuAction
    data object Day : MenuAction
    data object Night : MenuAction
    data object Sunrise : MenuAction
    data object Sunset : MenuAction
}
