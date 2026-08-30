package com.era.app.ui.recuperacion

import com.era.app.utils.CriteriosContrasena
import com.era.app.utils.EraError
import com.era.app.utils.PasswordPolicy

enum class CampoRecuperacion {
    CORREO,
    CODIGO_OTP,
    NUEVA_CONTRASENA,
    CONFIRMAR_CONTRASENA,
}

data class RecuperacionUiState(
    val correo: String = "",
    val codigoOtp: String = "",
    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val nuevaContrasenaVisible: Boolean = false,
    val confirmarVisible: Boolean = false,
    val criteriosContrasena: CriteriosContrasena =
        PasswordPolicy.criterios(nuevaContrasena, "", ""),
    val reenvioSegundosRestantes: Int = 0,
    val errores: Set<CampoRecuperacion> = emptySet(),
    val errorGeneral: EraError? = null,
)

sealed interface RecuperacionEvento {
    data object NavegarAPaso2 : RecuperacionEvento
    data object NavegarAPaso3 : RecuperacionEvento
    data object RecuperacionExitosa : RecuperacionEvento
    data object ReiniciarFlujo : RecuperacionEvento
    data class Aviso(val error: EraError) : RecuperacionEvento
}