package com.era.app.remote.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUsernameRequest(
    val nombreUsuario: String,
)
