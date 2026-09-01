package com.era.app.ui.perfil

import com.era.app.remote.dto.user.UserProfile
import com.era.app.utils.EraError

data class MiCuentaUiState(
    val cargando: Boolean = false,
    val perfil: UserProfile? = null,
    val errorGeneral: EraError? = null,
    val dialogoAbierto: Boolean = false,
    val nombreUsuario: String = "",
    val guardando: Boolean = false,
    val errorNombreUsuario: String? = null,
    val selectorAvatarAbierto: Boolean = false,
    val subiendoAvatar: Boolean = false,
    val bytesAvatarPersonalizado: ByteArray? = null,
    val errorAvatar: EraError? = null,
    val avatarPresetSeleccionado: Int? = null,
)

sealed interface MiCuentaEvento {
    data object NavegarALogin : MiCuentaEvento
    data class MostrarSnackbar(val mensaje: String) : MiCuentaEvento
}
