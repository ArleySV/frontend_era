package com.era.app.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetConfirmRequest(
    @SerialName("reset_token")
    val resetToken: String,
    @SerialName("nueva_contrasena")
    val nuevaContrasena: String,
    @SerialName("confirmar_contrasena")
    val confirmarContrasena: String,
)
