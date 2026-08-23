package com.era.app.remote.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val nombreMenor: String,
    val fechaNacimiento: String,
    val correo: String,
    val nombreUsuario: String,
    val avatar: String? = null,
)
