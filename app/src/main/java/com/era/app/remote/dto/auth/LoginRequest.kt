package com.era.app.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("usuario_o_correo")
    val usuarioOCorreo: String,
    val contrasena: String,
)
