package com.era.app.ui.splash

data class SplashUiState(
    val cargando: Boolean = true,
)

sealed interface SplashEvento {
    data class NavegarAHome(val route: String) : SplashEvento
    data object NavegarALogin : SplashEvento
}
