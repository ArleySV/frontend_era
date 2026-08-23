package com.era.app.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailRequest(
    val correo: String,
    val codigo: String,
)
