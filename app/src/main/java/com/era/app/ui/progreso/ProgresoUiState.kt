package com.era.app.ui.progreso

import com.era.app.data.model.NivelConProgreso
import com.era.app.utils.EraError

data class ProgresoUiState(
    val niveles: List<NivelConProgreso> = emptyList(),
    val cargando: Boolean = false,
    val sincronizando: Boolean = false,
    val error: EraError? = null,
    val nivelesCompletados: Int = 0,
    val porcentaje: Float = 0f,
    val reintentosTotales: Int = 0,
    val dialogoResetVisible: Boolean = false,
    val contrasenaReset: String = "",
    val reseteando: Boolean = false,
)

sealed interface ProgresoEvento {
    data class Error(val error: EraError) : ProgresoEvento
    data object SesionExpirada : ProgresoEvento
    data object ResetExitoso : ProgresoEvento
}
