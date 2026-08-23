package com.era.app.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ResendOtpRequest(
    val correo: String,
)
