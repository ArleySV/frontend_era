package com.era.app.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val nombreMenor: String,
    val fechaNacimiento: String,
    val nombreAcudiente: String,
    val cedulaAcudiente: String,
    val correo: String,
    val nombreUsuario: String,
    val avatar: String? = null,
    val contrasena: String,
    val confirmarContrasena: String,
)
