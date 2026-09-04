package com.era.app.ui.niveles

import com.era.app.data.model.NivelConProgreso

data class NivelesUiState(
    val niveles: List<NivelConProgreso> = emptyList(),
    val cargando: Boolean = true,
)

sealed interface NivelesEvento {
    data class NavegarAJuego(val orden: Int) : NivelesEvento
}
