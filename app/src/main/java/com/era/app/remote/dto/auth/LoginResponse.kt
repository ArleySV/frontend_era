package com.era.app.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
)
