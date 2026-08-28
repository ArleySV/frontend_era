package com.era.app.ui.login

import com.era.app.utils.EraError

enum class CampoLogin {
    USUARIO_O_CORREO,
    CONTRASENA,
}

data class LoginUiState(
    val usuarioOCorreo: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val cargando: Boolean = false,
    val errorGeneral: EraError? = null,
    val campoConError: CampoLogin? = null,
)

sealed interface LoginEvento {
    data object NavegarAHome : LoginEvento
    data object NavegarALogin : LoginEvento
    data object NavegarARegistro : LoginEvento
    data class MostrarSnackbar(val mensaje: String) : LoginEvento
}
