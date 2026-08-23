package com.era.app.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("nombre_menor")
    val nombreMenor: String,
    @SerialName("fecha_nacimiento")
    val fechaNacimiento: String,
    @SerialName("nombre_acudiente")
    val nombreAcudiente: String,
    @SerialName("cedula_acudiente")
    val cedulaAcudiente: String,
    val correo: String,
    @SerialName("nombre_usuario")
    val nombreUsuario: String,
    val avatar: String? = null,
    val contrasena: String,
    @SerialName("confirmar_contrasena")
    val confirmarContrasena: String,
)
