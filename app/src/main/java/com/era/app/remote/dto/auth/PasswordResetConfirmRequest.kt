package com.era.app.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetConfirmRequest(
    val resetToken: String,
    val nuevaContrasena: String,
    val confirmarContrasena: String,
)
