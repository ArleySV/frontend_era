package com.era.app.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val usuarioOCorreo: String,
    val contrasena: String,
)
