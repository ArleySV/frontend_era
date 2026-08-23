package com.era.app.remote.dto.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUsernameRequest(
    @SerialName("nombre_usuario")
    val nombreUsuario: String,
)
