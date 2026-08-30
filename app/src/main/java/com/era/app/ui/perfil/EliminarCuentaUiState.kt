package com.era.app.ui.perfil

import com.era.app.utils.EraError

data class EliminarCuentaUiState(
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val cargando: Boolean = false,
    val errorGeneral: EraError? = null,
    val mostrarDialogoConfirmacion: Boolean = false
)

sealed interface EliminarCuentaEvento {
    data object NavegarALogin : EliminarCuentaEvento
}
