package com.era.app.ui.juego

import com.era.app.data.model.NivelConProgreso

enum class FaseJuego { CARGANDO, JUGANDO, RESULTADO, PAUSA, MENU }

data class JuegoUiState(
    val fase: FaseJuego = FaseJuego.CARGANDO,
    val nivel: NivelConProgreso? = null,
    val segundosRestantes: Int = 15,
    val opcionSeleccionada: Int? = null,
    val resultadoCorrecto: Boolean? = null,
    val segundosPausa: Int = 60,
    val mensajeResultado: String = "",
    val fraseSabia: String = "",
)

sealed interface JuegoEvento {
    data class NavegarANiveles(val orden: Int) : JuegoEvento
    data object VolverANiveles : JuegoEvento
}