package com.era.app.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetVerifyRequest(
    val correo: String,
    val codigo: String,
)
