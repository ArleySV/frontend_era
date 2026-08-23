package com.era.app.remote.dto.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("nombre_menor")
    val nombreMenor: String,
    @SerialName("fecha_nacimiento")
    val fechaNacimiento: String,
    val correo: String,
    @SerialName("nombre_usuario")
    val nombreUsuario: String,
    val avatar: String? = null,
)
